package ru.hryasch.coachnotes.fragments

import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.StateStrategyType

interface HomeView: MvpView
{
    @StateStrategyType(AddToEndSingleStrategy::class)
    fun setPersonsCount(count: Int?)

    @StateStrategyType(AddToEndSingleStrategy::class)
    fun setGroupsCount(count: Int?)
}