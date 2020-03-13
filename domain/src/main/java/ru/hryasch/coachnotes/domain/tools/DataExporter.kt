package ru.hryasch.coachnotes.domain.tools

import com.soywiz.klock.YearMonth
import ru.hryasch.coachnotes.domain.group.data.Group
import ru.hryasch.coachnotes.domain.journal.data.JournalChunk

interface DataExporter
{
    suspend fun export(chunks: List<JournalChunk>,
                       group: Group,
                       period: YearMonth,
                       coachName: String? = null)
}