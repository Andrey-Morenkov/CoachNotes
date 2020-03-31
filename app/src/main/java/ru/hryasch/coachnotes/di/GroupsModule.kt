package ru.hryasch.coachnotes.di

import org.koin.dsl.module
import ru.hryasch.coachnotes.domain.group.data.Group
import ru.hryasch.coachnotes.groups.GroupsAdapter

val groupsModule = module {

    factory { (groupsList: List<Group>, listener: GroupsAdapter.GroupClickListener) -> GroupsAdapter(groupsList, listener) }

}