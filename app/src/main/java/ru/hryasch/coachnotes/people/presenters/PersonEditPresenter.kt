package ru.hryasch.coachnotes.people.presenters

interface PersonEditPresenter: PersonPresenter
{
    fun updateOrCreatePerson()
}