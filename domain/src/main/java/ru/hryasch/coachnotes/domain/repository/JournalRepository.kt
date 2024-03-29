package ru.hryasch.coachnotes.domain.repository

import ru.hryasch.coachnotes.domain.common.GroupId
import ru.hryasch.coachnotes.domain.journal.data.JournalChunk
import java.time.YearMonth


interface JournalRepository: AbstractRepository
{
    // 1 month period for now only
    suspend fun getJournalChunks(period: YearMonth, groupId: GroupId): List<JournalChunk>?
    suspend fun updateJournalChunk(chunk: JournalChunk)
    suspend fun deleteAllJournalsByGroup(groupId: GroupId)
}