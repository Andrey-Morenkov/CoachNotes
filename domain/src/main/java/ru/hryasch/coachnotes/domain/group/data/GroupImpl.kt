package ru.hryasch.coachnotes.domain.group.data

import org.koin.core.KoinComponent
import org.koin.core.get
import org.koin.core.qualifier.named
import ru.hryasch.coachnotes.domain.common.GroupId
import ru.hryasch.coachnotes.domain.common.PersonId
import java.util.LinkedList
import java.util.UUID

class GroupImpl private constructor(override val id: GroupId,
                                    override var name: String,
                                    override var availableAbsoluteAge: IntRange? = null,
                                    override var isPaid: Boolean = false) : Group
{
    override val membersList: MutableList<PersonId> = LinkedList()
    override val scheduleDays: MutableList<ScheduleDay> = LinkedList()

    companion object: KoinComponent {
        fun generateNew(): GroupImpl
        {
            val id: UUID = get(named("groupUUID"))
            return GroupImpl(id.toString(), "")
        }
    }

    override fun compareTo(other: Group): Int
    {
        return "$availableAbsoluteAge $name $id $isPaid".compareTo("${other.availableAbsoluteAge} ${other.name} ${other.id} ${other.isPaid}")
    }

    override fun toString(): String = "Group[$id]: ($name, isPaid = $isPaid, ages = ${availableAbsoluteAge?.first} - ${availableAbsoluteAge?.last}, members[${membersList.size}]: $membersList, scheduleDays: $scheduleDays)"
}