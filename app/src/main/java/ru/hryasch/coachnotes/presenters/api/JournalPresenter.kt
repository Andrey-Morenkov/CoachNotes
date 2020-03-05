package ru.hryasch.coachnotes.presenters.api

interface JournalPresenter
{
    fun test(col: Int, row: Int)

    fun nextMonth()

    fun prevMonth()

    fun changePeriod(month: String, year: Int)
}