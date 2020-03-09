package ru.hryasch.coachnotes.domain.group.data

import ru.hryasch.coachnotes.domain.common.GroupId

interface Group
{
    val id: GroupId
    var name: String
    var availableAge: Byte


}