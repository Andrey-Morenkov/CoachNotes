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

    this.forEach { personList.add(it.fromDao()) }

    return personList
}

fun PersonDAO.fromDao(): Person
{
    val person = PersonImpl(this.surname!!, this.name!!, daoDateFormat.parse(this.birthday!!).local.date, this.id!!)
    person.isPaid = this.isPaid
    person.groupId = this.groupId
    person.patronymic = this.patronymic

    return person
}

fun Person.toDao(): PersonDAO
{
    val dao = PersonDAO(this.id, this.name, this.surname, this.birthday!!.format(daoDateFormat))

    dao.patronymic = this.patronymic
    dao.isPaid = isPaid
    dao.groupId = groupId

    return dao
}