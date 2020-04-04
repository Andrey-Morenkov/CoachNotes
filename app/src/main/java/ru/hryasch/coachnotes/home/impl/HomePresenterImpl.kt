package ru.hryasch.coachnotes.home.impl

import com.pawegio.kandroid.i
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.channels.ReceiveChannel
import moxy.InjectViewState
import moxy.MvpPresenter
import org.koin.core.KoinComponent
import org.koin.core.get
import org.koin.core.inject
import org.koin.core.qualifier.named
import ru.hryasch.coachnotes.domain.group.data.Group
import ru.hryasch.coachnotes.domain.home.HomeInteractor
import ru.hryasch.coachnotes.domain.person.data.Person
import ru.hryasch.coachnotes.fragments.HomeView
import ru.hryasch.coachnotes.home.HomePresenter

@ExperimentalCoroutinesApi
@InjectViewState
class HomePresenterImpl: MvpPresenter<HomeView>(), HomePresenter, KoinComponent
{
    private val homeInteractor: HomeInteractor by inject()

    private val groupsRecvChannel: ReceiveChannel<List<Person>> = get(named("recvGroupsList"))
    private val peopleRecvChannel: ReceiveChannel<List<Group>>  = get(named("recvPeopleList"))

    private val subscriptions: Job = Job()

    init
    {
        loadingState()

        GlobalScope.launch(Dispatchers.Default)
        {
            val peopleCount = homeInteractor.getPeopleCount()
            withContext(Dispatchers.Main)
            {
                viewState.setPersonsCount(peopleCount)
                subscribeOnPeopleChanges()
            }
        }

        GlobalScope.launch(Dispatchers.Default)
        {
            val groupsCount = homeInteractor.getGroupCount()
            withContext(Dispatchers.Main)
            {
                viewState.setGroupsCount(groupsCount)
                subscribeOnGroupsChanges()
            }
        }

    }

    private fun loadingState()
    {
        viewState.setGroupsCount(null)
        viewState.setPersonsCount(null)
    }

    @ExperimentalCoroutinesApi
    private fun subscribeOnPeopleChanges()
    {
        GlobalScope.launch(Dispatchers.IO + subscriptions)
        {
            while (true)
            {
                i("channel <sendPeopleList>: WAITING TO RECEIVE")
                val newData = peopleRecvChannel.receive()
                i("channel <sendPeopleList>: RECEIVED")

                withContext(Dispatchers.Main)
                {
                    viewState.setPersonsCount(newData.size)
                }
            }
        }
    }

    @ExperimentalCoroutinesApi
    private fun subscribeOnGroupsChanges()
    {
        GlobalScope.launch(Dispatchers.IO + subscriptions)
        {
            while (true)
            {
                i("channel <sendGroupsList>: WAITING TO RECEIVE")
                val newData = groupsRecvChannel.receive()
                i("channel <sendGroupsList>: RECEIVED")

                withContext(Dispatchers.Main)
                {
                    viewState.setGroupsCount(newData.size)
                }
            }
        }
    }

    override fun onDestroy()
    {
        subscriptions.cancel()

        peopleRecvChannel.cancel()
        groupsRecvChannel.cancel()

        super.onDestroy()
    }
}