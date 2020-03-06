package ru.hryasch.coachnotes.domain.converters

import ru.hryasch.coachnotes.domain.journal.data.TableData
import ru.hryasch.coachnotes.repository.dao.JournalChunkDAO
import java.util.*

fun TableData.toDAO(): List<JournalChunkDAO>
{
    val chunks: MutableList<JournalChunkDAO> = LinkedList()



    return chunks
}