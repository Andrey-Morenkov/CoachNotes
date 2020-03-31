package ru.hryasch.coachnotes.di

import org.koin.dsl.module
import ru.hryasch.coachnotes.domain.common.GroupId
import ru.hryasch.coachnotes.domain.person.data.Person
import ru.hryasch.coachnotes.groups.GroupMembersAdapter
import ru.hryasch.coachnotes.people.PeopleAdapter

val peopleModule = module {

    factory { (peopleList: List<Person>, groupsNames: Map<GroupId, String>, listener: PeopleAdapter.PersonClickListener) -> PeopleAdapter(peopleList, groupsNames, listener) }
    factory { (peopleList: List<Person>, groupsNames: Map<GroupId, String>, listener: GroupMembersAdapter.RemovePersonListener) -> GroupMembersAdapter(peopleList, groupsNames, listener) }
}