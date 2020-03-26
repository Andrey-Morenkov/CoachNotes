package ru.hryasch.coachnotes.people.presenters

import ru.hryasch.coachnotes.domain.common.PersonId

interface PeoplePresenter
{
    fun onPersonClicked(personId: PersonId)
}