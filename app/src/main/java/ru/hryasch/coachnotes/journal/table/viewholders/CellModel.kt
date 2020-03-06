package ru.hryasch.coachnotes.journal.table.viewholders

import com.evrencoskun.tableview.sort.ISortableModel
import ru.hryasch.coachnotes.domain.journal.data.CellData
import ru.hryasch.coachnotes.domain.journal.data.ColumnHeaderData
import ru.hryasch.coachnotes.domain.journal.data.RowHeaderData

class CellModel(private var id: String,
                var data: CellData?) : ISortableModel
{
    override fun getContent(): Any? = data
    override fun getId(): String = id

    override fun toString(): String
    {
        return "[CellModel($id): $data]"
    }
}

class ColumnHeaderModel(var data: ColumnHeaderData)

class RowHeaderModel(var data: RowHeaderData)