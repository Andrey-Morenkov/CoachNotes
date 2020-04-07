package ru.hryasch.coachnotes.fragments

import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.SingleStateStrategy
import moxy.viewstate.strategy.StateStrategyType
import ru.hryasch.coachnotes.domain.common.GroupId
import ru.hryasch.coachnotes.domain.group.data.Group
import ru.hryasch.coachnotes.domain.person.data.Person

interface GroupView: MvpView
{
    @StateStrategyType(SingleStateStrategy::class)
    fun setGroupData(group: Group, members: List<Person>, groupNames: Map<GroupId, String>)

    @StateStrategyType(SingleStateStrategy::class)
    fun loadingState()

    @StateStrategyType(AddToEndSingleStrategy::class)
    fun showDeletePersonFromGroupNotification(person: Person?)

    @StateStrategyType(AddToEndSingleStrategy::class)
    fun showAddPeopleToGroupNotification(people: List<Person>?)
}