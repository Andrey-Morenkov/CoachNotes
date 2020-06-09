package ru.hryasch.coachnotes.fragments

import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.StateStrategyType
import ru.hryasch.coachnotes.domain.group.data.Group

interface HomeView: MvpView
{
    @StateStrategyType(AddToEndSingleStrategy::class)
    fun setPeopleCount(count: Int?)

    @StateStrategyType(AddToEndSingleStrategy::class)
    fun setGroups(groups: List<Group>?)
}