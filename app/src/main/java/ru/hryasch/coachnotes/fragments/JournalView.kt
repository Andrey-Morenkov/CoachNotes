package ru.hryasch.coachnotes.fragments

import moxy.MvpView
import moxy.viewstate.strategy.*


import ru.hryasch.coachnotes.journal.table.data.TableModel
import java.time.LocalDate
import java.time.YearMonth

//TODO: custom strategy as: GeneralState(waiting/showing) + CurrentPeriod + some other additions?

interface JournalView: MvpView
{
    // Base (level 0)
    @StateStrategyType(SingleStateStrategy::class)
    fun loadingState()

    @StateStrategyType(SingleStateStrategy::class)
    fun showingState(tableContent: TableModel?, noPeople: Boolean = false)

    // Permanent state (level 1)
    @StateStrategyType(AddToEndSingleStrategy::class)
    fun setPeriod(period: YearMonth)

    @StateStrategyType(AddToEndSingleStrategy::class)
    fun lockJournal(isLocked: Boolean? = true)

    @StateStrategyType(AddToEndSingleStrategy::class)
    fun hideRows(rows: List<Int>?)

    @StateStrategyType(AddToEndSingleStrategy::class)
    fun hideColumns(columns: List<Int>?)

    // Timed events (level 2)
    @StateStrategyType(AddToEndSingleStrategy::class)
    fun showSavingJournalNotification(isFinished: Boolean? = true) //hotfix with null to dismiss this

    // Runtime events (level 3)
    @StateStrategyType(SkipStrategy::class)
    fun refreshData()

    @StateStrategyType(SkipStrategy::class)
    fun showLockedJournalNotification()

    @StateStrategyType(SkipStrategy::class)
    fun showDeleteColumnNotification(date: LocalDate, col: Int)
}