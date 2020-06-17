package ru.hryasch.coachnotes.domain.person.data

import com.soywiz.klock.Date
import ru.hryasch.coachnotes.domain.common.GroupId
import ru.hryasch.coachnotes.domain.common.PersonId
import java.io.Serializable

interface Person: Comparable<Person>, Serializable
{
    // Common params
    val id: PersonId
    var name: String
    var surname: String
    var patronymic: String?
    var birthday: Date?
    var groupId: GroupId?
    var isPaid: Boolean

    var relativeInfos: MutableList<RelativeInfo>

    override fun toString(): String
}