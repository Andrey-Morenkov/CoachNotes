package ru.hryasch.coachnotes.repository.dao

import com.soywiz.klock.Date
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

import ru.hryasch.coachnotes.repository.common.GroupId

sealed class JournalMarkDAO()
class JournalMarkPresence(): JournalMarkDAO()
class JournalMarkAbsence(val mark: String? = null): JournalMarkDAO()

data class JournalChunkDataDAO(val name: String, val mark: JournalMarkDAO)

open class JournalChunkDAO(): RealmObject()
{
    @PrimaryKey
    var timestamp: Date = Date(0)

    @PrimaryKey
    var groupId: GroupId = 0

    var data: RealmList<JournalChunkDataDAO> = RealmList()
}