package ru.hryasch.coachnotes.repository.converters

import com.soywiz.klock.DateFormat
import com.soywiz.klock.parse
import ru.hryasch.coachnotes.domain.journal.data.*
import ru.hryasch.coachnotes.domain.person.Person
import ru.hryasch.coachnotes.domain.person.PersonImpl
import ru.hryasch.coachnotes.repository.dao.*
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

fun PersonDAO.fromDAO(): Person
{
    return PersonImpl(this.id, this.name!!, this.surname!!)
}

fun List<JournalChunkDAO>.fromDAO(): List<JournalChunk>
{
    val chunkList: MutableList<JournalChunk> = LinkedList()

    for (item in this)
    {
        val chunk = JournalChunk(daoDateFormat.parse(item.timestamp).local.date,
                                 item.groupId)

        item.data.forEach {
            val mark = JournalMarkDAO.fromString(it.mark)?.fromDAO()

            chunk.content[JournalChunkPersonName(it.surname, it.name)] = mark
        }
        chunkList.add(chunk)
    }
    return chunkList
}