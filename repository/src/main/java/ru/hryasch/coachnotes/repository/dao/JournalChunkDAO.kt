package ru.hryasch.coachnotes.repository.dao

import com.pawegio.kandroid.i
import com.soywiz.klock.Date
import com.soywiz.klock.parse
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.Index
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required
import ru.hryasch.coachnotes.domain.journal.data.CellData
import ru.hryasch.coachnotes.domain.journal.data.ChunkPersonName
import ru.hryasch.coachnotes.domain.person.data.Person

import ru.hryasch.coachnotes.repository.common.GroupId
import ru.hryasch.coachnotes.repository.converters.daoDateFormat
import ru.hryasch.coachnotes.repository.converters.toDAO


open class JournalChunkDataDAO(): RealmObject()
{
    var name: String = ""
    var surname: String = ""
    var mark: String = ""

    constructor(surname: String, name: String, mark: JournalMarkDAO) : this()
    {
        this.name = name
        this.surname = surname
        this.mark = mark.serialize()
    }

    constructor(surname: String, name: String, mark: CellData) : this()
    {
        this.name = name
        this.surname = surname
        this.mark = mark.toDAO().serialize()
    }

    constructor(personInfo: Person, mark: CellData): this()
    {
        this.name = personInfo.name
        this.surname = personInfo.surname
        this.mark = mark.toDAO().serialize()
    }

    constructor(personInfo: ChunkPersonName, mark: CellData): this()
    {
        this.name = personInfo.name
        this.surname = personInfo.surname
        this.mark = mark.toDAO().serialize()
    }
}

data class JournalChunkDAOId (val date: Date, val groupId: GroupId)
{
    companion object
    {
        private const val delimiter = "/"

        fun getSerialized(date: Date, groupId: GroupId): String
        {
            return "${date.format(daoDateFormat)}$delimiter$groupId"
        }

        fun getSerialized(date: String, groupId: GroupId): String
        {
            return "$date$delimiter$groupId"
        }

        fun deserialize(str: String): JournalChunkDAOId
        {
            val groupIdStr = str.reversed()
                                .substringBefore(delimiter)
                                .reversed()

            val date = str.substringBefore("$delimiter$groupIdStr")
            return JournalChunkDAOId(daoDateFormat.parse(date).local.date, groupIdStr.toInt())
        }
    }
}

open class JournalChunkDAO(): RealmObject()
{
    @Index
    @Required
    @PrimaryKey
    var id: String? = null
    var data: RealmList<JournalChunkDataDAO> = RealmList()

    constructor(date: Date, groupId: GroupId) : this()
    {
        id = JournalChunkDAOId.getSerialized(date, groupId)
        i("created new chunk: id = $id")
    }

    fun isEmpty() = data.isEmpty()
}