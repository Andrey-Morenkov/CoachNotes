package ru.hryasch.coachnotes.repository.person

import com.pawegio.kandroid.e
import io.realm.ObjectChangeSet
import io.realm.Realm
import io.realm.kotlin.where
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import org.koin.core.KoinComponent
import org.koin.core.get
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
import ru.hryasch.coachnotes.domain.common.GroupId
import ru.hryasch.coachnotes.domain.common.PersonId
import ru.hryasch.coachnotes.domain.person.data.Person
import ru.hryasch.coachnotes.domain.repository.PersonRepository
import ru.hryasch.coachnotes.repository.common.PeopleChannelsStorage
import ru.hryasch.coachnotes.repository.converters.fromDAO
import ru.hryasch.coachnotes.repository.converters.fromDao
import ru.hryasch.coachnotes.repository.converters.toDao
import ru.hryasch.coachnotes.repository.dao.PersonDAO
import java.util.concurrent.Executors

@ExperimentalCoroutinesApi
class PersonRepositoryImpl: PersonRepository, KoinComponent
{
    private lateinit var db: Realm
    private val dbContext = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    private val initializingJob: Job

    init
    {
        initializingJob = GlobalScope.launch(dbContext)
        {
            db = Realm.getInstance(get(named("persons")))
            initTriggers()
        }
    }

    override suspend fun getPerson(personId: PersonId): Person?
    {
        var personDao: PersonDAO? = null

        withContext(dbContext)
        {
            db.executeTransaction {
                val result = it.where<PersonDAO>()
                               .equalTo("id", personId)
                               .findFirst()

                result?.let {res ->
                    personDao = it.copyFromRealm(res)
                }
            }
        }

        return personDao?.fromDao()
    }

    override suspend fun getPersonsByGroup(groupId: GroupId): List<Person>?
    {
        var peopleDao: List<PersonDAO>? = null

        withContext(dbContext)
        {
            db.executeTransaction {
                val result = it.where<PersonDAO>()
                               .equalTo("groupId", groupId)
                               .findAll()

                result?.let { res ->
                    peopleDao = it.copyFromRealm(res)
                }
            }
        }

        return peopleDao?.fromDAO()
    }

    override suspend fun getAllPeople(): List<Person>?
    {
        if (initializingJob.isActive) initializingJob.join()

        var peopleList: List<PersonDAO>? = null

        withContext(dbContext)
        {
            db.executeTransaction {
                val result = it.where<PersonDAO>()
                               .findAll()

                result?.let { res ->
                    peopleList = it.copyFromRealm(res)
                }
            }
        }

        return peopleList?.fromDAO()
    }

    @ExperimentalCoroutinesApi
    override suspend fun addOrUpdatePerson(person: Person)
    {
        withContext(dbContext)
        {
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

                it.copyToRealmOrUpdate(person.toDao())

                if (existPerson == null)
                {
                    setSpecificPersonTrigger(person.id)
                }
            }
        }
    }

    override suspend fun deletePerson(person: Person)
    {
        withContext(Dispatchers.Main)
        {
            PeopleChannelsStorage.personById[person.id]!!.observable?.removeAllChangeListeners()
            PeopleChannelsStorage.personById[person.id]!!.observable == null
        }
        withContext(dbContext)
        {
            db.executeTransaction {
                val target = it.where<PersonDAO>()
                               .equalTo("id", person.id)
                               .findFirst()

                target?.deleteFromRealm()
            }
        }
    }

    override suspend fun closeDb()
    {
        withContext(dbContext)
        {
            db.close()
        }
        withContext(Dispatchers.Main)
        {
            PeopleChannelsStorage.allPeople.mainDbEntity?.close()
            PeopleChannelsStorage.groupPeopleByGroupId.values.forEach {
                it.mainDbEntity?.close()
            }
            PeopleChannelsStorage.personById.values.forEach {
                it.mainDbEntity?.close()
            }
        }
    }


    private suspend fun initTriggers()
    {
        val allPeople = db.where<PersonDAO>().findAll()
        allPeople.forEach {
            setSpecificPersonTrigger(it.fromDao().id)
            if (it.groupId != null)
            {
                setSpecificGroupPeopleTrigger(it.fromDao().groupId!!)
            }
        }

        setAllPeopleChangesTrigger()
    }

    @ExperimentalCoroutinesApi
    private fun setSpecificPersonTrigger(personId: PersonId)
    {
        val broadcastChannel: ConflatedBroadcastChannel<Person> = get(named("sendSpecificPerson")) { parametersOf(personId) }

        GlobalScope.launch(Dispatchers.Main)
        {
            if (PeopleChannelsStorage.personById[personId]!!.mainDbEntity == null)
            {
                PeopleChannelsStorage.personById[personId]!!.mainDbEntity = getDb()
            }
            val db1 = PeopleChannelsStorage.personById[personId]!!.mainDbEntity
            db1!!.refresh()

            val specificPerson = db1.where<PersonDAO>()
                                    .equalTo("id", personId)
                                    .findFirst()

            specificPerson!!.removeAllChangeListeners()
            specificPerson.addChangeListener { t: PersonDAO, _: ObjectChangeSet? ->
                GlobalScope.launch(Dispatchers.Main)
                {
                    e("channel <SpecificPerson[$personId]>: SEND")
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
            if (PeopleChannelsStorage.groupPeopleByGroupId[groupId]!!.mainDbEntity == null)
            {
                PeopleChannelsStorage.groupPeopleByGroupId[groupId]!!.mainDbEntity = getDb()
            }
            val db1 = PeopleChannelsStorage.groupPeopleByGroupId[groupId]!!.mainDbEntity
            db1!!.refresh()

            val peopleBySpecificGroup = db1.where<PersonDAO>()
                                           .equalTo("groupId", groupId)
                                           .findAll()

            peopleBySpecificGroup!!.removeAllChangeListeners()
            peopleBySpecificGroup.addChangeListener { elements ->
                GlobalScope.launch(Dispatchers.Main)
                {
                    e("channel <SpecificGroupAllPeople[$groupId]>: SEND")
                    broadcastChannel.send(elements.fromDAO())
                }
            }

            PeopleChannelsStorage.groupPeopleByGroupId[groupId]!!.observable = peopleBySpecificGroup
        }
    }

    @ExperimentalCoroutinesApi
    private suspend fun setAllPeopleChangesTrigger()
    {
        val broadcastChannel: ConflatedBroadcastChannel<List<Person>> = get(named("sendPeopleList"))

        GlobalScope.launch(Dispatchers.Main)
        {
            if (PeopleChannelsStorage.allPeople.mainDbEntity == null)
            {
                PeopleChannelsStorage.allPeople.mainDbEntity = getDb()
            }
            val db1 = PeopleChannelsStorage.allPeople.mainDbEntity
            db1!!.refresh()

            val allPeople = db1.where<PersonDAO>()
                               .findAll()

            allPeople.removeAllChangeListeners()
            allPeople.addChangeListener { elements ->
                val res = elements.fromDAO()
                GlobalScope.launch(Dispatchers.Main)
                {
                    e("channel <AllPeople>: SEND")
                    broadcastChannel.send(res)
                }
            }

            PeopleChannelsStorage.allPeople.observable = allPeople
        }
    }

    private fun getDb(): Realm = Realm.getInstance(get(named("persons")))
}