package ru.hryasch.coachnotes.domain.repository

import com.soywiz.klock.Date
import com.soywiz.klock.YearMonth
import ru.hryasch.coachnotes.domain.common.GroupId
import ru.hryasch.coachnotes.domain.group.data.Group
import ru.hryasch.coachnotes.domain.journal.data.CellData
import ru.hryasch.coachnotes.domain.journal.data.JournalChunk
import ru.hryasch.coachnotes.domain.person.Person


interface JournalRepository
{
    // 1 month period for now only
    suspend fun getJournalChunks(period: YearMonth,
                                 groupId: GroupId): List<JournalChunk>?

    suspend fun updateJournalChunk(chunk: JournalChunk)
}