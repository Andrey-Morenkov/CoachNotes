package ru.hryasch.coachnotes.people.presenters.impl

import kotlinx.coroutines.*
import moxy.InjectViewState
import moxy.MvpPresenter
import org.koin.core.KoinComponent
import org.koin.core.inject
import ru.hryasch.coachnotes.domain.common.PersonId
import ru.hryasch.coachnotes.domain.person.interactors.PersonInteractor
import ru.hryasch.coachnotes.fragments.api.PeopleView
import ru.hryasch.coachnotes.people.presenters.PeoplePresenter

@InjectViewState
class PeoplePresenterImpl: MvpPresenter<PeopleView>(), PeoplePresenter, KoinComponent
{
    private val peopleInteractor: PersonInteractor by inject()

    init
    {
        loadingState()

        val peopleList = GlobalScope.async { peopleInteractor.getPeopleList() }
        val groupNames = GlobalScope.async { peopleInteractor.getGroupNames() }

        GlobalScope.launch(Dispatchers.Main)
        {
            viewState.setPeopleList(peopleList.await(), groupNames.await())
        }
    }

    override fun onPersonClicked(personId: PersonId)
    {
        TODO("Not yet implemented")
    }

    private fun loadingState()
    {
        viewState.setPeopleList(null)
    }
}