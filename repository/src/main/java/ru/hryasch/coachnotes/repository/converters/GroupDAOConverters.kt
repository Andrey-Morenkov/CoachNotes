package ru.hryasch.coachnotes.repository.converters

import ru.hryasch.coachnotes.domain.group.data.Group
import ru.hryasch.coachnotes.domain.group.data.GroupImpl
import ru.hryasch.coachnotes.repository.dao.GroupDAO
import java.util.*

@JvmName("DAOGroupListConverter")
fun List<GroupDAO>.fromDAO(): List<Group>
{
    val groupList: MutableList<Group> = LinkedList()

    this.forEach { groupList.add(it.fromDAO()) }

    return groupList
}

fun GroupDAO.fromDAO(): Group
{
    val group = GroupImpl(this.id!!, this.name!!, this.availableAge?.toByte())
    this.members.forEach {
        group.membersList.add(it)
    }
    return group
}