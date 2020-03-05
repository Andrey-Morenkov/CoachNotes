package ru.hryasch.coachnotes.journal.table

import com.mooveit.library.Fakeit
import com.pawegio.kandroid.e
import kotlin.random.Random

open class TableModel()
{
    var columnHeaderContent: MutableList<ColumnHeaderModel> = ArrayList()
    var rowHeaderContent: MutableList<RowHeaderModel> = ArrayList()
    var cellContent: MutableList<MutableList<CellModel>> = ArrayList()
}

object QuickMarch2020DowGenerator
{
    // 1 march is вс
    fun getDOF(day: Int): String
    {
        return when (day % 7)
        {
            0 -> "сб"
            1 -> "вс"
            2 -> "пн"
            3 -> "вт"
            4 -> "ср"
            5 -> "чт"
            6 -> "пт"

            else -> "---"
        }
    }
}

class MockTableModel(): TableModel()
{
    val width = 20
    val height = 100

    init
    {
        generateColumnHeader()
        generateRowHeader()
        generateCells()
    }

    private fun generateColumnHeader()
    {
        for (i in 1..width)
        {
            columnHeaderContent.add(ColumnHeaderModel(ColumnHeaderData(i, QuickMarch2020DowGenerator.getDOF(i))))
        }
    }

    private fun generateRowHeader()
    {
        for (i in 1..height)
        {
            val nnn = Fakeit.name().name().split(" ")
            rowHeaderContent.add(RowHeaderModel( RowHeaderData(nnn[0], nnn[1])))
        }
    }

    private fun generateCells()
    {
        for (i in 0 until height)
        {
            val col: MutableList<CellModel> = ArrayList()

            for (j in 0 until width)
            {
                val magic = Random.nextInt(0,30)

                val cmm = if (magic < 3)
                {
                    when (magic)
                    {
                        0 -> CellModel("$j:$i", PresenceData())
                        1 -> CellModel("$j:$i", AbsenceData())
                        2 -> CellModel("$j:$i", AbsenceData("Б"))
                        else -> null
                    }
                }
                else
                {
                    CellModel("$j:$i", null)
                }

                e("Generated: $cmm")

                col.add(cmm!!)
            }

            cellContent.add(col)
        }
    }
}