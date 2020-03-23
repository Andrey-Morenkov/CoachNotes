package ru.hryasch.coachnotes.journal.presenters

interface JournalPresenter
{
    fun onCellClicked(col: Int, row: Int)
    fun onCellLongPressed(col: Int, row: Int)
    fun onColumnLongPressed(col: Int)

    fun onExportButtonClicked()
    fun onLockUnlockJournal()

    fun onJournalSaveNotificationDismiss()

    fun nextMonth()
    fun prevMonth()
    fun changePeriod(month: String, year: Int)
    fun deleteColumnData(col: Int?)
}