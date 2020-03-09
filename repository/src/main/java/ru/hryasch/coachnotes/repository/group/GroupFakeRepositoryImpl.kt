package ru.hryasch.coachnotes.repository.group

import io.realm.Realm
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.koin.core.qualifier.named
import ru.hryasch.coachnotes.domain.group.data.Group
import ru.hryasch.coachnotes.domain.repository.GroupRepository
import ru.hryasch.coachnotes.repository.common.GroupId
import ru.hryasch.coachnotes.repository.dao.GroupDAO

class GroupFakeRepositoryImpl: GroupRepository, KoinComponent
{
    private val db: Realm by inject(named("groups_mock"))

    init
    {

    }


    override suspend fun getGroup(group: GroupId): Group
    {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun generateGroupDb()
    {

    }
}