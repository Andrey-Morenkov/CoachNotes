package ru.hryasch.coachnotes.repository.group

import com.pawegio.kandroid.e
import com.pawegio.kandroid.i
import com.pawegio.kandroid.w
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
import ru.hryasch.coachnotes.repository.converters.fromDao
import ru.hryasch.coachnotes.repository.converters.toDao
import ru.hryasch.coachnotes.repository.dao.DeletedGroupDAO
import ru.hryasch.coachnotes.repository.dao.DeletedPersonDAO
import ru.hryasch.coachnotes.repository.dao.GroupDAO
import java.util.LinkedList
import java.util.Locale
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

    override suspend fun getSimilarGroupIfExists(groupName: String): Group?
    {
        // standardize
        val targetGroupName = groupName.toLowerCase(Locale.getDefault())

        var group: Group? = null

        withContext(dbContext)
        {
            db.executeTransaction {
                val result = it.where<GroupDAO>()
                               .findAll()

                result?.forEach { grp ->
                    val existName = grp.name!!.toLowerCase(Locale.getDefault())
                    if (existName == targetGroupName)
                    {
                        group = it.copyFromRealm(grp).fromDAO()
                        return@executeTransaction
                    }
                }
            }
        }

        return group
    }

    override suspend fun getGroup(groupId: GroupId): Group?
    {
        var group: Group? = null

        withContext(dbContext)
        {
            db.executeTransaction {
                val result = it.where<GroupDAO>()
                               .equalTo("id", groupId)
                               .findFirst()

                result?.let { res ->
                    group = it.copyFromRealm(res).fromDAO()
                    return@executeTransaction
                }

                val result2 = it.where<DeletedGroupDAO>()
                                .equalTo("id", groupId)
                                .findFirst()

                result2?.let { res ->
                    group = it.copyFromRealm(res).fromDAO()
                }
            }
        }

        return group
    }

    override suspend fun getDeletedGroup(groupId: GroupId): Group?
    {
        var group: Group? = null

        withContext(dbContext)
        {
            db.executeTransaction {
                val result = it.where<DeletedGroupDAO>()
                               .equalTo("id", groupId)
                               .findFirst()

                result?.let { res ->
                    group = it.copyFromRealm(res).fromDAO()
                }
            }
        }

        return group
    }

    override suspend fun getGroups(groupsIds: List<GroupId>): List<Group>?
    {
        val groups: MutableList<Group> = LinkedList()

        for (groupId in groupsIds)
        {
            getGroup(groupId)?.let {
                groups.add(it)
            }
        }

        return groups
    }

    override suspend fun getAllGroups(): List<Group>?
    {
        if (initializingJob.isActive) initializingJob.join()

        val existingGroups = getAllExistingGroups()
        val deletedGroups = getAllDeletedGroups()

        val groupsList: MutableList<Group> = LinkedList()
        existingGroups?.let {
            groupsList.addAll(it)
        }
        deletedGroups?.let {
            groupsList.addAll(it)
        }

        if (groupsList.isEmpty())
        {
            return null
        }

        return groupsList
    }

    override suspend fun getAllExistingGroups(): List<Group>?
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

        return groupsList?.fromDAO()?.sorted()
    }

    override suspend fun getAllDeletedGroups(): List<Group>?
    {
        if (initializingJob.isActive) initializingJob.join()

        var groupsList: List<DeletedGroupDAO>? = null

        withContext(dbContext)
        {
            db.executeTransaction {
                val result = it.where<DeletedGroupDAO>()
                               .findAll()

                result?.let { res ->
                    groupsList = it.copyFromRealm(res)
                }
            }
        }

        return groupsList?.fromDAO()?.sorted()
    }

    override suspend fun getGroupsByScheduleDay(dayPosition0: Int): List<Group>?
    {
        var groupsList: List<GroupDAO>? = null

        withContext(dbContext)
        {
            db.executeTransaction {
                val result = it.where<GroupDAO>()
                               .contains("scheduleDaysCode0", dayPosition0.toString())
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

                var removedGroup: DeletedGroupDAO? = null
                if (existGroup == null) {
                    removedGroup = it.where<DeletedGroupDAO>()
                                     .equalTo("id", group.id)
                                     .findFirst()
                }

                isAddingGroup = ( existGroup == null && removedGroup == null )

                e("updating group: $group")

                it.copyToRealmOrUpdate(group.toDao()!!)
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
            GroupChannelsStorage.groupById[group.id]?.observable?.removeAllChangeListeners()
            GroupChannelsStorage.groupById[group.id]?.observable = null
        }

        withContext(dbContext)
        {
            db.executeTransaction {
                val target = it.where<GroupDAO>()
                               .equalTo("id", group.id)
                               .findFirst()

                target?.run {
                    val deletedGroup = it.copyFromRealm(this).delete()
                    it.copyToRealmOrUpdate(deletedGroup)

                    // FIX: because realm has strange behaviour if you next add group with same name and it will have previous members and schedule days
                    this.members.clear()
                    this.scheduleDays.clear()
                    it.copyToRealmOrUpdate(this)
                }
            }

            db.executeTransaction {
                val target = it.where<GroupDAO>()
                               .equalTo("id", group.id)
                               .findFirst()
                target?.deleteFromRealm()
            }
        }
    }

    override suspend fun deleteGroupPermanently(group: Group)
    {
        withContext(Dispatchers.Main)
        {
            GroupChannelsStorage.groupById[group.id]?.observable?.removeAllChangeListeners()
            GroupChannelsStorage.groupById[group.id]?.observable = null
        }

        var deletedGroupFound = false
        withContext(dbContext)
        {
            db.executeTransaction {
                val targetDeleted = it.where<DeletedGroupDAO>()
                                       .equalTo("id", group.id)
                                       .findFirst()

                targetDeleted?.run {
                    // FIX: because realm has strange behaviour if you next add group with same name and it will have previous members and schedule days
                    this.members.clear()
                    this.scheduleDays.clear()
                    it.copyToRealmOrUpdate(this)
                    deletedGroupFound = true
                }
            }

            if (deletedGroupFound)
            {
                db.executeTransaction {
                    val target = it.where<DeletedGroupDAO>()
                        .equalTo("id", group.id)
                        .findFirst()
                    target?.deleteFromRealm()
                }
                return@withContext
            }

            db.executeTransaction {
                val targetAlive = it.where<GroupDAO>()
                                    .equalTo("id", group.id)
                                    .findFirst()
                targetAlive?.run {
                    // FIX: because realm has strange behaviour if you next add group with same name and it will have previous members and schedule days
                    this.members.clear()
                    this.scheduleDays.clear()
                    it.copyToRealmOrUpdate(this)
                }
            }

            db.executeTransaction {
                val targetAlive = it.where<GroupDAO>()
                                    .equalTo("id", group.id)
                                    .findFirst()

                targetAlive?.deleteFromRealm()
            }
        }
    }

    override suspend fun reviveGroup(groupId: GroupId): Group?
    {
        var revivedGroup: Group? = null

        withContext(dbContext)
        {
            db.executeTransaction {
                val target = it.where<DeletedGroupDAO>()
                               .equalTo("id", groupId)
                               .findFirst()

                target?.run {
                    revivedGroup = it.copyFromRealm(this).revive().fromDAO()

                    // FIX: because realm has strange behaviour if you next add group with same name and it will have previous members and schedule days
                    this.members.clear()
                    this.scheduleDays.clear()
                    it.copyToRealmOrUpdate(this)
                }
            }

            if (revivedGroup != null)
            {
                db.executeTransaction {
                    val target = it.where<DeletedGroupDAO>()
                                   .equalTo("id", groupId)
                                   .findFirst()

                    target?.deleteFromRealm()
                }
            }
        }

        revivedGroup?.let {
            addOrUpdateGroup(it)
        }

        return revivedGroup
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