package ru.hryasch.coachnotes.repository.person

import ru.hryasch.coachnotes.repository.common.PersonId
import ru.hryasch.coachnotes.repository.dao.PersonDAO

interface PersonRepository
{
    fun getPerson(person: PersonId): PersonDAO
    fun getPersonList(persons: List<PersonId>): List<PersonDAO>
}