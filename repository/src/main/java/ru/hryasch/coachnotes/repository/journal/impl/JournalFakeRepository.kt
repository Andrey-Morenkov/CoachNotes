package ru.hryasch.coachnotes.repository.journal.impl

import com.soywiz.klock.DateTimeRange
import com.soywiz.klock.YearMonth
import org.koin.core.KoinComponent
import ru.hryasch.coachnotes.repository.common.GroupId
import ru.hryasch.coachnotes.repository.dao.JournalChunkDAO
import ru.hryasch.coachnotes.repository.journal.JournalRepository

class JournalFakeRepository: JournalRepository, KoinComponent
{

    override suspend fun getJournalChunks(
        period: YearMonth,
        groupId: GroupId
    ): List<JournalChunkDAO> {
        return ArrayList<JournalChunkDAO>()
    }
}