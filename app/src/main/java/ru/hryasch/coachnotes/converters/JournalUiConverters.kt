package ru.hryasch.coachnotes.converters

import ru.hryasch.coachnotes.domain.journal.data.TableData
import ru.hryasch.coachnotes.journal.table.TableModel
import ru.hryasch.coachnotes.journal.table.viewholders.CellModel
import ru.hryasch.coachnotes.journal.table.viewholders.ColumnHeaderModel
import ru.hryasch.coachnotes.journal.table.viewholders.RowHeaderModel

fun TableData.toModel(): TableModel
{
    val tableModel: TableModel = TableModel()

    columnHeadersData.forEach {
        tableModel.columnHeaderContent.add(ColumnHeaderModel(it))
    }

    rowHeadersData.forEach {
        tableModel.rowHeaderContent.add(RowHeaderModel(it))
    }

    for ((y, row) in cellsData.iterator().withIndex())
    {
        val rowModel: MutableList<CellModel> = ArrayList()

        for ((x, data) in row.iterator().withIndex())
        {
            rowModel.add(CellModel("$x:$y", data))
        }

        tableModel.cellContent.add(rowModel)
    }

    return tableModel
}