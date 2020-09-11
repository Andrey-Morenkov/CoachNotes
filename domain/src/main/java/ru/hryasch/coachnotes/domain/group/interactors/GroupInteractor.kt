package ru.hryasch.coachnotes.domain.group.interactors

import ru.hryasch.coachnotes.domain.common.GroupId
import ru.hryasch.coachnotes.domain.group.data.Group
import ru.hryasch.coachnotes.domain.person.data.Person

interface GroupInteractor
{
    suspend fun getGroupsList(): List<Group>

    suspend fun getGroupNames(): Map<GroupId, String>

    suspend fun getPeopleListByGroup(groupId: GroupId): List<Person>

    suspend fun addOrUpdateGroup(group: Group)

    suspend fun deleteGroup(group: Group)

    suspend fun deleteGroupPermanently(group: Group)
}