package ru.hryasch.coachnotes.domain.repository

import ru.hryasch.coachnotes.domain.common.GroupId
import ru.hryasch.coachnotes.domain.common.PersonId
import ru.hryasch.coachnotes.domain.person.Person

interface PersonRepository
{
    suspend fun getPerson(person: PersonId): Person?
    suspend fun getPersonsByGroup(group: GroupId): List<Person>?
}