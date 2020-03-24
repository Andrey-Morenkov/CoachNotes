package ru.hryasch.coachnotes.domain.home

interface HomeInteractor
{
    suspend fun getGroupCount(): Int
    suspend fun getPeopleCount(): Int
}