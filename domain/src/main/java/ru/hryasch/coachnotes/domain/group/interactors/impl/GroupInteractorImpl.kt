package ru.hryasch.coachnotes.domain.group.interactors.impl

import com.pawegio.kandroid.i
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.koin.core.qualifier.named
import ru.hryasch.coachnotes.domain.common.GroupId
import ru.hryasch.coachnotes.domain.repository.GroupRepository
import ru.hryasch.coachnotes.domain.group.data.Group
import ru.hryasch.coachnotes.domain.group.interactors.GroupInteractor
import ru.hryasch.coachnotes.domain.person.data.Person
import ru.hryasch.coachnotes.domain.repository.JournalRepository
import ru.hryasch.coachnotes.domain.repository.PersonRepository
import java.util.*

class GroupInteractorImpl: GroupInteractor, KoinComponent
{
    private val groupRepository: GroupRepository by inject(named("release"))
    private val peopleRepository: PersonRepository by inject(named("release"))
    private val journalRepository: JournalRepository by inject(named("release"))

    override suspend fun getGroupsList(): List<Group>
    {
        val groups = groupRepository.getAllGroups() ?: LinkedList()

        groups.forEach {
            i("group[${it.name} ${it.id}]: people count = ${it.membersList.size}")
        }

        return groups
    }

    override suspend fun getMaxGroupId(): GroupId
    {
        val maxId = groupRepository.getAllGroups()?.map { it.id }?.max()
        return maxId ?: 0
    }

    override suspend fun getGroupNames(): Map<GroupId, String>
    {
        val groupsList = groupRepository.getAllGroups()
        val res: MutableMap<GroupId, String> = HashMap()
        groupsList?.forEach {
            res[it.id] = it.name
        }
        return res
    }

    override suspend fun getPeopleListByGroup(groupId: GroupId): List<Person>
    {
        return peopleRepository.getPeopleByGroup(groupId) ?: LinkedList<Person>()
    }

    override suspend fun addOrUpdateGroup(group: Group)
    {
        groupRepository.addOrUpdateGroup(group)
    }

    override suspend fun deleteGroup(group: Group)
    {
        group.membersList.forEach {
            val person = peopleRepository.getPerson(it)
            person?.apply {
                    groupId = null
                    isPaid = false }
                  ?.let   { peopleRepository.addOrUpdatePeople(it) }
        }

        groupRepository.deleteGroup(group)
        journalRepository.deleteAllJournalsByGroup(group.id)
    }
}