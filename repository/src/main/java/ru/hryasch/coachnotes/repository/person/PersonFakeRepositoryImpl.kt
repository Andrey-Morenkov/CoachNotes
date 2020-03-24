package ru.hryasch.coachnotes.repository.person

import com.github.javafaker.Faker
import com.pawegio.kandroid.d
import io.realm.Realm
import io.realm.kotlin.where
import kotlinx.coroutines.*
import org.koin.core.KoinComponent
import org.koin.core.get
import org.koin.core.inject
import org.koin.core.qualifier.named
import ru.hryasch.coachnotes.domain.person.Person
import ru.hryasch.coachnotes.domain.repository.GroupRepository
import ru.hryasch.coachnotes.domain.repository.PersonRepository
import ru.hryasch.coachnotes.repository.common.GroupId
import ru.hryasch.coachnotes.repository.common.PersonId
import ru.hryasch.coachnotes.repository.converters.fromDAO
import ru.hryasch.coachnotes.repository.dao.PersonDAO
import java.util.*

class PersonFakeRepositoryImpl: PersonRepository, KoinComponent
{
    private val faker: Faker = Faker(Locale("ru"))
    private val groupRepo: GroupRepository by inject(named("mock"))
    private var initializingJob: Job

    init
    {
        d("PersonFakeRepositoryImpl INIT START")
        initializingJob = GlobalScope.launch(Dispatchers.Default)
        {
            generatePersonDb()
            d("PersonFakeRepositoryImpl INIT FINISH")
        }
    }

    private suspend fun generatePersonDb()
    {
        for (group in groupRepo.getAllGroups()!!)
        {
            for (personId in group.membersList)
            {

                val newPerson = PersonDAO(personId, group.id, faker.name().firstName(), faker.name().lastName())
                d("Generated person: ${newPerson.name} ${newPerson.surname} (id: ${newPerson.id} / groupId: ${newPerson.groupId})")

                getDb().executeTransaction {
                    it.copyToRealm(newPerson)
                }
            }
        }
    }

    override suspend fun getPerson(personId: PersonId): Person?
    {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun getPersonsByGroup(groupId: GroupId): List<Person>?
    {
        if (initializingJob.isActive) { initializingJob.join() }
        val db = getDb()
        db.refresh()

        val personsList: MutableList<Person> = LinkedList()
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
        if (initializingJob.isActive) { initializingJob.join() }

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

    private fun getDb(): Realm = Realm.getInstance(get(named("persons_mock")))
}