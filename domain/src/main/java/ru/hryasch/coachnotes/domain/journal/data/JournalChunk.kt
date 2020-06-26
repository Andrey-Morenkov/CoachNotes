package ru.hryasch.coachnotes.domain.journal.data

import ru.hryasch.coachnotes.domain.common.GroupId
import ru.hryasch.coachnotes.domain.person.data.Person
import java.time.LocalDate
import java.util.*

data class ChunkPersonName(val surname: String, val name: String): Comparable<ChunkPersonName>
{
    override fun compareTo(other: ChunkPersonName): Int
    {
        return "$surname $name".compareTo("${other.surname} ${other.name}")
    }

    constructor(person: Person): this(person.surname!!, person.name!!)
}

data class JournalChunk(val date: LocalDate,
                        val groupId: GroupId)
{
    val content: SortedMap<ChunkPersonName, CellData?> = TreeMap()
}