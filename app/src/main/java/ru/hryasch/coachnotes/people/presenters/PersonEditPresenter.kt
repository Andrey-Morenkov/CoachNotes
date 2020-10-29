package ru.hryasch.coachnotes.people.presenters

import ru.hryasch.coachnotes.domain.common.GroupId
import ru.hryasch.coachnotes.domain.person.data.Person

interface PersonEditPresenter
{
    fun applyInitialArgumentPersonAsync(person: Person?, lockGroup: GroupId?)

    fun applyPersonDataAsync(person: Person?, lockGroup: GroupId?)

    fun updateOrCreatePerson()

    fun deletePerson(person: Person)
}