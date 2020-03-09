package ru.hryasch.coachnotes.repository.person

import io.realm.Realm
import io.realm.kotlin.where
import org.koin.core.KoinComponent
import org.koin.core.get
import org.koin.core.qualifier.named
import ru.hryasch.coachnotes.domain.person.Person
import ru.hryasch.coachnotes.domain.repository.PersonRepository
import ru.hryasch.coachnotes.repository.common.GroupId
import ru.hryasch.coachnotes.repository.common.PersonId
import ru.hryasch.coachnotes.repository.converters.fromDAO
import ru.hryasch.coachnotes.repository.dao.PersonDAO
import java.util.*

class PersonFakeRepositoryImpl: PersonRepository, KoinComponent
{
    init
    {
        generatePersonDb()
    }

    private fun generatePersonDb()
    {
        val db = getDb()

        for (i in 1..3)
        {
            val person = PersonDAO()
            person.groupId = 1
            person.id = i
            person.surname = "Фамилия$i"
            person.name = "Имя$i"

            db.executeTransaction {
                it.copyToRealm(person)
            }
        }
    }

    override suspend fun getPerson(person: PersonId): Person?
    {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun getPersonsByGroup(group: GroupId): List<Person>?
    {
        val db = getDb()

        val personsList: MutableList<Person> = LinkedList()
        val personsDao = db.where<PersonDAO>()
                        .equalTo("groupId", 1.toInt())
                        .findAll()
        personsDao.forEach {
            personsList.add(it.fromDAO())
        }

        return personsList
    }

    private fun getDb(): Realm = Realm.getInstance(get(named("persons_mock")))
}