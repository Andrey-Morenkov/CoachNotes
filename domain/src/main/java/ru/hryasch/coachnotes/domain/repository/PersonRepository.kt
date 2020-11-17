package ru.hryasch.coachnotes.domain.repository

import ru.hryasch.coachnotes.domain.common.GroupId
import ru.hryasch.coachnotes.domain.common.PersonId
import ru.hryasch.coachnotes.domain.person.data.Person

interface PersonRepository: AbstractRepository
{
    suspend fun getSimilarPersonIfExists(personSurname: String): Person?
    suspend fun getPerson(personId: PersonId): Person?
    suspend fun getDeletedPerson(personId: PersonId): Person?
    suspend fun getPeople(peopleIds: List<PersonId>): List<Person>?
    suspend fun getAllPeople(): List<Person>?
    suspend fun getAllExistingPeople(): List<Person>?
    suspend fun getAllDeletedPeople(): List<Person>?
    suspend fun getPeopleByGroup(groupId: GroupId): List<Person>?

    suspend fun addOrUpdatePeople(people: List<Person>)
    suspend fun deletePerson(personId: PersonId)
    suspend fun deletePersonPermanently(personId: PersonId)
    suspend fun revivePerson(personId: PersonId): Person?
}