package ru.hryasch.coachnotes.domain.group.data

import ru.hryasch.coachnotes.domain.common.GroupId
import ru.hryasch.coachnotes.domain.common.PersonId
import java.io.Serializable

interface Group: Comparable<Group>, Serializable
{
    // Required params
    val id: GroupId
    var name: String

    // Optional params
    var availableAbsoluteAgeLow: Int?
    var availableAbsoluteAgeHigh: Int?
    val membersList: MutableList<PersonId>
    val scheduleDays: MutableList<ScheduleDay>
    var isPaid: Boolean
    var deletedTimestamp: Long? // if group deleted, this is not null

    fun copy(): Group
    fun applyData(otherGroup: Group)
    override fun toString(): String
}