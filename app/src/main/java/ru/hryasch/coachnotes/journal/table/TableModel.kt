package ru.hryasch.coachnotes.journal.table

import ru.hryasch.coachnotes.journal.table.viewholders.CellModel
import ru.hryasch.coachnotes.journal.table.viewholders.ColumnHeaderModel
import ru.hryasch.coachnotes.journal.table.viewholders.RowHeaderModel


class TableModel()
{
    var columnHeaderContent: MutableList<ColumnHeaderModel> = ArrayList()
    var rowHeaderContent: MutableList<RowHeaderModel> = ArrayList()
    var cellContent: MutableList<MutableList<CellModel>> = ArrayList()
}