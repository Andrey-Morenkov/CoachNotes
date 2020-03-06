package ru.hryasch.coachnotes.domain.journal.data

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



