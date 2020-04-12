package ru.hryasch.coachnotes.domain.home.impl

import org.koin.core.KoinComponent
import org.koin.core.inject
import org.koin.core.qualifier.named
import ru.hryasch.coachnotes.domain.group.data.Group
import ru.hryasch.coachnotes.domain.home.HomeInteractor
import ru.hryasch.coachnotes.domain.repository.GroupRepository
import ru.hryasch.coachnotes.domain.repository.PersonRepository

class HomeInteractorImpl: HomeInteractor, KoinComponent
{
    private val personRepository: PersonRepository by inject(named("mock"))
    private val groupRepository: GroupRepository by inject(named("mock"))

    override suspend fun getGroupCount(): Int
    {
        var count = 0
        val groups = groupRepository.getAllGroups()
        groups?.let { count = it.size }
        return count
    }

    override suspend fun getPeopleCount(): Int
    {
        var count = 0
        val groups = personRepository.getAllPeople()
        groups?.let { count = it.size }
        return count
    }

    override suspend fun getAllGroups(): List<Group>?
    {
        return groupRepository.getAllGroups()
    }
}