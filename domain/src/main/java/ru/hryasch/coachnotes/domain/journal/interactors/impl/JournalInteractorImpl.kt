package ru.hryasch.coachnotes.domain.journal.interactors.impl

import com.pawegio.kandroid.e
import com.pawegio.kandroid.i
import com.soywiz.klock.Date
import com.soywiz.klock.DateTime
import com.soywiz.klock.YearMonth
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.koin.core.qualifier.named

import ru.hryasch.coachnotes.domain.common.GroupId
import ru.hryasch.coachnotes.domain.journal.data.*
import ru.hryasch.coachnotes.domain.journal.interactors.JournalInteractor
import ru.hryasch.coachnotes.domain.person.Person
import ru.hryasch.coachnotes.domain.person.PersonImpl
import ru.hryasch.coachnotes.domain.repository.GroupRepository
import ru.hryasch.coachnotes.domain.repository.JournalRepository
import ru.hryasch.coachnotes.domain.repository.PersonRepository
import ru.hryasch.coachnotes.domain.tools.DataExporter
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashSet


class JournalInteractorImpl: JournalInteractor, KoinComponent
{
    private val journalRepository: JournalRepository by inject(named("mock"))
    private val personRepository: PersonRepository by inject(named("mock"))
    private val groupRepository: GroupRepository by inject(named("mock"))

    private val exporter: DataExporter by inject(named("docx"))

    override suspend fun getJournal(period: YearMonth, groupId: GroupId): TableData?
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

        return generateTableData(period, groupId, chunks, personRepository.getPersonsByGroup(groupId)!!)
    }

    override suspend fun saveJournalChunk(chunk: JournalChunk)
    {
        journalRepository.updateJournalChunk(chunk)
    }


    override suspend fun exportJournal(period: YearMonth, groupId: GroupId)
    {
        val group = groupRepository.getGroup(groupId)!!
        val chunks = journalRepository.getJournalChunks(period, groupId)!!

        exporter.export(chunks, group, period)
    }


    private fun generateTableData(period: YearMonth, groupId: GroupId, chunks: List<JournalChunk>?, groupPeople: List<Person>): TableData?
    {

        val tableData = TableData()

        tableData.groupId = groupId
        tableData.columnHeadersData.addAll(generateDayOfMonthDescription(period))


        generateNames(chunks, groupPeople, period)?.also { tableData.rowHeadersData.addAll(it) } ?: return null
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

    private fun generateNames(chunks: List<JournalChunk>?, groupPeople: List<Person>, period: YearMonth): List<RowHeaderData>?
    {
        val allPeople: MutableSet<Person> = HashSet()

        if (period.isHistorical())
        {
            // using only chunks info
            if (chunks == null)
            {
                // no chunks => no data
                e("historical, no chunks")
                return null
            }
            e("historical, have chunks")
        }
        else
        {
            e("non historical")
            // using mix of chunks people (if chunks exist) and current people
            allPeople.addAll(groupPeople)
        }

        chunks?.forEach {
            it.content.forEach { entry ->
                allPeople.add(PersonImpl(entry.key.surname, entry.key.name, -1, -1))
            }
        }

        val headers: MutableList<RowHeaderData> = LinkedList()
        allPeople.forEach {
            headers.add(RowHeaderData(it))
        }

        headers.sortWith( compareBy { it.person } )

        return headers
    }

    private fun generateCellData(chunks: List<JournalChunk>?,
                                 allPeople: List<RowHeaderData>,
                                 days: List<ColumnHeaderData>): MutableList<MutableList<CellData?>>
    {
        val cells = Array(allPeople.size) { arrayOfNulls<CellData?>(days.size)}

        for (col in cells[0].indices)
        {
            val chunk = chunks?.find { it.date == days[col].timestamp }
            if (chunk == null)
            {
                for (row in cells.indices)
                {
                    cells[row][col] = generateEmptyCellData()
                }
            }
            else
            {
                for (row in cells.indices)
                {
                    val personDataByDay = chunk.content.filter { it.key.surname == allPeople[row].person.surname &&
                                                                 it.key.name == allPeople[row].person.name }
                    // No person data for this date => no exist
                    if (personDataByDay.isEmpty())
                    {
                        cells[row][col] = generateNoExistData()
                    }
                    else
                    {
                        cells[row][col] = personDataByDay.values.first()
                    }
                }
            }
        }

        val cellsList: MutableList<MutableList<CellData?>> = ArrayList()
        cells.forEach {
            cellsList.add(it.toMutableList())
        }

        return cellsList
    }

    private fun generateEmptyCellData(): CellData? = null
    private fun generateNoExistData(): CellData = NoExistData()
}

private fun YearMonth.isHistorical(): Boolean = this.month != DateTime.nowLocal().month