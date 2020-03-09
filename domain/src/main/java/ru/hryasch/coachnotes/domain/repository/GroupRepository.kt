package ru.hryasch.coachnotes.domain.repository

import ru.hryasch.coachnotes.domain.common.GroupId
import ru.hryasch.coachnotes.domain.group.data.Group

interface GroupRepository
{
    suspend fun getGroup(group: GroupId): Group
}