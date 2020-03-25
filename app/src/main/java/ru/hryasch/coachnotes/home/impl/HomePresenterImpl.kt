package ru.hryasch.coachnotes.home.impl

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import moxy.InjectViewState
import moxy.MvpPresenter
import org.koin.core.KoinComponent
import org.koin.core.inject
import ru.hryasch.coachnotes.domain.home.HomeInteractor
import ru.hryasch.coachnotes.fragments.api.HomeView
import ru.hryasch.coachnotes.home.HomePresenter

@InjectViewState
class HomePresenterImpl: MvpPresenter<HomeView>(), HomePresenter, KoinComponent
{
    private val homeInteractor: HomeInteractor by inject()

    init
    {
        loadingState()

        GlobalScope.launch(Dispatchers.Default)
        {
            val peopleCount = homeInteractor.getPeopleCount()
            withContext(Dispatchers.Main)
            {
                viewState.setPersonsCount(peopleCount)
            }
        }

        GlobalScope.launch(Dispatchers.Default)
        {
            val groupsCount = homeInteractor.getGroupCount()
            withContext(Dispatchers.Main)
            {
                viewState.setGroupsCount(groupsCount)
            }
        }
    }

    private fun loadingState()
    {
        viewState.setGroupsCount(null)
        viewState.setPersonsCount(null)
    }
}