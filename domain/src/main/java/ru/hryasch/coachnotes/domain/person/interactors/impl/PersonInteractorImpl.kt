package ru.hryasch.coachnotes.domain.person.interactors.impl

import org.koin.core.KoinComponent
import org.koin.core.inject
import org.koin.core.qualifier.named
import ru.hryasch.coachnotes.domain.common.GroupId
import ru.hryasch.coachnotes.domain.person.data.Person
import ru.hryasch.coachnotes.domain.person.interactors.PersonInteractor
import ru.hryasch.coachnotes.domain.repository.GroupRepository
import ru.hryasch.coachnotes.domain.repository.PersonRepository
import java.util.*
import kotlin.collections.HashMap

class PersonInteractorImpl: PersonInteractor, KoinComponent
{
    private val peopleRepository: PersonRepository by inject(named("mock"))
    private val groupRepository: GroupRepository by inject(named("mock"))

    override suspend fun getPeopleList(): List<Person>
    {
        return peopleRepository.getAllPeople() ?: LinkedList()
    }

    override suspend fun getGroupNames(): Map<GroupId, String>
    {
        val groupsList = groupRepository.getAllGroups()
        val res: MutableMap<GroupId, String> = HashMap()
        groupsList?.forEach {
            res[it.id] = it.name
        }
        return res
    }
}