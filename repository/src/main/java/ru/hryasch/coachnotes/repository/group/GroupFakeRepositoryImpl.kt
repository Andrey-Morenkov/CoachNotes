package ru.hryasch.coachnotes.repository.group

import com.pawegio.kandroid.d
import com.pawegio.kandroid.e
import com.pawegio.kandroid.i
import io.realm.ObjectChangeSet
import io.realm.Realm
import io.realm.kotlin.where
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import org.koin.core.KoinComponent
import org.koin.core.get
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
import ru.hryasch.coachnotes.domain.common.PersonId
import ru.hryasch.coachnotes.domain.group.data.Group
import ru.hryasch.coachnotes.domain.person.data.Person
import ru.hryasch.coachnotes.domain.repository.GroupRepository
import ru.hryasch.coachnotes.repository.common.GroupChannelsStorage
import ru.hryasch.coachnotes.repository.common.GroupId
import ru.hryasch.coachnotes.repository.common.toAbsolute
import ru.hryasch.coachnotes.repository.converters.fromDAO
import ru.hryasch.coachnotes.repository.converters.toDao
import ru.hryasch.coachnotes.repository.dao.GroupDAO
import kotlin.random.Random

@ExperimentalCoroutinesApi
class GroupFakeRepositoryImpl: GroupRepository, KoinComponent
{
    private val initializingJob: Job

    init
    {
        initializingJob = GlobalScope.launch(Dispatchers.Default)
        {
            generateGroupDb()
        }
    }


    override suspend fun getGroup(groupId: GroupId): Group?
    {
        var groupDAO: GroupDAO? = null
        val db = getDb()

        db.executeTransaction {
            val result = it.where<GroupDAO>()
                           .equalTo("id", groupId)
                           .findFirst()
            result?.let { res ->
                groupDAO = it.copyFromRealm(res)
            }
        }

        return groupDAO?.fromDAO()
    }

    override suspend fun getAllGroups(): List<Group>?
    {
        if (initializingJob.isActive) initializingJob.join()

        var groupsList: List<GroupDAO>? = null
        val db = getDb()

        db.executeTransaction {
            val result = db.where<GroupDAO>()
                           .findAll()
            result?.let { res ->
                groupsList = it.copyFromRealm(res)
            }
        }

        return groupsList?.fromDAO()
    }

    override suspend fun addOrUpdateGroup(group: Group)
    {
        GlobalScope.launch(Dispatchers.Main)
        {
            val db = getDb()
            db.executeTransaction {
                val existGroup = it.where<GroupDAO>()
                                   .equalTo("id", group.id)
                                   .findFirst()
                if (existGroup == null)
                {
                    setSpecificGroupTrigger(group.id)
                }

                it.copyToRealmOrUpdate(group.toDao())
            }

        }.join()
    }

    override suspend fun deleteGroup(group: Group)
    {
        val db = getDb()
        db.executeTransaction {
            val target = db.where<GroupDAO>()
                           .equalTo("id", group.id)
                           .findFirst()

            GroupChannelsStorage.groupById[group.id]!!.observable?.removeAllChangeListeners()
            GroupChannelsStorage.groupById[group.id]!!.observable = null
            target?.deleteFromRealm()
        }
    }

    override suspend fun updatePeopleGroupInformation(person: Person)
    {
        val db = getDb()
        db.executeTransaction {
            val result = it.where<GroupDAO>().findAll()
            result.forEach {
                if (it.fromDAO().id != person.groupId)
                {
                    if (it.members.remove(person.id))
                    {
                        i("removed person $person from group ${it.id}")
                    }
                }
            }
        }
    }

    override suspend fun closeDb()
    {
    }

    private suspend fun generateGroupDb()
    {
        GlobalScope.launch(Dispatchers.Default)
        {
            val db = getDb()

            val group = GroupDAO(1, "Платники", true, 6.toAbsolute())
            repeat(20)
            {
                var newPersonId: Int

                do
                {
                    newPersonId = Random.nextInt(1, 100)
                }
                while (group.members.find { it == newPersonId } != null)

                group.members.add(newPersonId)
                d("generated person ID: $newPersonId")
            }

            db.executeTransaction {
                it.copyToRealm(group)
            }

            setSpecificGroupTrigger(group.id!!)
            setAllGroupsTrigger()

        }.join()
    }

    @ExperimentalCoroutinesApi
    private suspend fun setAllGroupsTrigger()
    {
        val broadcastChannel: ConflatedBroadcastChannel<List<Group>> = get(named("sendGroupsList"))

        GlobalScope.launch(Dispatchers.Main)
        {
            val db = getDb()
            db.refresh()

            val allGroups = db.where<GroupDAO>().findAll()
            allGroups.removeAllChangeListeners()
            allGroups.addChangeListener { elements ->
                GlobalScope.launch(Dispatchers.Main)
                {
                    e("channel <sendGroupsList>: SEND1")
                    broadcastChannel.send(elements.fromDAO())
                }
            }

            GroupChannelsStorage.allGroups.observable = allGroups
        }
    }

    @ExperimentalCoroutinesApi
    private fun setSpecificGroupTrigger(groupId: GroupId)
    {
        val broadcastChannel: ConflatedBroadcastChannel<Group> = get(named("sendSpecificGroup")) { parametersOf(groupId)}

        GlobalScope.launch(Dispatchers.Main)
        {
            val db = getDb()
            db.refresh()

            val specificGroup = db.where<GroupDAO>().equalTo("id", groupId).findFirst()

            specificGroup!!.removeAllChangeListeners()
            specificGroup.addChangeListener { t: GroupDAO, changeSet: ObjectChangeSet? ->
                GlobalScope.launch(Dispatchers.Main)
                {
                    e("channel <sendSpecificGroup[$groupId]>: SEND1")
                    broadcastChannel.send(t.fromDAO())
                }
            }

            GroupChannelsStorage.groupById[groupId]!!.observable = specificGroup
        }
    }

    private fun getDb(): Realm = Realm.getInstance(get(named("groups_mock"))).apply { this.refresh() }
}