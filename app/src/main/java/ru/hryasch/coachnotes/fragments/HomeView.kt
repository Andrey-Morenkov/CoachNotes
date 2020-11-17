package ru.hryasch.coachnotes.fragments

import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.SingleStateStrategy
import moxy.viewstate.strategy.StateStrategyType
import ru.hryasch.coachnotes.domain.group.data.Group
import ru.hryasch.coachnotes.home.data.HomeScheduleCell
import ru.hryasch.coachnotes.repository.global.GlobalSettings

interface HomeView: MvpView
{
    @StateStrategyType(AddToEndSingleStrategy::class)
    fun setCoachData(coachData: CoachData)

    @StateStrategyType(AddToEndSingleStrategy::class)
    fun setScheduleCells(scheduleCells: List<HomeScheduleCell>?)
}

data class CoachData(val fullName: GlobalSettings.Coach.CoachFullName,
                     val role: String)