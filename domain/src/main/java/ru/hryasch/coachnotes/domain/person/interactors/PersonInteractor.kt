package ru.hryasch.coachnotes.domain.person.interactors

import ru.hryasch.coachnotes.domain.common.GroupId
import ru.hryasch.coachnotes.domain.common.PersonId
import ru.hryasch.coachnotes.domain.person.data.Person
import java.lang.Exception

interface PersonInteractor
{
    suspend fun getPeopleList(): List<Person>

    suspend fun getGroupNames(): Map<GroupId, String>

    suspend fun getPeopleWithoutGroup(): List<Person>?



    suspend fun addOrUpdatePeople(people: List<Person>)

    suspend fun addOrUpdatePerson(person: Person)

    suspend fun addOrUpdatePersonForced(person: Person)



    suspend fun deletePerson(person: Person)

    suspend fun deletePersonPermanently(person: Person)

    suspend fun deletePersonFromGroup(personId: PersonId, groupId: GroupId)
}

class SimilarPersonFoundException(val existPerson: Person): Exception()