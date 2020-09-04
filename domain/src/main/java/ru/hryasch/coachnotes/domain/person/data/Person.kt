package ru.hryasch.coachnotes.domain.person.data

import ru.hryasch.coachnotes.domain.common.GroupId
import ru.hryasch.coachnotes.domain.common.PersonId
import java.io.Serializable
import java.time.LocalDate

interface Person: Comparable<Person>, Serializable
{
    // Required params
    val id: PersonId
    var name: String
    var surname: String
    var birthdayYear: Int

    // Optional params
    var patronymic: String?
    var fullBirthday: LocalDate?
    var groupId: GroupId?
    var isPaid: Boolean
    var deletedTimestamp: Long? // if person deleted, this is not null
    var relativeInfos: MutableList<RelativeInfo>

    override fun toString(): String
}