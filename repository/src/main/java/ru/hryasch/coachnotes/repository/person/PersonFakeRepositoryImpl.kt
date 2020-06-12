package ru.hryasch.coachnotes.repository.person

import com.github.javafaker.Faker
import com.pawegio.kandroid.d
import com.pawegio.kandroid.e
import com.pawegio.kandroid.i
import com.pawegio.kandroid.w
import io.realm.ObjectChangeSet
import io.realm.Realm
import io.realm.RealmResults
import io.realm.kotlin.where
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import org.koin.core.KoinComponent
import org.koin.core.get
import org.koin.core.inject
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
import ru.hryasch.coachnotes.domain.group.data.Group
import ru.hryasch.coachnotes.domain.person.data.Person
import ru.hryasch.coachnotes.domain.repository.GroupRepository
import ru.hryasch.coachnotes.domain.repository.PersonRepository
import ru.hryasch.coachnotes.repository.common.GroupId
import ru.hryasch.coachnotes.repository.common.PeopleChannelsStorage
import ru.hryasch.coachnotes.repository.common.PersonId
import ru.hryasch.coachnotes.repository.converters.fromDAO
import ru.hryasch.coachnotes.repository.converters.fromDao
import ru.hryasch.coachnotes.repository.converters.toDao
import ru.hryasch.coachnotes.repository.dao.GroupDAO
import ru.hryasch.coachnotes.repository.dao.PersonDAO
import java.util.*
import java.util.concurrent.Executors

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
        }
    }

    override suspend fun getPerson(personId: PersonId): Person?
    {
        var personDao: PersonDAO? = null
        val db = getDb()

        db.executeTransaction {
            val result = it.where<PersonDAO>()
                           .equalTo("id", personId)
                           .findFirst()
            result?.let {res ->
                personDao = it.copyFromRealm(res)
            }
        }

        return personDao?.fromDao()
    }

    override suspend fun getPeopleByGroup(groupId: GroupId): List<Person>?
    {
        var peopleDao: List<PersonDAO>? = null
        val db = getDb()

        db.executeTransaction {
            val result = it.where<PersonDAO>()
                .equalTo("groupId", groupId)
                .findAll()
            result?.let { res ->
                peopleDao = it.copyFromRealm(res)
            }
        }

        return peopleDao?.fromDAO()
    }

    override suspend fun getAllPeople(): List<Person>?
    {
        if (initializingJob.isActive) initializingJob.join()

        var peopleList: List<PersonDAO>? = null
        val db = getDb()

        db.executeTransaction {
            val result = it.where<PersonDAO>()
                           .findAll()
            result?.let { res ->
                peopleList = it.copyFromRealm(res)
            }
        }

        return peopleList?.fromDAO()
    }

    override suspend fun addOrUpdatePeople(person: Person)
    {
        GlobalScope.launch(Dispatchers.Main)
        {
            val db = getDb()
            db.executeTransaction {
                person.groupId?.let { groupId ->
                    val peopleByGroup = it.where<PersonDAO>()
                                          .equalTo("groupId", groupId)
                                          .findAll()
                    if (peopleByGroup == null)
                    {
                        setSpecificGroupPeopleTrigger(groupId)
                    }
                }

                val existPerson = it.where<PersonDAO>()
                                    .equalTo("id", person.id)
                                    .findFirst()

                if (existPerson == null)
                {
                    setSpecificPersonTrigger(person.id)
                }

                it.copyToRealmOrUpdate(person.toDao())
            }
        }.join()
    }

    override suspend fun deletePerson(person: Person)
    {
        val db = getDb()
        db.executeTransaction {
            val target = it.where<PersonDAO>()
                           .equalTo("id", person.id)
                           .findFirst()

            PeopleChannelsStorage.personById[person.id]!!.observable?.removeAllChangeListeners()
            PeopleChannelsStorage.personById[person.id]!!.observable == null
            target?.deleteFromRealm()
        }
    }

    override suspend fun closeDb()
    {
    }

    private suspend fun generatePersonDb()
    {
        val groupsList = groupRepo.getAllGroups()!!

        GlobalScope.launch(Dispatchers.Default)
        {
            val db = getDb()
            db.executeTransaction {
                it.deleteAll()
            }

            for (group in groupsList)
            {
                for (personId in group.membersList)
                {
                    val newPerson = PersonDAO(personId, faker.name().firstName(), faker.name().lastName(), "01/01/2014")
                        .apply {
                            groupId = group.id
                            isPaid = group.isPaid
                        }

                    d("Generated person: ${newPerson.name} ${newPerson.surname} (id: ${newPerson.id} groupId: ${newPerson.groupId})")

                    db.executeTransaction {
                        it.copyToRealm(newPerson)
                    }

                    setSpecificPersonTrigger(personId)
                }
                setSpecificGroupPeopleTrigger(group.id)
            }
            setAllPeopleChangesTrigger()
        }.join()
    }

    @ExperimentalCoroutinesApi
    private fun setSpecificPersonTrigger(personId: PersonId)
    {
        val broadcastChannel: ConflatedBroadcastChannel<Person> = get(named("sendSpecificPerson")) { parametersOf(personId) }

        GlobalScope.launch(Dispatchers.Main)
        {
            val db = getDb()
            val specificPerson = db.where<PersonDAO>()
                                   .equalTo("id", personId)
                                   .findFirst()

            specificPerson!!.removeAllChangeListeners()
            specificPerson.addChangeListener { t: PersonDAO, changeSet: ObjectChangeSet? ->
                GlobalScope.launch(Dispatchers.Main)
                {
                    e("channel <sendSpecificPerson[$personId]>: SEND1")
                    broadcastChannel.send(t.fromDao())
                }
            }

            PeopleChannelsStorage.personById[personId]!!.observable = specificPerson
        }
    }

    @ExperimentalCoroutinesApi
    private fun setSpecificGroupPeopleTrigger(groupId: GroupId)
    {
        val broadcastChannel: ConflatedBroadcastChannel<List<Person>> = get(named("sendPeopleByGroup")) { parametersOf(groupId) }

        GlobalScope.launch(Dispatchers.Main)
        {
            val db = getDb()
            val peopleBySpecificGroup = db.where<PersonDAO>().equalTo("groupId", groupId).findAll()

            peopleBySpecificGroup!!.removeAllChangeListeners()
            peopleBySpecificGroup.addChangeListener { elements ->
                GlobalScope.launch(Dispatchers.Main)
                {
                    e("channel <sendPeopleByGroup[$groupId]>: SEND1")
                    broadcastChannel.send(elements.fromDAO())
                }
            }

            PeopleChannelsStorage.groupPeopleByGroupId[groupId]!!.observable = peopleBySpecificGroup
        }
    }

    @ExperimentalCoroutinesApi
    private fun setAllPeopleChangesTrigger()
    {
        val broadcastChannel: ConflatedBroadcastChannel<List<Person>> = get(named("sendPeopleList"))

        GlobalScope.launch(Dispatchers.Main)
        {
            val db = getDb()
            val allPeople = db.where<PersonDAO>().findAll()

            allPeople.removeAllChangeListeners()
            allPeople.addChangeListener { elements ->
                val res = elements.fromDAO()
                GlobalScope.launch(Dispatchers.Main)
                {
                    e("channel <sendPeopleList>: SEND1")
                    broadcastChannel.send(res)
                }
            }

            PeopleChannelsStorage.allPeople.observable = allPeople
        }
    }

    private fun getDb(): Realm = Realm.getInstance(get(named("persons_mock"))).apply { this.refresh() }
}