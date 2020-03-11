package ru.hryasch.coachnotes.journal.presenters

interface JournalPresenter
{
    fun onCellClicked(col: Int, row: Int)
    fun onExportButtonClicked()

    fun nextMonth()
    fun prevMonth()
    fun changePeriod(month: String, year: Int)
}