package ru.hryasch.coachnotes.domain.journal.data

import com.soywiz.klock.Date
import ru.hryasch.coachnotes.domain.person.data.Person

sealed class CellData(val mark: String? = null)
{
    override fun toString(): String
    {
        return "<cell_data: $mark>"
    }

    companion object
    {
        fun getCopy(data: CellData?): CellData?
        {
            return when (data)
            {
                is PresenceData -> PresenceData()
                is AbsenceData -> AbsenceData(data.mark)
                is UnknownData -> UnknownData()
                is NoExistData -> NoExistData()
                else -> null
            }
        }
    }
}

class PresenceData() : CellData()
{
    override fun toString(): String
    {
        return "<presence_data>"
    }
}

class AbsenceData(mark: String? = null) : CellData(mark)
{
    override fun toString(): String
    {
        return "<absence_data: $mark>"
    }
}

class UnknownData(): CellData()
{
    override fun toString(): String
    {
        return "<unknown_data>"
    }
}

class NoExistData(): CellData()
{
    override fun toString(): String
    {
        return "<noexist_data>"
    }
}

data class ColumnHeaderData(var timestamp: Date)
data class RowHeaderData(val person: Person, val number: Int)


