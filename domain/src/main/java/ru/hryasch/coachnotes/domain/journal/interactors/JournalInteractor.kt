package ru.hryasch.coachnotes.domain.journal.interactors

import com.soywiz.klock.YearMonth

import ru.hryasch.coachnotes.domain.common.GroupId
import ru.hryasch.coachnotes.domain.journal.data.JournalChunk
import ru.hryasch.coachnotes.domain.journal.data.TableData


interface JournalInteractor
{
    suspend fun getJournal(period: YearMonth, groupId: GroupId): TableData?

    suspend fun saveJournalChunk(chunk: JournalChunk)

    suspend fun exportJournal(period: YearMonth, groupId: GroupId)
}