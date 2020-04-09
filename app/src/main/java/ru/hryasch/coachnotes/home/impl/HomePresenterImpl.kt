package ru.hryasch.coachnotes.home.impl

import com.pawegio.kandroid.d
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

    private val groupsRecvChannel: ReceiveChannel<List<Group>> = get(named("recvGroupsList"))
    private val peopleRecvChannel: ReceiveChannel<List<Person>>  = get(named("recvPeopleList"))

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

    override fun onDestroy()
    {
        subscriptions.cancel()

        peopleRecvChannel.cancel()
        groupsRecvChannel.cancel()

        super.onDestroy()
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
                val newData = peopleRecvChannel.receive()
                d("HomePresenterImpl <sendPeopleList>: RECEIVED")

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
                val newData = groupsRecvChannel.receive()
                d("HomePresenterImpl <sendGroupsList>: RECEIVED")

                withContext(Dispatchers.Main)
                {
                    viewState.setGroupsCount(newData.size)
                }
            }
        }
    }
}