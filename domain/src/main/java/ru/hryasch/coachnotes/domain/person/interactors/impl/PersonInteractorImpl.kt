package ru.hryasch.coachnotes.domain.person.interactors.impl

import com.pawegio.kandroid.e
import com.pawegio.kandroid.i
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.koin.core.KoinComponent
import org.koin.core.inject
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
    private val peopleRepository: PersonRepository by inject(named("release"))
    private val groupRepository: GroupRepository by inject(named("release"))

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

    override suspend fun getPeopleWithoutGroup(): List<Person>?
    {
        return peopleRepository.getAllPeople()?.filter { person -> person.groupId == null }
    }

    override suspend fun getMaxPersonId(): PersonId
    {
        val maxId = peopleRepository.getAllPeople()?.map { it.id }?.max()
        return maxId ?: 0
    }



    @ExperimentalCoroutinesApi
    override suspend fun addOrUpdatePeople(people: List<Person>)
    {
        groupRepository.updatePeopleGroupAffiliation(people)

        people.forEach {
            peopleRepository.addOrUpdatePeople(it)
        }
    }

    @ExperimentalCoroutinesApi
    override suspend fun addOrUpdatePerson(person: Person)
    {
        addOrUpdatePeople(listOf(person))
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
        var person = peopleRepository.getPeopleByGroup(groupId)?.find { prsn -> prsn.id == personId }
        if (person == null)
        {
            e("can't find person $personId in group $groupId, try to find globally")
            person = peopleRepository.getAllPeople()?.find { prsn -> prsn.id == personId }
            if (person == null)
            {
                e("can't find person $personId globally")
                return
            }
        }

        person.groupId = null
        person.isPaid = false

        addOrUpdatePeople(listOf(person))
    }
}