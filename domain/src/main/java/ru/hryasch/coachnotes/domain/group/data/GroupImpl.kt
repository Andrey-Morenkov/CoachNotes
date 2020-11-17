package ru.hryasch.coachnotes.domain.group.data

import org.koin.core.KoinComponent
import org.koin.core.get
import org.koin.core.qualifier.named
import ru.hryasch.coachnotes.domain.common.GroupId
import ru.hryasch.coachnotes.domain.common.PersonId
import java.util.LinkedList
import java.util.UUID

class GroupImpl (override val id: GroupId,
                 override var name: String) : Group
{
    override var deletedTimestamp: Long? = null
    override var availableAbsoluteAgeLow:  Int? = null
    override var availableAbsoluteAgeHigh: Int? = null
    override var isPaid: Boolean = false
    override val membersList: MutableList<PersonId> = LinkedList()
    override val scheduleDays: MutableList<ScheduleDay> = LinkedList()

    companion object: KoinComponent {
        fun generateNew(): GroupImpl
        {
            val id: UUID = get(named("groupUUID"))
            return GroupImpl(id.toString(), "")
        }
    }

    override fun copy(): Group
    {
        val copiedGroup = GroupImpl(id, name)
        copiedGroup.deletedTimestamp = deletedTimestamp
        copiedGroup.availableAbsoluteAgeLow = availableAbsoluteAgeLow
        copiedGroup.availableAbsoluteAgeHigh = availableAbsoluteAgeHigh
        copiedGroup.isPaid = isPaid
        copiedGroup.membersList.addAll(membersList)
        copiedGroup.scheduleDays.addAll(scheduleDays)

        return copiedGroup
    }

    override fun applyData(otherGroup: Group)
    {
        name = otherGroup.name
        deletedTimestamp = otherGroup.deletedTimestamp
        availableAbsoluteAgeLow = otherGroup.availableAbsoluteAgeLow
        availableAbsoluteAgeHigh = otherGroup.availableAbsoluteAgeHigh
        isPaid = otherGroup.isPaid
        membersList.clear()
        membersList.addAll(otherGroup.membersList)
        scheduleDays.clear()
        scheduleDays.addAll(otherGroup.scheduleDays)
    }

    override fun compareTo(other: Group): Int
    {
        if (name != other.name)
        {
            return name.compareTo(other.name)
        }

        if (isPaid != other.isPaid)
        {
            return isPaid.compareTo(other.isPaid)
        }

        return id.compareTo(other.id)
    }

    override fun toString(): String = "Group[$id]: ($name, isPaid = $isPaid, ages = $availableAbsoluteAgeLow - ${availableAbsoluteAgeHigh}, members[${membersList.size}]: $membersList, scheduleDays: $scheduleDays)"
}