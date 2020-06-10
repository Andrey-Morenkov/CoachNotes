package ru.hryasch.coachnotes.people.presenters

import ru.hryasch.coachnotes.domain.person.data.Person

interface PersonPresenter
{
    fun applyPersonDataAsync(person: Person?)
}