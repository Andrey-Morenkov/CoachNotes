package ru.hryasch.coachnotes.domain.journal.interactors.impl

import com.pawegio.kandroid.e
import com.pawegio.kandroid.i
import com.soywiz.klock.YearMonth
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.koin.core.qualifier.named

import ru.hryasch.coachnotes.domain.common.GroupId
import ru.hryasch.coachnotes.domain.journal.data.ColumnHeaderData
import ru.hryasch.coachnotes.domain.journal.data.JournalChunk
import ru.hryasch.coachnotes.domain.journal.data.TableData
import ru.hryasch.coachnotes.domain.journal.interactors.JournalInteractor
import ru.hryasch.coachnotes.domain.repository.GroupRepository
import ru.hryasch.coachnotes.domain.repository.JournalRepository
import ru.hryasch.coachnotes.domain.repository.PersonRepository


class JournalInteractorImpl: JournalInteractor, KoinComponent
{
    private val journalRepository: JournalRepository by inject(named("mock"))
    private val personRepository: PersonRepository by inject(named("mock"))

    override suspend fun getJournal(period: YearMonth, groupId: GroupId): TableData
    {
        val chunks = journalRepository.getJournalChunks(period, groupId)

        chunks?.forEach {
            e("CHUNK")
            i("timestamp = ${it.date.day}/${it.date.month}/${it.date.year}")
            i("groupId = ${it.groupId}")
            it.content.forEach { cnt ->
                i("data[${cnt.key} : ${cnt.value}]")
            }
        }

        delay(5000)

        return generateTableData(period, chunks)
    }

    override fun saveJournal(tableDump: TableData): Job
    {
        TODO()
    }

    private fun generateTableData(period: YearMonth, chunks: List<JournalChunk>?): TableData
    {
        val tableData = TableData()



        return tableData
    }

    private fun generateDayOfMonthDescription(period: YearMonth): List<ColumnHeaderData>
    {

    }

    private fun fillRowHeaders(tableData: TableData, chunks: List<JournalChunk>?)
    {

    }

    private fun fillCellsData(tableData: TableData, chunks: List<JournalChunk>?)
    {

    }
}