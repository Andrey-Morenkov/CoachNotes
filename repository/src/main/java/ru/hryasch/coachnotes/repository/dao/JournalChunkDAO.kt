package ru.hryasch.coachnotes.repository.dao

import com.pawegio.kandroid.d
import com.pawegio.kandroid.i
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required
import ru.hryasch.coachnotes.domain.journal.data.CellData
import ru.hryasch.coachnotes.domain.journal.data.ChunkPersonName

import ru.hryasch.coachnotes.repository.common.GroupId
import ru.hryasch.coachnotes.repository.common.PersonId
import ru.hryasch.coachnotes.repository.converters.daoDateFormat
import ru.hryasch.coachnotes.repository.converters.toDAO
import java.time.LocalDate
import java.time.format.DateTimeFormatter


open class JournalChunkDataDAO(): RealmObject()
{
    var personId: PersonId? = null
    var name: String = ""
    var surname: String = ""
    var mark: String = ""

    constructor(personInfo: ChunkPersonName, mark: CellData): this()
    {
        this.personId = personInfo.personId
        this.name = personInfo.name
        this.surname = personInfo.surname
        this.mark = mark.toDAO().serialize()
    }
}

data class JournalChunkDAOId(val date: LocalDate, val groupId: GroupId)
{
    companion object
    {
        private const val delimiter = "♦"

        fun getSerialized(date: LocalDate, groupId: GroupId): String
        {
            return "${date.format(DateTimeFormatter.ofPattern(daoDateFormat))}$delimiter$groupId"
        }

        fun deserialize(str: String): JournalChunkDAOId
        {
            d("deserialize chunkDAO id: $str")
            val components = str.split(delimiter)
            return JournalChunkDAOId(LocalDate.parse(components[0], DateTimeFormatter.ofPattern(daoDateFormat)), components[1])
        }
    }
}

open class JournalChunkDAO(): RealmObject()
{
    @PrimaryKey
    @Required
    var id: String? = null
    var data: RealmList<JournalChunkDataDAO> = RealmList()

    constructor(date: LocalDate, groupId: GroupId) : this()
    {
        id = JournalChunkDAOId.getSerialized(date, groupId)
        i("created new chunk: id = $id")
    }

    fun isEmpty() = data.isEmpty()
}