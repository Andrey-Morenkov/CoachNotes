package ru.hryasch.coachnotes.repository.converters

import ru.hryasch.coachnotes.domain.group.data.Group
import ru.hryasch.coachnotes.domain.group.data.GroupImpl
import ru.hryasch.coachnotes.repository.dao.GroupDAO
import java.util.*

@JvmName("DAOGroupListConverter")
fun List<GroupDAO>.fromDAO(): List<Group>
{
    val groupList: MutableList<Group> = LinkedList()

    this.forEach {
        val group = GroupImpl(it.id!!, it.name!!, it.availableAge?.toByte())
        it.members.forEach {
            group.membersList.add(it)
        }
        groupList.add(group)
    }

    return groupList
}