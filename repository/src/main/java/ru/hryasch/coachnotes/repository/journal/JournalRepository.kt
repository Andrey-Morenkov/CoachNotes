package ru.hryasch.coachnotes.repository.journal

import com.soywiz.klock.DateTimeRange
import com.soywiz.klock.YearMonth

import ru.hryasch.coachnotes.repository.common.GroupId
import ru.hryasch.coachnotes.repository.dao.JournalChunkDAO


interface JournalRepository
{
    // 1 month period for now only
    suspend fun getJournalChunks(period: YearMonth, groupId: GroupId): List<JournalChunkDAO>
}