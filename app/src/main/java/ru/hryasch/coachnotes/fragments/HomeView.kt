package ru.hryasch.coachnotes.fragments

import moxy.MvpView
import moxy.viewstate.strategy.SingleStateStrategy
import moxy.viewstate.strategy.StateStrategyType
import ru.hryasch.coachnotes.domain.group.data.Group
import ru.hryasch.coachnotes.home.data.HomeScheduleCell

interface HomeView: MvpView
{
    @StateStrategyType(SingleStateStrategy::class)
    fun setScheduleCells(scheduleCells: List<HomeScheduleCell>?)
}