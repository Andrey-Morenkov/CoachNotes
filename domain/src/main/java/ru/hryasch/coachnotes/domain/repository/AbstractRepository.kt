package ru.hryasch.coachnotes.domain.repository

interface AbstractRepository
{
    suspend fun closeDb()
}