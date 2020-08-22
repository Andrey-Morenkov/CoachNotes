package ru.hryasch.coachnotes.di

import org.koin.core.qualifier.named
import org.koin.dsl.module
import ru.hryasch.coachnotes.domain.common.GroupId
import ru.hryasch.coachnotes.domain.person.data.Person
import ru.hryasch.coachnotes.groups.GroupMembersAdapter
import ru.hryasch.coachnotes.people.PeopleAdapter
import java.util.UUID

val peopleModule = module {

    factory { (peopleList: List<Person>, groupsNames: Map<GroupId, String>, listener: PeopleAdapter.PersonClickListener) -> PeopleAdapter(peopleList, groupsNames, listener) }
    factory { (peopleList: MutableList<Person>, groupsNames: Map<GroupId, String>, listener: GroupMembersAdapter.RemovePersonListener) -> GroupMembersAdapter(peopleList, groupsNames, listener) }

    factory(named("personUUID"))
    {
        return@factory UUID.randomUUID() as UUID
    }
}