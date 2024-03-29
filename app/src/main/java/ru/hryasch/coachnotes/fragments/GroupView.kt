package ru.hryasch.coachnotes.fragments

import moxy.MvpView
import moxy.viewstate.strategy.*
import ru.hryasch.coachnotes.domain.common.GroupId
import ru.hryasch.coachnotes.domain.group.data.Group
import ru.hryasch.coachnotes.domain.person.data.Person

interface GroupView: MvpView
{
    @StateStrategyType(SingleStateStrategy::class)
    fun setGroupData(group: Group, members: List<Person>, groupNames: Map<GroupId, String>)

    @StateStrategyType(SingleStateStrategy::class)
    fun loadingState()

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun showAddPeopleToGroupNotification(people: List<Person>?)
}