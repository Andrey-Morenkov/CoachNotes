package ru.hryasch.coachnotes.domain.group.interactors.impl

import org.koin.core.KoinComponent
import org.koin.core.inject
import org.koin.core.qualifier.named
import ru.hryasch.coachnotes.domain.repository.GroupRepository
import ru.hryasch.coachnotes.domain.group.data.Group
import ru.hryasch.coachnotes.domain.group.interactors.GroupInteractor
import java.util.*

class GroupInteractorImpl: GroupInteractor, KoinComponent
{
    private val groupRepository: GroupRepository by inject(named("mock"))

    override suspend fun getGroupsList(): List<Group>
    {
        return groupRepository.getAllGroups() ?: LinkedList()
    }
}