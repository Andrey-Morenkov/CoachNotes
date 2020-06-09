package ru.hryasch.coachnotes.home.impl

import com.pawegio.kandroid.d
import kotlinx.coroutines.*
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

    private val groupsRecvChannel: ReceiveChannel<List<Group>>  = get(named("recvGroupsList"))
    private val peopleRecvChannel: ReceiveChannel<List<Person>> = get(named("recvPeopleList"))

    private val subscriptions: Job = Job()

    init
    {
        loadingState()

        GlobalScope.launch(Dispatchers.Default)
        {
            val peopleCount = homeInteractor.getPeopleCount()
            withContext(Dispatchers.Main)
            {
                viewState.setPeopleCount(peopleCount)
                subscribeOnPeopleChanges()
            }
        }

        GlobalScope.launch(Dispatchers.Default)
        {
            val groups = homeInteractor.getAllGroups()
            withContext(Dispatchers.Main)
            {
                viewState.setGroups(groups)
                subscribeOnGroupsChanges()
            }
        }
    }

    override fun onDestroy()
    {
        peopleRecvChannel.cancel()
        groupsRecvChannel.cancel()

        subscriptions.cancel()

        super.onDestroy()
    }



    private fun loadingState()
    {
        viewState.setGroups(null)
        viewState.setPeopleCount(null)
    }

    @ExperimentalCoroutinesApi
    private fun subscribeOnPeopleChanges()
    {
        GlobalScope.launch(Dispatchers.IO + subscriptions)
        {
            while (true)
            {
                val newData = peopleRecvChannel.receive()
                d("HomePresenterImpl <PeopleList>: RECEIVED (count = ${newData.size})")

                withContext(Dispatchers.Main)
                {
                    viewState.setPeopleCount(newData.size)
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
                d("HomePresenterImpl <GroupsList>: RECEIVED (count = ${newData.size})")

                withContext(Dispatchers.Main)
                {
                    viewState.setGroups(newData)
                }
            }
        }
    }
}