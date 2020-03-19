package ru.hryasch.coachnotes.fragments.api

import moxy.MvpView
import moxy.viewstate.strategy.*


import ru.hryasch.coachnotes.journal.table.TableModel

//TODO: custom strategy as: GeneralState(waiting/showing) + CurrentPeriod + some other additions?

interface JournalView: MvpView
{
    // Base (level 0)
    @StateStrategyType(SingleStateStrategy::class)
    fun waitingState()

    @StateStrategyType(SingleStateStrategy::class)
    fun showingState(tableContent: TableModel?)

    // Permanent state (level 1)
    @StateStrategyType(AddToEndSingleStrategy::class)
    fun setPeriod(month: String, year:Int)

    // Timed events (level 2)
    @StateStrategyType(AddToEndSingleStrategy::class)
    fun showSavingJournalNotification(isFinished: Boolean?) //hotfix with null to dismiss this

    // Runtime events (level 3)
    @StateStrategyType(SkipStrategy::class)
    fun refreshData()
}