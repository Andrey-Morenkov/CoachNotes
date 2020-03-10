package ru.hryasch.coachnotes.domain.journal.data

import com.soywiz.klock.Date
import ru.hryasch.coachnotes.domain.common.PersonId
import ru.hryasch.coachnotes.domain.person.Person

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

data class ColumnHeaderData(var timestamp: Date)
data class RowHeaderData(val person: Person)


