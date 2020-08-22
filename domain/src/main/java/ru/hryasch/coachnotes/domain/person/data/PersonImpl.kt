package ru.hryasch.coachnotes.domain.person.data

import org.koin.core.KoinComponent
import org.koin.core.get
import org.koin.core.qualifier.named
import ru.hryasch.coachnotes.domain.common.GroupId
import ru.hryasch.coachnotes.domain.common.PersonId
import ru.hryasch.coachnotes.domain.group.data.GroupImpl
import java.io.Serializable
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class PersonImpl private constructor(override val id: PersonId,
                                     override var surname: String,
                                     override var name: String,
                                     override var birthday: LocalDate? = null) : Person, Serializable
{
    override var patronymic: String? = null
    override var isPaid: Boolean = false
    override var groupId: GroupId? = null
    override var relativeInfos: MutableList<RelativeInfo> = LinkedList()

    companion object: KoinComponent
    {
        fun generateNew(): PersonImpl
        {
            val id: UUID = get(named("personUUID"))
            return PersonImpl(id.toString(), "", "")
        }
    }

    override fun compareTo(other: Person): Int
    {
        return id.compareTo(other.id)
    }

    override fun toString(): String = "Person[$id]: ($surname $name $patronymic ${birthday?.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))} isPaid = $isPaid group = $groupId relativeInfos: $relativeInfos)"
}