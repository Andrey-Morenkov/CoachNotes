package ru.hryasch.coachnotes.repository.group

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
import ru.hryasch.coachnotes.domain.common.GroupId
import ru.hryasch.coachnotes.domain.group.data.Group
import ru.hryasch.coachnotes.domain.person.data.Person
import ru.hryasch.coachnotes.domain.repository.GroupRepository
import ru.hryasch.coachnotes.repository.common.GroupChannelsStorage
import ru.hryasch.coachnotes.repository.converters.fromDAO
import ru.hryasch.coachnotes.repository.converters.toDao
import ru.hryasch.coachnotes.repository.dao.GroupDAO
import java.util.concurrent.Executors


@ExperimentalCoroutinesApi
class GroupRepositoryImpl: GroupRepository, KoinComponent
{
    private lateinit var db: Realm
    private val dbContext = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    private val initializingJob: Job

    init
    {
        initializingJob = GlobalScope.launch(dbContext)
        {
            db = Realm.getInstance(get(named("groups")))
            initTriggers()
        }
    }

    override suspend fun getGroup(groupId: GroupId): Group?
    {
        var groupDAO: GroupDAO? = null

        withContext(dbContext)
        {
            db.executeTransaction {
                val result = it.where<GroupDAO>()
                               .equalTo("id", groupId)
                               .findFirst()

                result?.let { res ->
                    groupDAO = it.copyFromRealm(res)
                }
            }
        }

        return groupDAO?.fromDAO()
    }

    override suspend fun getAllGroups(): List<Group>?
    {
        if (initializingJob.isActive) initializingJob.join()

        var groupsList: List<GroupDAO>? = null

        withContext(dbContext)
        {
            db.executeTransaction {
                val result = it.where<GroupDAO>()
                               .findAll()

                result?.let { res ->
                    groupsList = it.copyFromRealm(res)
                }
            }
        }

        return groupsList?.fromDAO()
    }

    @ExperimentalCoroutinesApi
    override suspend fun addOrUpdateGroup(group: Group)
    {
        withContext(dbContext)
        {
            var isAddingGroup = false

            db.executeTransaction {
                val existGroup = it.where<GroupDAO>()
                                   .equalTo("id", group.id)
                                   .findFirst()

                isAddingGroup = ( existGroup == null )

                it.copyToRealmOrUpdate(group.toDao())
            }

            if (isAddingGroup)
            {
                setSpecificGroupTrigger(group.id)
            }
        }
    }

    override suspend fun deleteGroup(group: Group)
    {
        withContext(Dispatchers.Main)
        {
            GroupChannelsStorage.groupById[group.id]!!.observable?.removeAllChangeListeners()
            GroupChannelsStorage.groupById[group.id]!!.observable = null
        }
        withContext(dbContext)
        {
            db.executeTransaction {
                val target = it.where<GroupDAO>()
                               .equalTo("id", group.id)
                               .findFirst()

                target?.deleteFromRealm()
            }
        }
    }

    override suspend fun updatePeopleGroupAffiliation(people: List<Person>)
    {
        // person already have new group here
        withContext(dbContext)
        {
            db.executeTransaction {
                val allGroups = it.where<GroupDAO>()
                                  .findAll()

                allGroups.forEach { group ->
                    people.forEach prs@ { person ->
                        val isExistPerson = group.members.find { personId -> personId == person.id } != null
                        val isShouldItBe  = person.groupId == group.id

                        if (isExistPerson && !isShouldItBe)
                        {
                            group.members.remove(person.id)
                            i("removed person $person from group ${group.id}")
                            return@prs
                        }

                        if (!isExistPerson && isShouldItBe)
                        {
                            group.members.add(person.id)
                            i("added person $person to group ${group.id}")
                        }
                    }
                }
            } // transaction
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
            GroupChannelsStorage.allGroups.mainDbEntity?.close()
            GroupChannelsStorage.groupById.values.forEach {
                it.mainDbEntity?.close()
            }
        }
    }

    private suspend fun initTriggers()
    {
        val allGroups = db.where<GroupDAO>().findAll()
        allGroups.forEach {
            setSpecificGroupTrigger(it.fromDAO().id)
        }

        setAllGroupsTrigger()
    }

    @ExperimentalCoroutinesApi
    private suspend fun setAllGroupsTrigger()
    {
        val broadcastChannel: ConflatedBroadcastChannel<List<Group>> = get(named("sendGroupsList"))

        GlobalScope.launch(Dispatchers.Main)
        {
            if (GroupChannelsStorage.allGroups.mainDbEntity == null)
            {
                GroupChannelsStorage.allGroups.mainDbEntity = getDb()
            }
            val db1 = GroupChannelsStorage.allGroups.mainDbEntity
            db1!!.refresh()

            val allGroups = db1.where<GroupDAO>()
                               .findAll()

            allGroups.removeAllChangeListeners()
            allGroups.addChangeListener { elements ->
                GlobalScope.launch(Dispatchers.Main)
                {
                    e("channel <AllGroups>: SEND")
                    broadcastChannel.send(elements.fromDAO())
                }
            }

            GroupChannelsStorage.allGroups.observable = allGroups
        }
    }

    @ExperimentalCoroutinesApi
    private fun setSpecificGroupTrigger(groupId: GroupId)
    {
        val broadcastChannel: ConflatedBroadcastChannel<Group> = get(named("sendSpecificGroup")) { parametersOf(groupId) }

        GlobalScope.launch(Dispatchers.Main)
        {
            if (GroupChannelsStorage.groupById[groupId]!!.mainDbEntity == null)
            {
                GroupChannelsStorage.groupById[groupId]!!.mainDbEntity = getDb()
            }
            val db1 = GroupChannelsStorage.groupById[groupId]!!.mainDbEntity
            db1!!.refresh()

            val specificGroup = db1.where<GroupDAO>()
                                   .equalTo("id", groupId)
                                   .findFirst()

            specificGroup!!.removeAllChangeListeners()
            specificGroup.addChangeListener { t: GroupDAO, _: ObjectChangeSet? ->
                GlobalScope.launch(Dispatchers.Main)
                {
                    e("channel <Group[$groupId]>: SEND")
                    broadcastChannel.send(t.fromDAO())
                }
            }

            GroupChannelsStorage.groupById[groupId]!!.observable = specificGroup
        }
    }

    private fun getDb(): Realm = Realm.getInstance(get(named("groups")))
}