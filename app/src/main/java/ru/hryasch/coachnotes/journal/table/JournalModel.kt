package ru.hryasch.coachnotes.journal.table

import com.evrencoskun.tableview.sort.ISortableModel

sealed class CellData(val mark: String? = null)
{
    override fun toString(): String
    {
        return "<cell_data: $mark>"
    }
}

class PresenceData() : CellData()
{
    override fun toString(): String
    {
        return "<presence_data: $mark>"
    }
}

class AbsenceData(mark: String? = null) : CellData(mark)
{
    override fun toString(): String
    {
        return "<absence_data: $mark>"
    }
}

data class ColumnHeaderData(val day: Int, val dayOfWeek: String)

data class RowHeaderData(val surname: String, val name: String)



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