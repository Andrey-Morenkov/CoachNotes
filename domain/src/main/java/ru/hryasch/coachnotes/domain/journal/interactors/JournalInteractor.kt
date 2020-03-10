package ru.hryasch.coachnotes.domain.journal.interactors

import com.soywiz.klock.Date
import com.soywiz.klock.YearMonth

import ru.hryasch.coachnotes.domain.common.GroupId
import ru.hryasch.coachnotes.domain.journal.data.CellData
import ru.hryasch.coachnotes.domain.journal.data.TableData
import ru.hryasch.coachnotes.domain.person.Person


interface JournalInteractor
{
    // TODO: convert fun with return to unit fun with callback channel
    suspend fun getJournal(period: YearMonth, groupId: GroupId): TableData

    suspend fun saveJournal(tableDump: TableData)

    // TODO: change save cell to save chunk
    suspend fun saveChangedCell(date: Date,
                                person: Person,
                                cellData: CellData?,
                                groupId: GroupId)
}