package ru.hryasch.coachnotes.domain.person.data

import ru.hryasch.coachnotes.domain.common.GroupId
import ru.hryasch.coachnotes.domain.common.PersonId
import java.io.Serializable
import java.time.LocalDate

interface Person: Comparable<Person>, Serializable
{
    // Common params
    val id: PersonId
    var name: String
    var surname: String
    var patronymic: String?
    var birthday: LocalDate?
    var groupId: GroupId?
    var isPaid: Boolean

    var relativeInfos: MutableList<RelativeInfo>

    override fun toString(): String
}