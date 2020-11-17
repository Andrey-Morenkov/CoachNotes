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
    private val personRepository: PersonRepository by inject(named("release"))
    private val groupRepository: GroupRepository by inject(named("release"))

    override suspend fun getPeopleCount(): Int
    {
        var count = 0
        val groups = personRepository.getAllExistingPeople()
        groups?.let { count = it.size }
        return count
    }

    override suspend fun getAllGroups(): List<Group>?
    {
        return groupRepository.getAllExistingGroups()
    }
}