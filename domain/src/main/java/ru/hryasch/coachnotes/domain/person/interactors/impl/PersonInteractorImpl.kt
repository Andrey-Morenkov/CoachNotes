package ru.hryasch.coachnotes.domain.person.interactors.impl

import com.pawegio.kandroid.e
import com.pawegio.kandroid.i
import com.pawegio.kandroid.w
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.koin.core.qualifier.named
import ru.hryasch.coachnotes.domain.common.GroupId
import ru.hryasch.coachnotes.domain.common.PersonId
import ru.hryasch.coachnotes.domain.person.data.Person
import ru.hryasch.coachnotes.domain.person.interactors.PersonInteractor
import ru.hryasch.coachnotes.domain.person.interactors.SimilarPersonFoundException
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
        return peopleRepository.getAllExistingPeople() ?: LinkedList()
    }

    override suspend fun getGroupNames(): Map<GroupId, String>
    {
        val groupsList = groupRepository.getAllExistingGroups()
        val res: MutableMap<GroupId, String> = HashMap()
        groupsList?.forEach {
            res[it.id] = it.name
        }
        return res
    }

    override suspend fun getPeopleWithoutGroup(): List<Person>?
    {
        return peopleRepository.getAllExistingPeople()?.filter { person -> person.groupId == null }
    }



    @ExperimentalCoroutinesApi
    override suspend fun addOrUpdatePeople(people: List<Person>)
    {
        groupRepository.updatePeopleGroupAffiliation(people)
        peopleRepository.addOrUpdatePeople(people)
    }

    @ExperimentalCoroutinesApi
    override suspend fun addOrUpdatePerson(person: Person)
    {
        val similarPerson = peopleRepository.getSimilarPersonIfExists(person.surname)
        if (similarPerson != null)
        {
            w("found similar person: $similarPerson")
            throw SimilarPersonFoundException(similarPerson)
        }

        addOrUpdatePersonForced(person)
    }

    @ExperimentalCoroutinesApi
    override suspend fun addOrUpdatePersonForced(person: Person)
    {
        i("addOrUpdatePersonForced: $person")
        addOrUpdatePeople(listOf(person))
    }



    override suspend fun deletePerson(person: Person)
    {
        if (person.deletedTimestamp != null)
        {
            w("Try delete person ${person.id}, but it already deleted, skip")
            return
        }

        person.groupId?.let {
            val group = groupRepository.getGroup(it)
            if (group != null)
            {
                group.membersList.remove(person.id)
                i("membersList size after remove = ${group.membersList.size}")
                groupRepository.addOrUpdateGroup(group)
            }
        }
        peopleRepository.deletePerson(person.id)
    }

    override suspend fun deletePersonPermanently(person: Person)
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
        peopleRepository.deletePersonPermanently(person.id)
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