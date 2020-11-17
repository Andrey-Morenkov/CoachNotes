package ru.hryasch.coachnotes.repository.converters

import com.pawegio.kandroid.i
import ru.hryasch.coachnotes.domain.person.data.ParentType
import ru.hryasch.coachnotes.domain.person.data.Person
import ru.hryasch.coachnotes.domain.person.data.PersonImpl
import ru.hryasch.coachnotes.domain.person.data.RelativeInfo
import ru.hryasch.coachnotes.repository.dao.DeletedPersonDAO
import ru.hryasch.coachnotes.repository.dao.PersonDAO
import ru.hryasch.coachnotes.repository.dao.RelativeInfoDAO
import java.lang.Exception
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.LinkedList

@JvmName("DAOPersonListConverter")
fun List<PersonDAO>.fromDAO(): List<Person>
{
    val personList: MutableList<Person> = LinkedList()
    this.forEach { personList.add(it.fromDao()) }

    return personList
}

@JvmName("DeletedDAOPersonListConverter")
fun List<DeletedPersonDAO>.fromDAO(): List<Person>
{
    val personList: MutableList<Person> = LinkedList()
    this.forEach { personList.add(it.fromDao()) }

    return personList
}



fun PersonDAO.fromDao(): Person
{
    val person = PersonImpl(this.id!!, this.surname!!, this.name!!, this.birthdayYear!!).apply {
        patronymic = this@fromDao.patronymic
        fullBirthday =
            try
            {
                LocalDate.parse(this@fromDao.fullBirthday, DateTimeFormatter.ofPattern(daoDateFormat))
            }
            catch (e: Exception)
            {
                null
            }

        groupId = this@fromDao.groupId
        isPaid = this@fromDao.isPaid
        deletedTimestamp = null
    }

    for (relativeInfo in this.relativeInfos)
    {
        person.relativeInfos.add(relativeInfo.fromDao())
    }

    return person
}

fun DeletedPersonDAO.fromDao(): Person
{
    val person = PersonImpl(this.id!!, this.surname!!, this.name!!, this.birthdayYear!!).apply {
        patronymic = this@fromDao.patronymic
        fullBirthday =
            try
            {
                LocalDate.parse(this@fromDao.fullBirthday, DateTimeFormatter.ofPattern(daoDateFormat))
            }
            catch (e: Exception)
            {
                null
            }

        groupId = this@fromDao.groupId
        isPaid = this@fromDao.isPaid
        deletedTimestamp = this@fromDao.deleteTimestamp
    }

    for (relativeInfo in this.relativeInfos)
    {
        person.relativeInfos.add(relativeInfo.fromDao())
    }

    return person
}



fun Person.toDao(): PersonDAO?
{
    if (deletedTimestamp != null)
    {
        return null
    }

    var birthDate: String? = null
    if (fullBirthday != null)
    {
        birthDate = this@toDao.fullBirthday!!.format(DateTimeFormatter.ofPattern(daoDateFormat))
    }

    val dao = PersonDAO(this.id, this.name, this.surname, this.birthdayYear).apply {
        patronymic = this@toDao.patronymic
        fullBirthday = birthDate
        groupId = this@toDao.groupId
        isPaid = this@toDao.isPaid
        deletedTimestamp = null
    }

    for (relativeInfo in this.relativeInfos)
    {
        dao.relativeInfos.add(relativeInfo.toDao())
    }

    return dao
}

fun Person.toDeletedDao(): DeletedPersonDAO?
{
    if (deletedTimestamp == null)
    {
        return null
    }

    return DeletedPersonDAO(this.toDao()!!, this.deletedTimestamp!!)
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
    relativeInfo.type = ParentType.getBySerializedName(this.type!!)

    i("extracting phones (${this.phones.size})")
    for (phone in this.phones)
    {
        relativeInfo.addPhone(phone)
    }

    return relativeInfo
}