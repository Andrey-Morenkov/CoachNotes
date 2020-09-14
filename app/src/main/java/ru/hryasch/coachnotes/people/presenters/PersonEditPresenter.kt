package ru.hryasch.coachnotes.people.presenters

import ru.hryasch.coachnotes.domain.person.data.Person

interface PersonEditPresenter: PersonPresenter
{
    fun updateOrCreatePerson()

    fun deletePerson(person: Person)
}