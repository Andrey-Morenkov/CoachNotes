package ru.hryasch.coachnotes.domain.journal.interactors

import com.soywiz.klock.Date
import com.soywiz.klock.YearMonth

import ru.hryasch.coachnotes.domain.common.GroupId
import ru.hryasch.coachnotes.domain.journal.data.CellData
import ru.hryasch.coachnotes.domain.journal.data.JournalChunkPersonName
import ru.hryasch.coachnotes.domain.journal.data.TableData


interface JournalInteractor
{
    suspend fun getJournal(period: YearMonth, groupId: GroupId): TableData

    suspend fun saveJournal(tableDump: TableData)

    suspend fun saveChangedCell(date: Date,
                                person: JournalChunkPersonName,
                                cellData: CellData?,
                                groupId: GroupId)
}