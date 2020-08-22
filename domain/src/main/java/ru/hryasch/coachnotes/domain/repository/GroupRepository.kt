package ru.hryasch.coachnotes.domain.repository

import ru.hryasch.coachnotes.domain.common.GroupId
import ru.hryasch.coachnotes.domain.group.data.Group
import ru.hryasch.coachnotes.domain.person.data.Person

interface GroupRepository: AbstractRepository
{
    suspend fun getGroup(groupId: GroupId): Group?
    suspend fun getAllGroups(): List<Group>?
    suspend fun getAllExistGroups(): List<Group>?
    suspend fun getAllDeletedGroups(): List<Group>?
    suspend fun getGroupsByScheduleDay(dayPosition0: Int): List<Group>?

    suspend fun addOrUpdateGroup(group: Group)
    suspend fun deleteGroup(group: Group)
    suspend fun updatePeopleGroupAffiliation(people: List<Person>)
}