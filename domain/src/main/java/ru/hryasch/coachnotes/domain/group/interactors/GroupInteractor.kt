package ru.hryasch.coachnotes.domain.group.interactors

import ru.hryasch.coachnotes.domain.group.data.Group

interface GroupInteractor
{
    suspend fun getGroupsList(): List<Group>
}