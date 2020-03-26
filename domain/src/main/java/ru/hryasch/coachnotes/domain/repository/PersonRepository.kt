package ru.hryasch.coachnotes.domain.repository

import ru.hryasch.coachnotes.domain.common.GroupId
import ru.hryasch.coachnotes.domain.common.PersonId
import ru.hryasch.coachnotes.domain.person.data.Person

interface PersonRepository
{
    suspend fun getPerson(personId: PersonId): Person?
    suspend fun getPersonsByGroup(groupId: GroupId): List<Person>?
    suspend fun getAllPeople(): List<Person>?
}