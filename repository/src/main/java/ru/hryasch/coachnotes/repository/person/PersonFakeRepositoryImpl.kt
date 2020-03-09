package ru.hryasch.coachnotes.repository.person

import io.realm.Realm
import io.realm.RealmConfiguration
import org.koin.core.KoinComponent
import ru.hryasch.coachnotes.domain.person.Person
import ru.hryasch.coachnotes.domain.repository.PersonRepository
import ru.hryasch.coachnotes.repository.common.GroupId
import ru.hryasch.coachnotes.repository.common.PersonId
import ru.hryasch.coachnotes.repository.dao.PersonDAO

class PersonFakeRepositoryImpl: PersonRepository, KoinComponent
{
    private val db: Realm

    init
    {
        generatePersonDb()

        val config = RealmConfiguration.Builder()
            .name("persons_fake")
            .inMemory()
            .build()

        db = Realm.getInstance(config)
    }

    private fun generatePersonDb()
    {
        for (i in 1..3)
        {
            val person = PersonDAO()
            person.groupId = 1
            person.id = i
            person.secondName = "Вася"
            person.firstName = "$i"

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
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}