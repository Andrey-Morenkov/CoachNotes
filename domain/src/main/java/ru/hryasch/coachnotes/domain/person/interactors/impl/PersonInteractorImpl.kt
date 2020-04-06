package ru.hryasch.coachnotes.domain.person.interactors.impl

import com.pawegio.kandroid.i
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import org.koin.core.KoinComponent
import org.koin.core.get
import org.koin.core.inject
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
import ru.hryasch.coachnotes.domain.common.GroupId
import ru.hryasch.coachnotes.domain.common.PersonId
import ru.hryasch.coachnotes.domain.person.data.Person
import ru.hryasch.coachnotes.domain.person.interactors.PersonInteractor
import ru.hryasch.coachnotes.domain.repository.GroupRepository
import ru.hryasch.coachnotes.domain.repository.PersonRepository
import java.util.*
import kotlin.collections.HashMap

class PersonInteractorImpl: PersonInteractor, KoinComponent
{
    private val peopleRepository: PersonRepository by inject(named("mock"))
    private val groupRepository: GroupRepository by inject(named("mock"))

    override suspend fun getPeopleList(): List<Person>
    {
        return peopleRepository.getAllPeople() ?: LinkedList()
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

    @ExperimentalCoroutinesApi
    override suspend fun addOrUpdatePerson(person: Person)
    {
        person.groupId?.let {
            val group = groupRepository.getGroup(it)
            if (group != null)
            {
                val isExisted = group.membersList.find { personId -> personId == person.id }
                if (isExisted == null)
                {
                    group.membersList.add(person.id)
                    i("added person $person to group ${group.id}")
                    groupRepository.addOrUpdateGroup(group)
                }
            }
        }

        //hotfix for delete person
        val groups = groupRepository.getAllGroups()
        groups?.forEach {
            if (it.id != person.groupId)
            {
                val isExisted = it.membersList.find { personId -> personId == person.id }
                if (isExisted != null)
                {
                    it.membersList.remove(person.id)
                    groupRepository.addOrUpdateGroup(it)

                    //hotfix
                    val broadcast: ConflatedBroadcastChannel<List<Person>> = get(named("sendPeopleByGroup")) { parametersOf(it.id) }
                    i("channel <sendPeopleByGroup[${it.id}]>: SEND")
                    broadcast.send(peopleRepository.getPersonsByGroup(it.id)!!)
                }
            }
        }

        peopleRepository.addOrUpdatePerson(person)
    }

    override suspend fun getMaxPersonId(): PersonId
    {
        val maxId = peopleRepository.getAllPeople()?.map { it.id }?.max()
        return maxId ?: 0
    }

    override suspend fun deletePerson(person: Person)
    {
        person.groupId?.let {
            val group = groupRepository.getGroup(it)
            if (group != null)
            {
                group.membersList.remove(person.id)
                i("membersList size after remove = ${group.membersList.size}")
                groupRepository.addOrUpdateGroup(group)
            }
        }
        peopleRepository.deletePerson(person)
    }

    @ExperimentalCoroutinesApi
    override suspend fun deletePersonFromGroup(personId: PersonId, groupId: GroupId)
    {
        val person = peopleRepository.getPersonsByGroup(groupId)?.find { person -> person.id == personId }

        person?.let {
            it.groupId = null
            it.isPaid = false
            addOrUpdatePerson(it)
        }
    }
}