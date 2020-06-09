package ru.hryasch.coachnotes.domain.home

import ru.hryasch.coachnotes.domain.group.data.Group

interface HomeInteractor
{
    suspend fun getPeopleCount(): Int
    suspend fun getAllGroups(): List<Group>?
}