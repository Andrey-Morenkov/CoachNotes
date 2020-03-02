package ru.hryasch.coachnotes.journal

import com.evrencoskun.tableview.sort.ISortableModel

sealed class CellData(val mark: String? = null)
class PresenceData() : CellData()
class AbsenceData(mark: String?) : CellData(mark)

data class ColumnHeaderData(val day: Int, val dayOfWeek: String)

data class RowHeaderData(val surname: String, val name: String)



class CellModel(private var id: String,
                var data: CellData?) : ISortableModel
{
    override fun getContent(): Any? = data
    override fun getId(): String = id
}

class ColumnHeaderModel(var data: ColumnHeaderData)

class RowHeaderModel(var data: RowHeaderData)