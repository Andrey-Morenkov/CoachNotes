package ru.hryasch.coachnotes.domain.person.data

import com.soywiz.klock.Date
import ru.hryasch.coachnotes.domain.common.GroupId
import ru.hryasch.coachnotes.domain.common.PersonId

data class PersonImpl(override var surname: String,
                      override var name: String,
                      override var birthday: Date? = null,
                      override val id: PersonId = -1) : Person
{
    override var patronymic: String? = null
    override var isPaid: Boolean = false
    override var groupId: GroupId? = null
    override var parentFullName: String? = null
    override var parentPhone: String? = null

    override fun compareTo(other: Person): Int
    {
        return "$surname $name $groupId $id".compareTo("${other.surname} ${other.name} ${other.groupId} ${other.id}")
    }

    override fun toString(): String = "$id: [$surname $name $patronymic ${birthday?.format("dd.MM.yyyy")} isPaid = $isPaid group = $groupId]"
}