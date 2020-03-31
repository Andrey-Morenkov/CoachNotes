package ru.hryasch.coachnotes.groups.presenters.impl

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import moxy.InjectViewState
import moxy.MvpPresenter
import org.koin.core.KoinComponent
import org.koin.core.inject
import ru.hryasch.coachnotes.domain.group.data.Group
import ru.hryasch.coachnotes.domain.group.data.GroupImpl
import ru.hryasch.coachnotes.domain.group.interactors.GroupInteractor
import ru.hryasch.coachnotes.fragments.GroupView
import ru.hryasch.coachnotes.groups.presenters.GroupPresenter

@InjectViewState
class GroupPresenterImpl : MvpPresenter<GroupView>(), GroupPresenter, KoinComponent
{
    private val groupInteractor: GroupInteractor by inject()

    private lateinit var currentGroup: Group

    init
    {
        viewState.loadingState()
    }

    override suspend fun applyGroupData(group: Group?)
    {
        currentGroup = group ?: GroupImpl(groupInteractor.getMaxGroupId() + 1, "")
        val groupNames = groupInteractor.getGroupNames()
        val groupMembers = groupInteractor.getPeopleListByGroup(currentGroup.id)

        withContext(Dispatchers.Main)
        {
            viewState.setGroupData(currentGroup, groupMembers, groupNames)
        }
    }

    override fun updateOrCreateGroup()
    {
        TODO("Not yet implemented")
    }
}