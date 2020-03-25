package ru.hryasch.coachnotes.groups.presenters.impl

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import moxy.InjectViewState
import moxy.MvpPresenter
import org.koin.core.KoinComponent
import org.koin.core.inject
import ru.hryasch.coachnotes.domain.common.GroupId
import ru.hryasch.coachnotes.domain.group.interactors.GroupInteractor
import ru.hryasch.coachnotes.fragments.api.GroupsView
import ru.hryasch.coachnotes.groups.presenters.GroupsPresenter

@InjectViewState
class GroupsPresenterImpl: MvpPresenter<GroupsView>(), GroupsPresenter, KoinComponent
{
    private val groupsInteractor: GroupInteractor by inject()

    init
    {
        loadingState()

        GlobalScope.launch(Dispatchers.Default)
        {
            val groupsList = groupsInteractor.getGroupsList()
            withContext(Dispatchers.Main)
            {
                viewState.setGroupsList(groupsList)
            }
        }
    }

    override fun onGroupClicked(groupId: GroupId)
    {
        TODO("Not yet implemented")
    }

    private fun loadingState()
    {
        viewState.setGroupsList(null)
    }
}