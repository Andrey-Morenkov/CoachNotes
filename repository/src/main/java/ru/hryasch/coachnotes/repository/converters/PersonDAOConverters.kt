package ru.hryasch.coachnotes.repository.converters

import ru.hryasch.coachnotes.domain.person.Person
import ru.hryasch.coachnotes.domain.person.PersonImpl
import ru.hryasch.coachnotes.repository.dao.PersonDAO
import java.util.*

@JvmName("DAOPersonListConverter")
fun List<PersonDAO>.fromDAO(): List<Person>
{
    val personList: MutableList<Person> = LinkedList()

    this.forEach {
        personList.add(PersonImpl(it.surname!!, it.name!!, it.id, it.groupId))
    }

    return personList
}