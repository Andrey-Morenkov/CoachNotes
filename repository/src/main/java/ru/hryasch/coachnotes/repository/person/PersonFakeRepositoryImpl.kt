package ru.hryasch.coachnotes.repository.person

import com.github.javafaker.Faker
import com.pawegio.kandroid.d
import com.pawegio.kandroid.i
import io.realm.Realm
import io.realm.kotlin.where
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import org.koin.core.KoinComponent
import org.koin.core.get
import org.koin.core.inject
import org.koin.core.qualifier.named
import ru.hryasch.coachnotes.domain.group.data.Group
import ru.hryasch.coachnotes.domain.person.data.Person
import ru.hryasch.coachnotes.domain.repository.GroupRepository
import ru.hryasch.coachnotes.domain.repository.PersonRepository
import ru.hryasch.coachnotes.repository.common.GroupId
import ru.hryasch.coachnotes.repository.common.PersonId
import ru.hryasch.coachnotes.repository.converters.fromDAO
import ru.hryasch.coachnotes.repository.converters.fromDao
import ru.hryasch.coachnotes.repository.converters.toDao
import ru.hryasch.coachnotes.repository.dao.GroupDAO
import ru.hryasch.coachnotes.repository.dao.PersonDAO
import java.util.*

@ExperimentalCoroutinesApi
class PersonFakeRepositoryImpl: PersonRepository, KoinComponent
{
    private val faker: Faker = Faker(Locale("ru"))
    private val groupRepo: GroupRepository by inject(named("mock"))

    private val initializingJob: Job

    init
    {
        initializingJob = GlobalScope.launch(Dispatchers.Default)
        {
            generatePersonDb()
            setChangesTriggers()
        }
    }

    private suspend fun generatePersonDb()
    {
        val db = getDb()
        db.refresh()

        db.executeTransaction {
            it.deleteAll()
        }

        for (group in groupRepo.getAllGroups()!!)
        {
            for (personId in group.membersList)
            {
                val newPerson = PersonDAO(personId, faker.name().firstName(), faker.name().lastName(), "01/01/2014")
                    .apply {
                        groupId = group.id
                        isPaid = group.isPaid
                    }

                d("Generated person: ${newPerson.name} ${newPerson.surname} (id: ${newPerson.id} / groupId: ${newPerson.groupId})")

                getDb().executeTransaction {
                    it.copyToRealm(newPerson)
                }
            }
        }
    }

    override suspend fun getPerson(personId: PersonId): Person?
    {
        val db = getDb()
        db.refresh()

        val personDao = db.where<PersonDAO>()
                          .equalTo("id", personId)
                          .findFirst()

        return personDao?.fromDao()
    }

    override suspend fun getPersonsByGroup(groupId: GroupId): List<Person>?
    {
        val db = getDb()
        db.refresh()

        val personsDao = db.where<PersonDAO>()
                        .equalTo("groupId", groupId)
                        .findAll()

        return if (personsDao.isEmpty())
        {
            null
        }
        else
        {
            personsDao.fromDAO()
        }
    }

    override suspend fun getAllPeople(): List<Person>?
    {
        if (initializingJob.isActive) initializingJob.join()

        val db = getDb()
        db.refresh()

        val peopleList = db.where<PersonDAO>().findAll()
        return if (peopleList.isEmpty())
        {
            null
        }
        else
        {
            peopleList.fromDAO()
        }
    }

    override suspend fun addOrUpdatePerson(person: Person)
    {
        val db = getDb()
        db.refresh()

        db.executeTransaction {
            it.copyToRealmOrUpdate(person.toDao())
        }

        val broadcastChannel: ConflatedBroadcastChannel<Person> = get(named("sendSpecificPerson"))
        val personGroupBroadcastChannel: ConflatedBroadcastChannel<Group> = get(named("sendSpecificGroup"))

        val personDb = db.where<PersonDAO>().equalTo("id", person.id).findFirst()

        personDb?.let {
            i("channel <sendSpecificPerson>: SEND")
            broadcastChannel.send(it.fromDao())
        }
    }

    override suspend fun deletePerson(person: Person)
    {
        val db = getDb()
        db.refresh()

        val target = db.where<PersonDAO>()
                       .equalTo("id", person.id)
                       .findAll()

        db.executeTransaction {
            target.deleteAllFromRealm()
        }

        val allBroadcastChannel: ConflatedBroadcastChannel<List<Person>> = get(named("sendPeopleList"))
        val specificBroadcastChannel: ConflatedBroadcastChannel<List<Person>> = get(named("sendPeopleByGroup"))

        val allPeople = db.where<PersonDAO>().findAll()
        val specificPeople = db.where<PersonDAO>().equalTo("groupId", person.groupId).findAll()

        i("channel <sendPeopleList>: SEND")
        allBroadcastChannel.send(allPeople.fromDAO())

        i("channel <sendPeopleByGroup[${person.groupId}]>: SEND")
        specificBroadcastChannel.send(specificPeople.fromDAO())
    }

    @ExperimentalCoroutinesApi
    private suspend fun setChangesTriggers()
    {
        setAllPeopleChangesTrigger()
    }

    @ExperimentalCoroutinesApi
    private suspend fun setAllPeopleChangesTrigger()
    {
        val broadcastChannel: ConflatedBroadcastChannel<List<Person>> = get(named("sendPeopleList"))

        withContext(Dispatchers.Main)
        {
            val db = getDb()
            db.refresh()

            val allPeople = db.where<PersonDAO>().findAll()
            allPeople.addChangeListener { t, _ ->
                i("channel <sendPeopleList>: add change listener")
                GlobalScope.launch(Dispatchers.Main)
                {
                    i("channel <sendPeopleList>: SEND")
                    broadcastChannel.send(t.fromDAO())
                }
            }
        }
    }

    private fun getDb(): Realm = Realm.getInstance(get(named("persons_mock")))
}