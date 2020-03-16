package ru.hryasch.coachnotes.repository.group

import com.pawegio.kandroid.d
import io.realm.Realm
import io.realm.kotlin.where
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.core.KoinComponent
import org.koin.core.get
import org.koin.core.qualifier.named
import ru.hryasch.coachnotes.domain.group.data.Group
import ru.hryasch.coachnotes.domain.repository.GroupRepository
import ru.hryasch.coachnotes.repository.common.GroupId
import ru.hryasch.coachnotes.repository.converters.fromDAO
import ru.hryasch.coachnotes.repository.dao.GroupDAO
import kotlin.random.Random

class GroupFakeRepositoryImpl: GroupRepository, KoinComponent
{
    private var initializingJob: Job

    init
    {
        d("GroupRepo INIT START")
        initializingJob = GlobalScope.launch(Dispatchers.Default)
        {
            generateGroupDb()
            d("GroupRepo INIT FINISH")
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
        if (initializingJob.isActive) { initializingJob.join() }
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

    private suspend fun generateGroupDb()
    {
        val db = getDb()

        val group = GroupDAO(1, "платники", 6)

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

    private fun getDb(): Realm = Realm.getInstance(get(named("groups_mock")))
}