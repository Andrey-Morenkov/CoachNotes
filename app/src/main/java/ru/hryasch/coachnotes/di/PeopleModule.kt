package ru.hryasch.coachnotes.di

import androidx.recyclerview.widget.SortedList
import org.koin.dsl.module
import ru.hryasch.coachnotes.domain.common.GroupId
import ru.hryasch.coachnotes.domain.person.data.Person
import ru.hryasch.coachnotes.people.PeopleAdapter

val peopleModule = module {

    factory { (peopleList: List<Person>, groupsNames: Map<GroupId, String>) -> PeopleAdapter(peopleList, groupsNames) }
}