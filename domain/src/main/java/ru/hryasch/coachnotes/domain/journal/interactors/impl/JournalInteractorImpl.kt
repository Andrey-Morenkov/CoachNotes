package ru.hryasch.coachnotes.domain.journal.interactors.impl

import com.pawegio.kandroid.e
import com.pawegio.kandroid.i
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.koin.core.qualifier.named

import ru.hryasch.coachnotes.domain.common.GroupId
import ru.hryasch.coachnotes.domain.common.PersonId
import ru.hryasch.coachnotes.domain.journal.data.*
import ru.hryasch.coachnotes.domain.journal.interactors.JournalInteractor
import ru.hryasch.coachnotes.domain.person.data.Person
import ru.hryasch.coachnotes.domain.person.data.PersonImpl
import ru.hryasch.coachnotes.domain.repository.GroupRepository
import ru.hryasch.coachnotes.domain.repository.JournalRepository
import ru.hryasch.coachnotes.domain.repository.PersonRepository
import ru.hryasch.coachnotes.domain.tools.DataExporter
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZonedDateTime
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.stream.IntStream
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.HashSet


class JournalInteractorImpl: JournalInteractor, KoinComponent
{
    private val journalRepository: JournalRepository by inject(named("release"))
    private val personRepository: PersonRepository by inject(named("release"))
    private val groupRepository: GroupRepository by inject(named("release"))

    private val exporter: DataExporter by inject(named("docx"))

    override suspend fun getJournal(period: YearMonth, groupId: GroupId): TableData?
    {
        val chunks = journalRepository.getJournalChunks(period, groupId)

        chunks?.forEach {
            e("CHUNK")
            i("timestamp = ${it.date.dayOfMonth}/${it.date.month.value}/${it.date.year}")
            i("groupId = ${it.groupId}")
            it.content.forEach { cnt ->
                i("data[${cnt.key} : ${cnt.value}]")
            }
        }

        return if (period.isHistorical())
               {
                   generateTableData(period, groupId, chunks, null)
               }
               else
               {
                   generateTableData(period, groupId, chunks, personRepository.getPeopleByGroup(groupId))
               }
    }

    override suspend fun saveJournalChunk(chunk: JournalChunk)
    {
        journalRepository.updateJournalChunk(chunk)
    }


    override suspend fun exportJournal(period: YearMonth, groupId: GroupId)
    {
        val group = groupRepository.getGroup(groupId)!!
        val chunks = journalRepository.getJournalChunks(period, groupId)!!

        // TODO: get fresh people data before exporting

        exporter.export(chunks, group, period)
    }



    private suspend fun generateTableData(period: YearMonth, groupId: GroupId, chunks: List<JournalChunk>?, groupPeople: List<Person>?): TableData?
    {
        if (period.isHistorical() && chunks == null)
        {
            // No data about historical period
            return null
        }

        val columnHeaders: ArrayList<LocalDate> = ArrayList()
        val rowHeaders: ArrayList<Person> = ArrayList()

        GlobalScope.launch(Dispatchers.Default)
        {
            val days  = async(Dispatchers.Default) { generateDayOfMonthDescription(period) }
            val names = async(Dispatchers.Default) { generateNames(chunks, groupPeople, period) }

            columnHeaders.addAll(days.await())
            rowHeaders.addAll(names.await()!!)
        }.join()

        val cellData: List<List<CellData?>>
        cellData = if (chunks == null)
                   {
                       generateEmptyCellTable(columnHeaders.size, rowHeaders.size)
                   }
                   else
                   {
                       generateCellTable(chunks, rowHeaders, columnHeaders, period)
                   }

        return TableData(groupId, rowHeaders, columnHeaders, cellData)
    }

    private fun generateDayOfMonthDescription(period: YearMonth): List<LocalDate>
    {
        val headers: MutableList<LocalDate> = ArrayList(period.month.length(period.isLeapYear))

        for (day in (1 .. period.month.length(period.isLeapYear)))
        {
            headers[day - 1] = LocalDate.of(period.year, period.month, day)
        }
        return headers
    }

    private fun generateNames(chunks: List<JournalChunk>?, groupPeople: List<Person>?, period: YearMonth): List<Person>?
    {
        val allPeople: MutableMap<PersonId, Person> = HashMap()

        if (period.isHistorical())
        {
            // using only chunks info
            if (chunks == null)
            {
                // no chunks => no data
                i("historical, no chunks")
                return null
            }
            i("historical, have chunks")
        }
        else
        {
            e("non historical")
            // using mix of chunks people (if chunks exist) and current people
            groupPeople?.forEach {
                allPeople[it.id] = it
            }
        }

        chunks?.forEach {
            it.content.forEach { entry ->
                // Group data has more priority because it has freshest data
                allPeople.putIfAbsent(entry.key.personId, PersonImpl(entry.key.personId, entry.key.surname, entry.key.name, -1))
            }
        }

        val headers: MutableList<Person> = LinkedList()
        allPeople.values.toList().sorted().forEach {
            headers.add(it)
        }

        return headers
    }

