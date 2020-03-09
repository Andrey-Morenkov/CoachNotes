package ru.hryasch.coachnotes.domain.journal.interactors.impl

import com.pawegio.kandroid.e
import com.pawegio.kandroid.i
import com.soywiz.klock.Date
import com.soywiz.klock.YearMonth
import kotlinx.coroutines.Job
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.koin.core.qualifier.named

import ru.hryasch.coachnotes.domain.common.GroupId
import ru.hryasch.coachnotes.domain.journal.data.*
import ru.hryasch.coachnotes.domain.journal.interactors.JournalInteractor
import ru.hryasch.coachnotes.domain.person.Person
import ru.hryasch.coachnotes.domain.repository.JournalRepository
import ru.hryasch.coachnotes.domain.repository.PersonRepository
import java.util.*
import kotlin.collections.HashSet


class JournalInteractorImpl: JournalInteractor, KoinComponent
{
    private val journalRepository: JournalRepository by inject(named("mock"))
    private val personRepository: PersonRepository by inject(named("mock"))

    private val dayOfWeekNames: Array<String> by inject(named("daysOfWeek_RU"))

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

        return generateTableData(period, chunks, personRepository.getPersonsByGroup(1)!!)
    }

    override fun saveJournal(tableDump: TableData): Job
    {
        TODO()
    }

    private fun generateTableData(period: YearMonth, chunks: List<JournalChunk>?, people: List<Person>): TableData
    {
        val tableData = TableData()

        tableData.columnHeadersData.addAll(generateDayOfMonthDescription(period))
        tableData.rowHeadersData.addAll(generateNames(chunks, people))
        tableData.cellsData.addAll(generateCellData(chunks, tableData.rowHeadersData, tableData.columnHeadersData))

        return tableData
    }

    private fun generateDayOfMonthDescription(period: YearMonth): List<ColumnHeaderData>
    {
        val headers: MutableList<ColumnHeaderData> = LinkedList<ColumnHeaderData>()
        for (day in (1 .. period.days))
        {
            val dayOfWeek = Date.invoke(period.year, period.month, day).dayOfWeek.index0Monday
            headers.add(ColumnHeaderData(day, dayOfWeekNames[dayOfWeek]))
        }
        return headers
    }

    private fun generateNames(chunks: List<JournalChunk>?, people: List<Person>): List<RowHeaderData>
    {
        val allPeople: MutableSet<JournalChunkPersonName> = HashSet()

        people.forEach {
            allPeople.add(JournalChunkPersonName(it.surname, it.name))
        }

        val chunksPeople: MutableSet<JournalChunkPersonName> = HashSet()
        chunks?.forEach {
            it.content.forEach { entry ->
                chunksPeople.add(entry.key)
            }
        }

        chunksPeople.forEach {
            allPeople.add(it)
        }

        val headers: MutableList<RowHeaderData> = LinkedList()
        allPeople.forEach {
            headers.add(RowHeaderData(it.surname, it.name))
        }

        return headers
    }

    private fun generateCellData(chunks: List<JournalChunk>?, allPeople: List<RowHeaderData>, days: List<ColumnHeaderData>): MutableList<MutableList<CellData?>>
    {
        val cells: MutableList<MutableList<CellData?>> = LinkedList()

        allPeople.forEach { person ->
            val dataByDay: MutableList<CellData?> = LinkedList()

            days.forEach { dayData ->
                val chunk = chunks?.find { it -> it.date.day == dayData.day }
                if (chunk == null)
                {
                    dataByDay.add(generateEmptyCellData())
                }
                else
                {
                    val personDataByDay = chunk.content.filter { it.key.surname == person.surname &&
                                                                 it.key.name == person.name}

                    if (personDataByDay.isEmpty())
                    {
                        //TODO: there was no this person => new noExistData?
                        dataByDay.add(generateEmptyCellData())
                    }
                    else
                    {
                        dataByDay.add(personDataByDay.values.first())
                    }
                }
            }

            cells.add(dataByDay)
        }

        return cells
    }

    private fun generateEmptyCellData(): CellData? = null
}