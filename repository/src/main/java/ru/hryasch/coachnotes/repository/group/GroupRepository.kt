package ru.hryasch.coachnotes.repository.group

import ru.hryasch.coachnotes.repository.common.GroupId
import ru.hryasch.coachnotes.repository.dao.GroupDAO

interface GroupRepository
{
    fun getGroup(group: GroupId): GroupDAO
}