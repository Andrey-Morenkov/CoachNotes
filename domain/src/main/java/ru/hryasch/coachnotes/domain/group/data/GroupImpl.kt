package ru.hryasch.coachnotes.domain.group.data

import ru.hryasch.coachnotes.domain.common.GroupId
import ru.hryasch.coachnotes.domain.common.PersonId
import java.util.*

class GroupImpl(override val id: GroupId,
                override var name: String,
                override var availableAge: Byte? = null) : Group
{
    override val membersList: MutableList<PersonId> = LinkedList()
}