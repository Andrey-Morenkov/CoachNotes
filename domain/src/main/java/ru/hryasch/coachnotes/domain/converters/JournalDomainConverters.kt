package ru.hryasch.coachnotes.domain.converters

import com.pawegio.kandroid.i
import com.pawegio.kandroid.w
import com.soywiz.klock.parse
import ru.hryasch.coachnotes.domain.journal.data.*
import ru.hryasch.coachnotes.repository.dao.JournalChunkDAO
import ru.hryasch.coachnotes.repository.dao.JournalMarkAbsence
import ru.hryasch.coachnotes.repository.dao.JournalMarkDAO
import ru.hryasch.coachnotes.repository.dao.JournalMarkPresence
import ru.hryasch.coachnotes.repository.journal.impl.daoDateFormat
import java.util.*

fun TableData.toDAO(): List<JournalChunkDAO> {
    val chunks: MutableList<JournalChunkDAO> = LinkedList()



    return chunks
}

fun JournalMarkDAO.fromDAO(): CellData? {
    return when (this) {
        is JournalMarkPresence -> PresenceData()
        is JournalMarkAbsence -> AbsenceData(this.mark)
        else -> null
    }
}

fun List<JournalChunkDAO>.fromDAO(): List<JournalChunk>
{
    i("fromDAO")
    val chunkList: MutableList<JournalChunk> = LinkedList()

    for (item in this)
    {
        val chunk = JournalChunk(daoDateFormat.parse(item.timestamp).local.date,
                                 item.groupId)

        w("fromDao groupId: ${item.groupId}")
        w("fromDao timestamp: ${chunk.date.day}/${chunk.date.month}/${chunk.date.year}")

        item.data.forEach {
            val name = it.name
            val mark = JournalMarkDAO.fromString(it.mark)?.fromDAO()

            w("fromDao name: $name")
            w("fromDao mark: ${mark.toString()}")

            chunk.content[name] = mark
        }
        chunkList.add(chunk)
    }
    return chunkList
}