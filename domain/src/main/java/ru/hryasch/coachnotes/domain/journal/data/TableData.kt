package ru.hryasch.coachnotes.domain.journal.data

import ru.hryasch.coachnotes.domain.common.GroupId

class TableData
{
    var groupId: GroupId = 0
    var columnHeadersData: MutableList<ColumnHeaderData> = ArrayList()
    var rowHeadersData: MutableList<RowHeaderData> = ArrayList()
    var cellsData: MutableList<MutableList<CellData?>> = ArrayList()
}