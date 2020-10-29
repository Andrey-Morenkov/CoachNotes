package ru.hryasch.coachnotes.domain.person.data

import org.koin.core.KoinComponent
import org.koin.core.get
import org.koin.core.qualifier.named
import ru.hryasch.coachnotes.domain.common.GroupId
import ru.hryasch.coachnotes.domain.common.PersonId
import java.io.Serializable
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.LinkedList
import java.util.UUID

class PersonImpl (override val id: PersonId,
                  override var surname: String,
                  override var name: String,
                  override var birthdayYear: Int) : Person, Serializable
{
    override var fullBirthday: LocalDate? = null
    override var patronymic: String? = null
    override var isPaid: Boolean = false
    override var groupId: GroupId? = null
    override var relativeInfos: MutableList<RelativeInfo> = LinkedList()
    override var deletedTimestamp: Long? = null

    companion object: KoinComponent
    {
        fun generateNew(): PersonImpl
        {
            val id: UUID = get(named("personUUID"))
            return PersonImpl(id.toString(), "", "", 0)
        }
    }

    override fun compareTo(other: Person): Int
    {
        if (surname != other.surname)
        {
            return surname.compareTo(other.surname)
        }

        if (name != other.name)
        {
            return name.compareTo(other.name)
        }

        if (birthdayYear != other.birthdayYear)
        {
            return birthdayYear.compareTo(other.birthdayYear)
        }

        return id.compareTo(other.id)
    }

    override fun toString(): String = "Person[$id]: ($surname $name $patronymic ${fullBirthday?.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))} isPaid = $isPaid group = $groupId relativeInfos: $relativeInfos)"
}