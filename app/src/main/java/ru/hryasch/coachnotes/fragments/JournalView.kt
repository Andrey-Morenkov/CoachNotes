package ru.hryasch.coachnotes.fragments

import moxy.MvpView
import moxy.viewstate.strategy.*


import ru.hryasch.coachnotes.journal.table.TableModel

//TODO: custom strategy as: GeneralState(waiting/showing) + CurrentPeriod + some other additions?

interface JournalView: MvpView
{
    // Base (level 0)
    @StateStrategyType(SingleStateStrategy::class)
    fun loadingState()

    @StateStrategyType(SingleStateStrategy::class)
    fun showingState(tableContent: TableModel?)

    // Permanent state (level 1)
    @StateStrategyType(AddToEndSingleStrategy::class)
    fun setPeriod(month: String, year: Int)

    @StateStrategyType(AddToEndSingleStrategy::class)
    fun lockJournal(isLocked: Boolean? = true)

    // Timed events (level 2)
    @StateStrategyType(AddToEndSingleStrategy::class)
    fun showSavingJournalNotification(isFinished: Boolean? = true) //hotfix with null to dismiss this

    @StateStrategyType(AddToEndSingleStrategy::class)
    fun showDeleteColNotification(dateString: String?, col: Int = -1)

    // Runtime events (level 3)
    @StateStrategyType(SkipStrategy::class)
    fun refreshData()

    @StateStrategyType(SkipStrategy::class)
    fun showLockedJournalNotification()
}