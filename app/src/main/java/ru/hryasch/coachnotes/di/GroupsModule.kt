package ru.hryasch.coachnotes.di

import org.koin.core.qualifier.named
import org.koin.dsl.module
import ru.hryasch.coachnotes.domain.group.data.Group
import ru.hryasch.coachnotes.groups.GroupsAdapter
import java.util.UUID

val groupsModule = module {

    factory { (groupsList: List<Group>, listener: GroupsAdapter.GroupClickListener) -> GroupsAdapter(groupsList, listener) }

    factory(named("groupUUID"))
    {
        return@factory UUID.randomUUID() as UUID
    }
}