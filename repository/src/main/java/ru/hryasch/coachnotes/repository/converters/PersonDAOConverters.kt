package ru.hryasch.coachnotes.repository.converters

import com.pawegio.kandroid.i
import com.soywiz.klock.parse
import ru.hryasch.coachnotes.domain.person.data.ParentType
import ru.hryasch.coachnotes.domain.person.data.Person
import ru.hryasch.coachnotes.domain.person.data.PersonImpl
import ru.hryasch.coachnotes.domain.person.data.RelativeInfo
import ru.hryasch.coachnotes.repository.dao.PersonDAO
import ru.hryasch.coachnotes.repository.dao.RelativeInfoDAO
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

    i("extracting relative infos (${this.relativeInfos.size})")
    for (relativeInfo in this.relativeInfos)
    {
        person.relativeInfos.add(relativeInfo.fromDao())
    }

    return person
}

fun Person.toDao(): PersonDAO
{
    val dao = PersonDAO(this.id, this.name, this.surname, this.birthday!!.format(daoDateFormat))

    dao.patronymic = this.patronymic
    dao.isPaid = isPaid
    dao.groupId = groupId

    i("adding relative infos (${this.relativeInfos.size})")
    for (relativeInfo in this.relativeInfos)
    {
        dao.relativeInfos.add(relativeInfo.toDao())
    }

    return dao
}

fun RelativeInfo.toDao(): RelativeInfoDAO
{
    val dao = RelativeInfoDAO(this.name, this.type.type)

    i("adding phones (${this.getPhones().size})")
    for (phone in this.getPhones())
    {
        dao.phones.add(phone)
    }

    return dao
}

fun RelativeInfoDAO.fromDao(): RelativeInfo
{
    val relativeInfo = RelativeInfo()
    relativeInfo.name = this.name!!
    relativeInfo.type = ParentType.valueOf(this.type!!)

    i("extracting phones (${this.phones.size})")
    for (phone in this.phones)
    {
        relativeInfo.addPhone(phone)
    }

    return relativeInfo
}