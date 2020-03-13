package ru.hryasch.coachnotes.domain.group.data

import ru.hryasch.coachnotes.domain.common.GroupId

class GroupImpl(
    override val id: GroupId,
    override var name: String,
    override var availableAge: Byte
) : Group
{

}