package ru.hryasch.coachnotes.domain.group.data

import ru.hryasch.coachnotes.domain.common.GroupId
import ru.hryasch.coachnotes.domain.common.PersonId
import java.io.Serializable

interface Group: Comparable<Group>, Serializable
{
    val id: GroupId
    var name: String
    var availableAbsoluteAge: IntRange?
    val membersList: List<PersonId>
    val isPaid: Boolean

    override fun toString(): String
}