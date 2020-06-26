package ru.hryasch.coachnotes.domain.person.data

import ru.hryasch.coachnotes.domain.common.GroupId
import ru.hryasch.coachnotes.domain.common.PersonId
import java.io.Serializable
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

data class PersonImpl(override var surname: String,
                      override var name: String,
                      override var birthday: LocalDate? = null,
                      override val id: PersonId = -1) : Person, Serializable
{
    override var patronymic: String? = null
    override var isPaid: Boolean = false
    override var groupId: GroupId? = null
    override var relativeInfos: MutableList<RelativeInfo> = LinkedList()

    override fun compareTo(other: Person): Int
    {
        return "$surname $name $groupId $id".compareTo("${other.surname} ${other.name} ${other.groupId} ${other.id}")
    }

    override fun toString(): String = "Person[$id]: ($surname $name $patronymic ${birthday?.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))} isPaid = $isPaid group = $groupId relativeInfos: $relativeInfos)"
}