package ru.hryasch.coachnotes.repository.group.impl

import org.koin.core.KoinComponent
import ru.hryasch.coachnotes.repository.common.GroupId
import ru.hryasch.coachnotes.repository.dao.GroupDAO
import ru.hryasch.coachnotes.repository.group.GroupRepository

class GroupFakeRepositoryImpl: GroupRepository, KoinComponent
{
    private val groupCount = 1

    init
    {

    }


    override fun getGroup(group: GroupId): GroupDAO
    {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}