package ru.hryasch.coachnotes.domain.journal.interactors.impl

import com.pawegio.kandroid.e
import com.pawegio.kandroid.i
import com.soywiz.klock.Date
import com.soywiz.klock.YearMonth
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.koin.core.qualifier.named

import ru.hryasch.coachnotes.domain.common.GroupId
import ru.hryasch.coachnotes.domain.journal.data.*
import ru.hryasch.coachnotes.domain.journal.interactors.JournalInteractor
import ru.hryasch.coachnotes.domain.person.Person
import ru.hryasch.coachnotes.domain.person.PersonImpl
import ru.hryasch.coachnotes.domain.repository.JournalRepository
import ru.hryasch.coachnotes.domain.repository.PersonRepository
import java.util.*
import kotlin.collections.HashSet


class JournalInteractorImpl: JournalInteractor, KoinComponent
{
    private val journalRepository: JournalRepository by inject(named("mock"))
    private val personRepository: PersonRepository by inject(named("mock"))

    override suspend fun getJournal(period: YearMonth, groupId: GroupId): TableData
    {
        val chunks = journalRepository.getJournalChunks(period, groupId)

        chunks?.forEach {
            e("CHUNK")
            i("timestamp = ${it.date.day}/${it.date.month.index1}/${it.date.year}")
            i("groupId = ${it.groupId}")
            it.content.forEach { cnt ->
                i("data[${cnt.key} : ${cnt.value}]")
            }
        }

        //TODO: chunks post processing

        return generateTableData(period, groupId, chunks, personRepository.getPersonsByGroup(groupId)!!)
    }

    override suspend fun saveJournal(tableDump: TableData)
    {
        TODO()
    }

    override suspend fun saveChangedCell(date: Date,
                                         person: Person,
                                         cellData: CellData?,
                                         groupId: GroupId)
    {
        journalRepository.updateJournalChunkData(date, groupId, person, cellData)
    }



    private fun generateTableData(period: YearMonth, groupId: GroupId, chunks: List<JournalChunk>?, people: List<Person>): TableData
    {
        val tableData = TableData()

        tableData.groupId = groupId
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
            headers.add(ColumnHeaderData(Date.Companion.invoke(period, day)))
        }
        return headers
    }

    private fun generateNames(chunks: List<JournalChunk>?, people: List<Person>): List<RowHeaderData>
    {
        val allPeople: MutableSet<Person> = HashSet()

        people.forEach {
            allPeople.add(PersonImpl(it.surname, it.name))
        }

        val chunksPeople: MutableSet<Person> = HashSet()
        chunks?.forEach {
            it.content.forEach { entry ->
                chunksPeople.add(PersonImpl(entry.key.surname, entry.key.name))
            }
        }

        chunksPeople.forEach {
            allPeople.add(it)
        }

        val headers: MutableList<RowHeaderData> = LinkedList()
        allPeople.forEach {
            headers.add(RowHeaderData(it))
        }

        headers.sortBy {
            it.person.surname
            it.person.name
        }

        return headers
    }

    private fun generateCellData(chunks: List<JournalChunk>?,
                                 allPeople: List<RowHeaderData>,
                                 days: List<ColumnHeaderData>): MutableList<MutableList<CellData?>>
    {
        val cells: MutableList<MutableList<CellData?>> = LinkedList()

        allPeople.forEach { personData ->
            val dataByDay: MutableList<CellData?> = LinkedList()

            days.forEach { dayData ->
                val chunk = chunks?.find { it.date == dayData.timestamp }
                if (chunk == null)
                {
                    dataByDay.add(generateEmptyCellData())
                }
                else
                {
                    val personDataByDay = chunk.content.filter { it.key.surname == personData.person.surname &&
                                                                 it.key.name == personData.person.name }

                    if (personDataByDay.isEmpty())
                    {
                        //TODO: there was no this personData => new noExistData?
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