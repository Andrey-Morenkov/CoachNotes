package ru.hryasch.coachnotes.domain.group.data

import ru.hryasch.coachnotes.domain.common.GroupId
import ru.hryasch.coachnotes.domain.common.PersonId

interface Group
{
    val id: GroupId
    var name: String
    var availableAge: Byte?
    val membersList: List<PersonId>
}