package ru.hryasch.coachnotes.repository.journal.impl

import com.soywiz.klock.DateTimeRange
import com.soywiz.klock.YearMonth
import io.realm.Realm
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.koin.core.qualifier.named
import ru.hryasch.coachnotes.repository.common.GroupId
import ru.hryasch.coachnotes.repository.dao.JournalChunkDAO
import ru.hryasch.coachnotes.repository.journal.JournalRepository

class JournalFakeRepository: JournalRepository, KoinComponent
{
    private val db: Realm by inject(named("journal_storage_mock"))

    override suspend fun getJournalChunks(period: YearMonth,
                                          groupId: GroupId): List<JournalChunkDAO>
    {
        return ArrayList<JournalChunkDAO>()
    }
}