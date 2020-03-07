package ru.hryasch.coachnotes.repository.person.impl

import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.kotlin.where
import org.koin.core.KoinComponent
import ru.hryasch.coachnotes.repository.common.GroupId
import ru.hryasch.coachnotes.repository.common.PersonId
import ru.hryasch.coachnotes.repository.dao.PersonDAO
import ru.hryasch.coachnotes.repository.person.PersonRepository

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

    override fun getPerson(person: PersonId): PersonDAO
    {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getPersonsByGroup(group: GroupId): List<PersonDAO>?
    {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}