    private fun generateEmptyCellTable(width: Int, height: Int): List<List<CellData?>>
    {
        val cellsList: ArrayList<ArrayList<CellData?>> = ArrayList(height)
        for (i in 0 until height)
        {
            cellsList[i] = ArrayList(width)
            for (j in 0 until width)
            {
                cellsList[i][j] = null
            }
        }

        return cellsList
    }

    private fun generateCellTable(chunks   : List<JournalChunk>,
                                  allPeople: ArrayList<Person>,
                                  days     : ArrayList<LocalDate>,
                                  period   : YearMonth): List<List<CellData?>>
    {
        // <Date <PersonId, CellData?>>
        val chunksMap: ConcurrentHashMap<LocalDate, ConcurrentHashMap<PersonId, CellData?>> = ConcurrentHashMap()
        chunks.forEach {chunk ->
            chunksMap[chunk.date] = ConcurrentHashMap()
            chunk.content.forEach {
                chunksMap[chunk.date]!![it.key.personId] = it.value
            }
        }

        val cells = Array(allPeople.size) { arrayOfNulls<CellData?>(days.size)}
        IntStream
            .range(0, cells.size) // for each person
            .parallel()
            .forEach { row ->
                val rowData = cells[row]
                val noExistDaysPositions: MutableList<Int> = LinkedList()

                for (col in rowData.indices) // for each day
                {
                    if (!chunksMap.contains(days[col]))
                    {
                        // No chunk data for this day
                        rowData[col] = generateEmptyCellData() // empty cell
                        continue
                    }

                    if (!chunksMap[days[col]]!!.contains(allPeople[row].id))
                    {
                        // No chunk data for this person
                        rowData[col] = generateNoExistData() // "x"
                        noExistDaysPositions.add(col)
                        continue
                    }

                    // Have chunk data for this person
                    rowData[col] = chunksMap[days[col]]!![allPeople[row].id]
                }

                postProcessRowCells(rowData, allPeople[row], noExistDaysPositions, period.isHistorical())
            }

        val cellsList: MutableList<MutableList<CellData?>> = ArrayList()
        cells.forEach {
            cellsList.add(it.toMutableList())
        }

        return cellsList
    }

    private fun generateEmptyCellData(): CellData? = null
    private fun generateNoExistData(): CellData = NoExistData()

    private fun postProcessRowCells(cells: Array<CellData?>, person: Person, noExistDaysPositions: List<Int>, isHistorical: Boolean)
    {
        if (isHistorical)
        {
            postProcessHistorical(cells, noExistDaysPositions)
        }
        else
        {
            postProcessNonHistorical(cells, person, noExistDaysPositions)
        }
    }

    private fun fillNoExistDataWaveAlgorithm(noExistPosition: Int, array: Array<CellData?>, rightExcludeBorderPosition: Int? = null)
    {
        // fills both sides of "no exist" data like waves from a stone thrown into the water  ((( <- X -> )))

        var currPos = noExistPosition - 1
        while((currPos >= 0) && (array[currPos] == null))
        {
            array[currPos] = NoExistData()
            currPos--
        }

        val stopPos = rightExcludeBorderPosition ?: array.size
        currPos = noExistPosition + 1
        while ((currPos < array.size) && (currPos < stopPos) && (array[currPos] == null))
        {
            array[currPos] = NoExistData()
            currPos++
        }
    }

    private fun postProcessHistorical(cells: Array<CellData?>, noExistDaysPositions: List<Int>)
    {
        if (noExistDaysPositions.isEmpty())
        {
            return
        }

        for (noExistDay in noExistDaysPositions)
        {
            fillNoExistDataWaveAlgorithm(noExistDay, cells)
        }
    }

    private fun postProcessNonHistorical(cells: Array<CellData?>, person: Person, noExistDaysPositions: List<Int>)
    {
        val todayPosition = LocalDate.now().dayOfMonth - 1
        if (person.isNotInGroupNow())
        {
            val noExistDaysExtended: MutableList<Int> = LinkedList(noExistDaysPositions)
            noExistDaysExtended.add(todayPosition)
            
            for (noExistDay in noExistDaysExtended)
            {
                fillNoExistDataWaveAlgorithm(noExistDay, cells, todayPosition)
            }
            return
        }

        for (noExistDay in noExistDaysPositions)
        {
            fillNoExistDataWaveAlgorithm(noExistDay, cells, todayPosition)
        }
    }
}

private fun YearMonth.isHistorical(): Boolean = this.month != LocalDate.now().month

private fun Person.isNotInGroupNow(): Boolean = this.birthdayYear <= 0