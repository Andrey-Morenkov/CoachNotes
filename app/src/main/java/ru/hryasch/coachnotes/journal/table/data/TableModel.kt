package ru.hryasch.coachnotes.journal.table.data

import com.pawegio.kandroid.d
import com.pawegio.kandroid.e
import kotlinx.coroutines.*
import ru.hryasch.coachnotes.domain.common.GroupId
import ru.hryasch.coachnotes.domain.common.PersonId
import ru.hryasch.coachnotes.domain.group.data.ScheduleDay
import ru.hryasch.coachnotes.domain.journal.data.CellData
import ru.hryasch.coachnotes.domain.journal.data.RawTableData
import ru.hryasch.coachnotes.domain.person.data.Person
import java.time.LocalDate
import java.util.stream.Collectors
import java.util.stream.IntStream

class TableModel(rawTableData: RawTableData)
{
    val groupId: GroupId = rawTableData.group?.id ?: "DELETED"

    val columnHeaderContent: List<ColumnHeaderModel>
    val columnsToHide:       List<Int>
    val rowHeaderContent:    List<RowHeaderModel>
    val rowsToHide:          List<Int>
    val cellsContent:        List<List<CellModel>>

    init
    {
        var columnHeaders:     List<ColumnHeaderModel> = ArrayList(0)
        var columnHideHeaders: List<Int>               = ArrayList(0)
        var rowHeaders:        List<RowHeaderModel>    = ArrayList(0)
        var rowHideHeaders:    List<Int>               = ArrayList(0)
        var cContent:          List<List<CellModel>>   = ArrayList(0)

        runBlocking {
            GlobalScope.launch(Dispatchers.Default)
            {
                val col  = async { generateColumnHeaders(rawTableData.daysData, rawTableData.group?.scheduleDays) }
                val row  = async { generateRowHeaders(rawTableData.peopleData,  rawTableData.group?.membersList) }
                val cell = async { generateCells(rawTableData.cellsData) }

                columnHeaders = col.await().first
                columnHideHeaders = col.await().second
                rowHeaders = row.await().first
                rowHideHeaders = row.await().second
                cContent = cell.await()
            }.join()
        }

        columnHeaderContent = columnHeaders
        columnsToHide = columnHideHeaders
        rowHeaderContent = rowHeaders
        rowsToHide = rowHideHeaders
        cellsContent = cContent
    }

    private fun generateColumnHeaders(rawDaysData: List<LocalDate>, groupSchedule: List<ScheduleDay>?): Pair<List<ColumnHeaderModel>, List<Int>>
    {
        val columnHeaders: MutableList<ColumnHeaderModel> = ArrayList(rawDaysData.size)
        val columnHideHeaders: MutableList<Int> = ArrayList()
        val makeScheduleDayCheck = groupSchedule != null && groupSchedule.isNotEmpty()
        val existScheduleDaysPositions = groupSchedule?.stream()?.map(ScheduleDay::dayPosition0)?.collect(Collectors.toList())
        d("existScheduleDays = ${existScheduleDaysPositions.toString()}")

        for ((i, rawDay) in rawDaysData.withIndex())
        {
            columnHeaders.add(ColumnHeaderModel(i, rawDay))
            if (makeScheduleDayCheck && existScheduleDaysPositions!!.stream().noneMatch { existDay0 -> existDay0 == (rawDay.dayOfWeek.value - 1) })
            {
                columnHideHeaders.add(i)
            }
        }

        d("hide ${columnHideHeaders.size} columns")
        return Pair(columnHeaders, columnHideHeaders)
    }

    private fun generateRowHeaders(rawPeopleData: List<Person>, groupPeopleIds: List<PersonId>?): Pair<List<RowHeaderModel>, List<Int>>
    {
        val rowHeaders: MutableList<RowHeaderModel> = ArrayList(rawPeopleData.size)
        val rowHideHeaders: MutableList<Int> = ArrayList()
        val makeMembersCheck = groupPeopleIds != null && groupPeopleIds.isNotEmpty()
        val existPeople = groupPeopleIds?.stream()

        for ((i, rawPerson) in rawPeopleData.withIndex())
        {
            rowHeaders.add(RowHeaderModel(i,i + 1, rawPerson))
            if (makeMembersCheck && existPeople!!.noneMatch { existPerson -> existPerson == rawPerson.id })
            {
                rowHideHeaders.add(i)
            }
        }

        return Pair(rowHeaders, rowHideHeaders)
    }

    private fun generateCells(rawCellsData: List<List<CellData?>>): List<List<CellModel>>
    {
        val cells: MutableList<List<CellModel>> = ArrayList(rawCellsData.size)
        IntStream
            .range(0, rawCellsData.size) // for each person
            .parallel()
            .forEach { y ->
                val rowData = rawCellsData[y]
                val rowCells: MutableList<CellModel> = ArrayList(rowData.size)
                for ((x, cellData) in rowData.withIndex())
                {
                    rowCells.add(CellModel("$x:$y", cellData))
                }
                cells.add(y, rowCells)
            }

        return cells
    }
}