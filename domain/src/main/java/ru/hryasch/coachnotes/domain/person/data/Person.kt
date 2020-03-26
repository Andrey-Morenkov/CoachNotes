package ru.hryasch.coachnotes.domain.person.data

import ru.hryasch.coachnotes.domain.common.GroupId
import ru.hryasch.coachnotes.domain.common.PersonId

interface Person: Comparable<Person>
{
    val id: PersonId
    var name: String
    var surname: String
    var groupId: GroupId?
    var isPaid: Boolean

    override fun toString(): String
}