package ru.hryasch.coachnotes.fragments

import androidx.recyclerview.widget.SortedList
import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.OneExecutionStateStrategy
import moxy.viewstate.strategy.StateStrategyType
import ru.hryasch.coachnotes.domain.group.data.Group

interface GroupsView: MvpView
{
    @StateStrategyType(AddToEndSingleStrategy::class)
    fun setGroupsList(groupsList: List<Group>?)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun refreshData()
}