package ru.hryasch.coachnotes.domain.person.interactors

import ru.hryasch.coachnotes.domain.common.GroupId
import ru.hryasch.coachnotes.domain.person.data.Person

interface PersonInteractor
{
    suspend fun getPeopleList(): List<Person>

    suspend fun getGroupNames(): Map<GroupId, String>
}