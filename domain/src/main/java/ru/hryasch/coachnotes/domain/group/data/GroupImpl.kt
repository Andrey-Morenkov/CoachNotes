package ru.hryasch.coachnotes.domain.group.data

import ru.hryasch.coachnotes.domain.common.GroupId
import ru.hryasch.coachnotes.domain.common.PersonId
import java.util.*

class GroupImpl(override val id: GroupId,
                override var name: String,
                override var availableAbsoluteAge: IntRange? = null,
                override var isPaid: Boolean = false) : Group
{
    override val membersList: MutableList<PersonId> = LinkedList()

    override fun compareTo(other: Group): Int
    {
        return "$id $name $isPaid".compareTo("${other.id} ${other.name} ${other.isPaid}")
    }

    override fun toString(): String = "$id: [$name, isPaid = $isPaid, ages = ${availableAbsoluteAge?.first} - ${availableAbsoluteAge?.last}, members count = ${membersList.size}]"
}