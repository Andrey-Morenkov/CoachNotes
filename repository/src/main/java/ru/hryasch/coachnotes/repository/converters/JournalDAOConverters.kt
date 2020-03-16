package ru.hryasch.coachnotes.repository.converters

import com.soywiz.klock.DateFormat
import ru.hryasch.coachnotes.domain.journal.data.*
import ru.hryasch.coachnotes.domain.person.Person
import ru.hryasch.coachnotes.domain.person.PersonImpl
import ru.hryasch.coachnotes.repository.dao.*
import java.util.*

val daoDateFormat = DateFormat("dd/MM/yyyy")


fun JournalMarkDAO.fromDAO(): CellData
{
    return when (this)
    {
        is JournalMarkPresenceDAO -> PresenceData()
        is JournalMarkAbsenceDAO -> AbsenceData(this.mark)
    }
}

fun CellData.toDAO(): JournalMarkDAO
{
    return when (this)
    {
        is PresenceData -> JournalMarkPresenceDAO()
        is AbsenceData -> JournalMarkAbsenceDAO(this.mark)
    }
}

@JvmName("DAOChunkListConverter")
fun List<JournalChunkDAO>.fromDAO(): List<JournalChunk>
{
    val chunkList: MutableList<JournalChunk> = LinkedList()

    this.forEach {
        val (date, groupId) = JournalChunkDAOId.deserialize(it.id!!)

        val chunk = JournalChunk(date, groupId)

        it.data.forEach { personDataDAO ->
            val mark = JournalMarkDAO.deserialize(personDataDAO.mark)!!.fromDAO()
            chunk.content[JournalChunkPersonName(personDataDAO.surname, personDataDAO.name)] = mark
        }

        chunkList.add(chunk)
    }

    return chunkList
}