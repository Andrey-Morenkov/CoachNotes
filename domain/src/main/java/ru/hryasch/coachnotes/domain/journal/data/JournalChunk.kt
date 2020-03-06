package ru.hryasch.coachnotes.domain.journal.data

import com.soywiz.klock.YearMonth
import ru.hryasch.coachnotes.domain.common.GroupId

data class JournalChunk(val date: YearMonth,
                        val groupId: GroupId)
{
    val content: Map<String, CellData?> = HashMap()

    fun isEmpty() = content.isEmpty()
}