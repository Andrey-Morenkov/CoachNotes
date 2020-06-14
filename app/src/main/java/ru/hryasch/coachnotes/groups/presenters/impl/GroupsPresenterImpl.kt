package ru.hryasch.coachnotes.groups.presenters.impl

import com.pawegio.kandroid.d
import com.pawegio.kandroid.i
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ReceiveChannel
import moxy.InjectViewState
import moxy.MvpPresenter
import org.koin.core.KoinComponent
import org.koin.core.get
import org.koin.core.inject
import org.koin.core.qualifier.named
import ru.hryasch.coachnotes.domain.common.GroupId
import ru.hryasch.coachnotes.domain.group.data.Group
import ru.hryasch.coachnotes.domain.group.interactors.GroupInteractor
import ru.hryasch.coachnotes.domain.person.data.Person
import ru.hryasch.coachnotes.fragments.GroupsView
import ru.hryasch.coachnotes.groups.presenters.GroupsPresenter

@ExperimentalCoroutinesApi
@InjectViewState
class GroupsPresenterImpl: MvpPresenter<GroupsView>(), GroupsPresenter, KoinComponent
{
    private val groupsInteractor: GroupInteractor by inject()

    private val groupsRecvChannel: ReceiveChannel<List<Group>> = get(named("recvGroupsList"))
    private val subscriptions: Job = Job()

    init
    {
        loadingState()

        GlobalScope.launch(Dispatchers.Default)
        {
            val groupsList = groupsInteractor.getGroupsList()
            groupsList.forEach {
                i("group info: $it")
            }

            withContext(Dispatchers.Main)
            {
                viewState.setGroupsList(groupsList)
                subscribeOnGroupsChanges()
            }
        }
    }

    private fun loadingState()
    {
        viewState.setGroupsList(null)
    }

    override fun onDestroy()
    {
        subscriptions.cancel()
        groupsRecvChannel.cancel()

        super.onDestroy()
    }

    @ExperimentalCoroutinesApi
    private fun subscribeOnGroupsChanges()
    {
        GlobalScope.launch(Dispatchers.IO + subscriptions)
        {
            while (true)
            {
                val newData = groupsRecvChannel.receive()
                d("GroupsPresenterImpl <AllGroups>: RECEIVED")

                withContext(Dispatchers.Main)
                {
                    viewState.setGroupsList(newData)
                }
            }
        }
    }
}