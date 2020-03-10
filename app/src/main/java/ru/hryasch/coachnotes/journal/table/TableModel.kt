package ru.hryasch.coachnotes.journal.table

import ru.hryasch.coachnotes.domain.common.GroupId
import ru.hryasch.coachnotes.journal.table.viewholders.CellModel
import ru.hryasch.coachnotes.journal.table.viewholders.ColumnHeaderModel
import ru.hryasch.coachnotes.journal.table.viewholders.RowHeaderModel


class TableModel()
{
    var groupId: GroupId = 0
    var columnHeaderContent: MutableList<ColumnHeaderModel> = ArrayList()
    var rowHeaderContent: MutableList<RowHeaderModel> = ArrayList()
    var cellContent: MutableList<MutableList<CellModel>> = ArrayList()
}