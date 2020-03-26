package ru.hryasch.coachnotes.domain.person.data

import ru.hryasch.coachnotes.domain.common.GroupId
import ru.hryasch.coachnotes.domain.common.PersonId
import ru.hryasch.coachnotes.domain.person.data.Person

data class PersonImpl(override var surname: String,
                      override var name: String,
                      override var id: PersonId = -1,
                      override var groupId: GroupId? = null,
                      override var isPaid: Boolean = false) : Person
{
    override fun compareTo(other: Person): Int
    {
        return "$surname $name $groupId $id".compareTo("${other.surname} ${other.name} ${other.groupId} ${other.id}")
    }

    override fun toString(): String = "$id: [$surname $name group = $groupId]"
}