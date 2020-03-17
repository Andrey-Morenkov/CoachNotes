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
    // States (level 0)
    @StateStrategyType(SingleStateStrategy::class)
    fun waitingState()

    @StateStrategyType(SingleStateStrategy::class)
    fun showingState(tableContent: TableModel)

    // Additional events (level 1)
    @StateStrategyType(AddToEndSingleStrategy::class)
    fun setPeriod(month: String, year:Int)

    // Runtime events (level 2)
    @StateStrategyType(SkipStrategy::class)
    fun refreshData()
}