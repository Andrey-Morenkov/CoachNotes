package ru.hryasch.coachnotes.journal.table

import ru.hryasch.coachnotes.domain.common.GroupId
import ru.hryasch.coachnotes.journal.table.viewholders.CellModel
import ru.hryasch.coachnotes.journal.table.viewholders.ColumnHeaderModel
import ru.hryasch.coachnotes.journal.table.viewholders.RowHeaderModel


class TableModel
{
    var groupId: GroupId = ""
    var columnHeaderContent: MutableList<ColumnHeaderModel> = ArrayList()
    var rowHeaderContent: MutableList<RowHeaderModel> = ArrayList()
    var rowFullHeaderContent: MutableList<RowHeaderModel> = ArrayList()
    var cellContent: MutableList<MutableList<CellModel>> = ArrayList()
    var cellFullContent: MutableList<MutableList<CellModel>> = ArrayList()
}