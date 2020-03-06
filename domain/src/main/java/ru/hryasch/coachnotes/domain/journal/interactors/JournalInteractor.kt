package ru.hryasch.coachnotes.domain.journal.interactors

import com.soywiz.klock.YearMonth
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job

import ru.hryasch.coachnotes.domain.common.GroupId
import ru.hryasch.coachnotes.domain.journal.data.JournalChunk
import ru.hryasch.coachnotes.domain.journal.data.TableData


interface JournalInteractor
{
    suspend fun getJournal(period: YearMonth, groupId: GroupId): TableData

    fun saveJournal(tableDump: TableData): Job
}