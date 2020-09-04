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
    var availableAbsoluteAge: IntRange?
    val membersList: MutableList<PersonId>
    val scheduleDays: MutableList<ScheduleDay>
    var isPaid: Boolean
    var deletedTimestamp: Long? // if group deleted, this is not null

    override fun toString(): String
}