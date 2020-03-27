package ru.hryasch.coachnotes.people.presenters

import ru.hryasch.coachnotes.domain.person.data.Person

interface PersonPresenter
{
    suspend fun applyPersonData(person: Person?)

    fun updateOrCreatePerson()
}