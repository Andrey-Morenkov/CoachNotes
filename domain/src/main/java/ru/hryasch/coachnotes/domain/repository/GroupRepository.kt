package ru.hryasch.coachnotes.domain.repository

import ru.hryasch.coachnotes.domain.common.GroupId
import ru.hryasch.coachnotes.domain.group.data.Group
import ru.hryasch.coachnotes.domain.person.data.Person

interface GroupRepository: AbstractRepository
{
    suspend fun getSimilarGroupIfExists(groupName: String): Group?
    suspend fun getGroup(groupId: GroupId): Group?
    suspend fun getDeletedGroup(groupId: GroupId): Group?
    suspend fun getGroups(groups: List<GroupId>): List<Group>?
    suspend fun getAllGroups(): List<Group>?
    suspend fun getAllExistingGroups(): List<Group>?
    suspend fun getAllDeletedGroups(): List<Group>?
    suspend fun getGroupsByScheduleDay(dayPosition0: Int): List<Group>?

    suspend fun addOrUpdateGroup(group: Group)
    suspend fun deleteGroup(group: Group)
    suspend fun deleteGroupPermanently(group: Group)
    suspend fun reviveGroup(groupId: GroupId): Group?

    suspend fun updatePeopleGroupAffiliation(people: List<Person>)
}