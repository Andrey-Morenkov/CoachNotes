package ru.hryasch.coachnotes.domain.tools

import ru.hryasch.coachnotes.domain.group.data.Group
import ru.hryasch.coachnotes.domain.journal.data.JournalChunk
import java.time.YearMonth

interface DataExporter
{
    suspend fun export(chunks: List<JournalChunk>,
                       group: Group,
                       period: YearMonth,
                       coachName: String? = null)
}