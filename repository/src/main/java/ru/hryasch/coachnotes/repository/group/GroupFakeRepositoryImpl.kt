package ru.hryasch.coachnotes.repository.group

import com.pawegio.kandroid.d
import com.pawegio.kandroid.i
import io.realm.Realm
import io.realm.kotlin.where
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import org.koin.core.KoinComponent
import org.koin.core.get
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
import ru.hryasch.coachnotes.domain.group.data.Group
import ru.hryasch.coachnotes.domain.person.data.Person
import ru.hryasch.coachnotes.domain.repository.GroupRepository
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
            setChangesTriggers()
        }
    }


    override suspend fun getGroup(groupId: GroupId): Group?
    {
        val db = getDb()
        db.refresh()

        val group = db.where<GroupDAO>()
                      .equalTo("id", groupId)
                      .findFirst()

        return group?.fromDAO()
    }

    override suspend fun getAllGroups(): List<Group>?
    {
        if (initializingJob.isActive) initializingJob.join()

        val db = getDb()
        db.refresh()

        val groupsList = db.where<GroupDAO>().findAll()
        return if (groupsList.isEmpty())
        {
            null
        }
        else
        {
            groupsList.fromDAO()
        }
    }

    override suspend fun addOrUpdateGroup(group: Group)
    {
        val db = getDb()
        db.refresh()

        db.executeTransaction {
            it.copyToRealmOrUpdate(group.toDao())
        }

        val groups = db.where<GroupDAO>().findAll()
        groups.forEach {
            i("group after update: ${it.fromDAO()}")
        }

        val specificBroadcastChannel: ConflatedBroadcastChannel<Group> = get(named("sendSpecificGroup")) { parametersOf(group.id) }
        val groupDb = db.where<GroupDAO>().equalTo("id", group.id).findFirst()
        groupDb?.let {
            i("channel <sendSpecificGroup[${group.id}]>: SEND")
            specificBroadcastChannel.send(it.fromDAO())
        }

        val broadcastChannel: ConflatedBroadcastChannel<List<Group>> = get(named("sendGroupsList"))
        val groupsDb = db.where<GroupDAO>().findAll()
        groupsDb?.let {
            i("channel <sendGroupsList>: SEND")
            broadcastChannel.send(it.fromDAO())
        }
    }

    override suspend fun deleteGroup(group: Group)
    {
        val db = getDb()
        db.refresh()

        val target = db.where<GroupDAO>()
                       .equalTo("id", group.id)
                       .findAll()

        db.executeTransaction {
            target.deleteAllFromRealm()
        }

        val broadcastChannel: ConflatedBroadcastChannel<List<Group>> = get(named("sendGroupsList"))
        val allGroups = db.where<GroupDAO>().findAll()

        i("channel <sendGroupsList>: SEND")
        broadcastChannel.send(allGroups.fromDAO())
    }

    private suspend fun generateGroupDb()
    {
        val db = getDb()
        db.refresh()

        db.executeTransaction {
            it.deleteAll()
        }

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
    }

    @ExperimentalCoroutinesApi
    private suspend fun setChangesTriggers()
    {
        setAllGroupsChangesTrigger()
    }

    @ExperimentalCoroutinesApi
    private suspend fun setAllGroupsChangesTrigger()
    {
        val broadcastChannel: ConflatedBroadcastChannel<List<Group>> = get(named("sendGroupsList"))

        withContext(Dispatchers.Main)
        {
            val db = getDb()
            db.refresh()

            val allGroups = db.where<GroupDAO>().findAll()
            allGroups.addChangeListener { t, _ ->
                i("channel <sendGroupsList>: add change listener")
                GlobalScope.launch(Dispatchers.Main)
                {
                    i("channel <sendGroupsList>: SEND")
                    broadcastChannel.send(t.fromDAO())
                }
            }
        }
    }

    private fun getDb(): Realm = Realm.getInstance(get(named("groups_mock")))
}