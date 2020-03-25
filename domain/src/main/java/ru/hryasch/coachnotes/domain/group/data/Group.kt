package ru.hryasch.coachnotes.domain.group.data

import ru.hryasch.coachnotes.domain.common.GroupId
import ru.hryasch.coachnotes.domain.common.PersonId

interface Group
{
    val id: GroupId
    var name: String
    var availableAbsoluteAge: IntRange?
    val membersList: List<PersonId>
    val isPaid: Boolean
}