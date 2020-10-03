package ru.hryasch.coachnotes.journal.table.data

import com.evrencoskun.tableview.sort.ISortableModel
import ru.hryasch.coachnotes.domain.journal.data.*
import ru.hryasch.coachnotes.domain.person.data.Person
import java.time.LocalDate

data class CellModel(private val id: String,
                     var       data: CellData?) : ISortableModel
{
    override fun getContent(): Any? = data
    override fun getId(): String = id

    override fun toString(): String
    {
        return "[CellModel($id): $data]"
    }
}

data class ColumnHeaderModel(val date: LocalDate)

data class RowHeaderModel(var index: Int,
                          val person: Person)