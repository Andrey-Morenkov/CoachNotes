package ru.hryasch.coachnotes.domain.journal.data

import com.soywiz.klock.Date
import ru.hryasch.coachnotes.domain.common.GroupId
import java.util.*
import kotlin.collections.HashMap

data class JournalChunkPersonName(val surname: String, val name: String): Comparable<JournalChunkPersonName>
{
    override fun compareTo(other: JournalChunkPersonName): Int
    {
        return "$surname $name".compareTo("${other.surname} ${other.name}")
    }
}

data class JournalChunk(val date: Date,
                        val groupId: GroupId)
{
    val content: SortedMap<JournalChunkPersonName, CellData?> = TreeMap()
}