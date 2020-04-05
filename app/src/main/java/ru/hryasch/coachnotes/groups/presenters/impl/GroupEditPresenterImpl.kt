package ru.hryasch.coachnotes.groups.presenters.impl

import com.pawegio.kandroid.i
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import moxy.InjectViewState
import moxy.MvpPresenter
import org.koin.core.KoinComponent
import org.koin.core.inject
import ru.hryasch.coachnotes.domain.group.data.Group
import ru.hryasch.coachnotes.domain.group.data.GroupImpl
import ru.hryasch.coachnotes.domain.group.interactors.GroupInteractor
import ru.hryasch.coachnotes.fragments.GroupEditView
import ru.hryasch.coachnotes.groups.presenters.GroupEditPresenter

@InjectViewState
class GroupEditPresenterImpl: MvpPresenter<GroupEditView>(), GroupEditPresenter, KoinComponent
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

        withContext(Dispatchers.Main)
        {
            viewState.setGroupData(currentGroup)
        }
    }

    override fun updateOrCreateGroup()
    {
        i("updateOrCreateGroup: $currentGroup")

        GlobalScope.launch(Dispatchers.Main)
        {
            groupInteractor.addOrUpdateGroup(currentGroup)

            withContext(Dispatchers.Main)
            {
                viewState.updateOrCreateGroupFinished()
            }
        }
    }

    override fun deleteGroup(group: Group)
    {
        viewState.loadingState()

        GlobalScope.launch(Dispatchers.Main)
        {
            groupInteractor.deleteGroup(group)

            withContext(Dispatchers.Main)
            {
                viewState.deleteGroupFinished()
            }
        }
    }
}