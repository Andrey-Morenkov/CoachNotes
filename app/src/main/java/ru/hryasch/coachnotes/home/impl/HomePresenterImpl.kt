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
import ru.hryasch.coachnotes.fragments.HomeView
import ru.hryasch.coachnotes.home.HomePresenter
import java.util.Collections

@ExperimentalCoroutinesApi
@InjectViewState
class HomePresenterImpl: MvpPresenter<HomeView>(), HomePresenter, KoinComponent
{
    private val homeInteractor: HomeInteractor by inject()
    private val groupsRecvChannel: ReceiveChannel<List<Group>>  = get(named("recvGroupsList"))
    private val subscriptions: Job = Job()

    init
    {
        loadingState()

        GlobalScope.launch(Dispatchers.Default)
        {
            val groups = homeInteractor.getAllGroups()
            withContext(Dispatchers.Main)
            {
                viewState.setGroups(groups ?: Collections.emptyList())
                subscribeOnGroupsChanges()
            }
        }
    }

    override fun onDestroy()
    {
        groupsRecvChannel.cancel()
        subscriptions.cancel()

        super.onDestroy()
    }



    private fun loadingState()
    {
        viewState.setGroups(null)
    }

    @ExperimentalCoroutinesApi
    private fun subscribeOnGroupsChanges()
    {
        GlobalScope.launch(Dispatchers.IO + subscriptions)
        {
            while (true)
            {
                val newData = groupsRecvChannel.receive()
                d("HomePresenterImpl <AllGroups>: RECEIVED (count = ${newData.size})")

                withContext(Dispatchers.Main)
                {
                    viewState.setGroups(newData)
                }
            }
        }
    }
}