package ru.hryasch.coachnotes.domain.journal.data

import com.soywiz.klock.Date
import ru.hryasch.coachnotes.domain.common.GroupId

data class JournalChunkPersonName(val surname: String, val name: String)

data class JournalChunk(val date: Date,
                        val groupId: GroupId)
{
    val content: MutableMap<JournalChunkPersonName, CellData?> = HashMap()
}