package ru.hryasch.coachnotes.fragments.api

import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.SingleStateStrategy
import moxy.viewstate.strategy.SkipStrategy
import moxy.viewstate.strategy.StateStrategyType
import moxy.viewstate.strategy.alias.AddToEndSingle

import ru.hryasch.coachnotes.journal.table.TableModel

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