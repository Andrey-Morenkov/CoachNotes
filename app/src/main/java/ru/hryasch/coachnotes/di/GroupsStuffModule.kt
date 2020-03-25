package ru.hryasch.coachnotes.di

import android.content.Context
import org.koin.dsl.module
import ru.hryasch.coachnotes.domain.group.data.Group
import ru.hryasch.coachnotes.groups.GroupsAdapter

val groupsModule = module {

    factory { (groupsList: List<Group>) -> GroupsAdapter(groupsList) }
}