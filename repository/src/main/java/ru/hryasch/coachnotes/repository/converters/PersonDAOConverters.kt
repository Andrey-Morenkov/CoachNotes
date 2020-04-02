package ru.hryasch.coachnotes.repository.converters

import com.soywiz.klock.parse
import ru.hryasch.coachnotes.domain.person.data.Person
import ru.hryasch.coachnotes.domain.person.data.PersonImpl
import ru.hryasch.coachnotes.repository.dao.PersonDAO
import java.util.*

@JvmName("DAOPersonListConverter")
fun List<PersonDAO>.fromDAO(): List<Person>
{
    val personList: MutableList<Person> = LinkedList()

    this.forEach {
        personList.add(PersonImpl(it.surname!!, it.name!!, daoDateFormat.parse(it.birthday!!).local.date, it.id!!)
            .apply {
                isPaid = it.isPaid
                groupId = it.groupId
            })
    }

    return personList
}

fun Person.toDao(): PersonDAO
{
    val dao = PersonDAO(this.id, this.name, this.surname, this.birthday!!.format(daoDateFormat))

    dao.isPaid = isPaid
    dao.groupId = groupId

    return dao
}