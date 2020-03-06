package ru.hryasch.coachnotes.domain.journal.interactors.impl

import com.soywiz.klock.DateTimeRange
import com.soywiz.klock.YearMonth
import kotlinx.coroutines.Job
import org.koin.core.KoinComponent
import org.koin.core.inject

import ru.hryasch.coachnotes.domain.common.GroupId
import ru.hryasch.coachnotes.domain.journal.data.TableData
import ru.hryasch.coachnotes.domain.journal.interactors.JournalInteractor
import ru.hryasch.coachnotes.repository.journal.JournalRepository


class JournalInteractorImpl: JournalInteractor, KoinComponent
{
    private val journalRepository: JournalRepository by inject()

    override suspend fun getJournal(period: YearMonth, groupId: GroupId): TableData
    {
        val chunks = journalRepository.getJournalChunks(period, groupId)

    }

    override fun saveJournal(tableDump: TableData): Job
    {
        TODO()
    }
}