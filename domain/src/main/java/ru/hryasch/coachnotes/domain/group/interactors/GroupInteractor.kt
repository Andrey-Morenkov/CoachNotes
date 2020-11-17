package ru.hryasch.coachnotes.domain.group.interactors

import ru.hryasch.coachnotes.domain.common.GroupId
import ru.hryasch.coachnotes.domain.group.data.Group
import ru.hryasch.coachnotes.domain.person.data.Person
import java.lang.Exception

interface GroupInteractor
{
    suspend fun getGroupsList(): List<Group>

    suspend fun getGroupNames(): Map<GroupId, String>

    suspend fun getPeopleListByGroup(groupId: GroupId): List<Person>

    suspend fun addOrUpdateGroup(group: Group)

    suspend fun addOrUpdateGroupForced(group: Group)

    suspend fun deleteGroupAndRemoveAllPeopleFromThisGroup(group: Group)

    suspend fun deleteGroupPermanently(group: Group)
}

class SimilarGroupFoundException(val existGroup: Group): Exception()