package ru.hryasch.coachnotes.fragments

import moxy.MvpView
import moxy.viewstate.strategy.SingleStateStrategy
import moxy.viewstate.strategy.StateStrategyType
import ru.hryasch.coachnotes.domain.group.data.Group

interface HomeView: MvpView
{
    @StateStrategyType(SingleStateStrategy::class)
    fun setGroups(groups: List<Group>?)
}