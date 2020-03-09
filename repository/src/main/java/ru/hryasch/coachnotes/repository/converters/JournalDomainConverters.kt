package ru.hryasch.coachnotes.repository.converters

import com.pawegio.kandroid.i
import com.pawegio.kandroid.w
import com.soywiz.klock.DateFormat
import com.soywiz.klock.parse
import ru.hryasch.coachnotes.domain.journal.data.*
import ru.hryasch.coachnotes.repository.dao.JournalChunkDAO
import ru.hryasch.coachnotes.repository.dao.JournalMarkAbsence
import ru.hryasch.coachnotes.repository.dao.JournalMarkDAO
import ru.hryasch.coachnotes.repository.dao.JournalMarkPresence
import java.util.*

val daoDateFormat = DateFormat("dd/MM/yyyy")

fun TableData.toDAO(): List<JournalChunkDAO>
{
    val chunks: MutableList<JournalChunkDAO> = LinkedList()



    return chunks
}

fun JournalMarkDAO.fromDAO(): CellData?
{
    return when (this)
    {
        is JournalMarkPresence -> PresenceData()
        is JournalMarkAbsence -> AbsenceData(this.mark)
    }
}

fun List<JournalChunkDAO>.fromDAO(): List<JournalChunk>
{
    val chunkList: MutableList<JournalChunk> = LinkedList()

    for (item in this)
    {
        val chunk = JournalChunk(daoDateFormat.parse(item.timestamp).local.date,
                                 item.groupId)

        item.data.forEach {
            val name = it.name
            val mark = JournalMarkDAO.fromString(it.mark)?.fromDAO()

            chunk.content[name] = mark
        }
        chunkList.add(chunk)
    }
    return chunkList
}