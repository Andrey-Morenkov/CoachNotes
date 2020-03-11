package ru.hryasch.coachnotes.fragments.api

import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.SingleStateStrategy
import moxy.viewstate.strategy.SkipStrategy
import moxy.viewstate.strategy.StateStrategyType


import ru.hryasch.coachnotes.journal.table.TableModel

//TODO: custom strategy as: GeneralState(waiting/showing) + CurrentPeriod + some other additions?

interface JournalView: MvpView
{
    @StateStrategyType(SingleStateStrategy::class)
    fun waitingState()

    @StateStrategyType(AddToEndSingleStrategy::class)
    fun setPeriod(month: String, year:Int)

    @StateStrategyType(SingleStateStrategy::class)
    fun showingState(tableContent: TableModel)

    @StateStrategyType(SkipStrategy::class)
    fun refreshData()
}