package ru.hryasch.coachnotes.repository.dao

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

import ru.hryasch.coachnotes.repository.common.GroupId

sealed class JournalMarkDAO()
{
    companion object
    {
        fun fromString(str: String): JournalMarkDAO?
        {
            val options = str.split(" ")
            return when(options[0])
            {
                "p" -> JournalMarkPresence()
                "a" ->
                {
                    if (options.size == 1)
                    {
                        JournalMarkAbsence()
                    }
                    else
                    {
                        JournalMarkAbsence(options[1])
                    }
                }
                else -> null
            }
        }
    }
}

class JournalMarkPresence(): JournalMarkDAO()
{
    override fun toString(): String = "p"
}
class JournalMarkAbsence(val mark: String? = null): JournalMarkDAO()
{
    override fun toString(): String
    {
        var str = "a"
        mark?.let { str += " $mark" }
        return str
    }
}

open class JournalChunkDataDAO(): RealmObject()
{
    var name: String = ""
    var surname: String = ""
    var mark: String = ""
}

open class JournalChunkDAO(): RealmObject()
{
    @PrimaryKey
    var timestamp: String = ""
    var groupId: GroupId = 0
    var data: RealmList<JournalChunkDataDAO> = RealmList()
}