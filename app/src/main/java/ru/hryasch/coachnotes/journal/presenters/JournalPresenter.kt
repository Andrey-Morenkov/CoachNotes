package ru.hryasch.coachnotes.journal.presenters

import ru.hryasch.coachnotes.domain.group.data.Group
import java.time.YearMonth

interface JournalPresenter
{
    // View events
    fun onCellClicked(col: Int, row: Int)
    fun onCellLongPressed(col: Int, row: Int)
    fun onColumnLongPressed(col: Int)
    fun onExportDocButtonClicked()
    fun onLockUnlockJournal()
    fun onShowAllPeopleClicked(isShowAll: Boolean)
    fun onShowAllDaysClicked(isShowAll: Boolean)
    fun onJournalSaveNotificationDismiss()

    // Commands
    fun changePeriod(newPeriod: YearMonth)
    fun deleteColumnData(col: Int?)

    fun applyGroupData(group: Group)
}