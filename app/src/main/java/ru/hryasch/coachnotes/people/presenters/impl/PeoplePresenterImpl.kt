package ru.hryasch.coachnotes.people.presenters.impl

import com.pawegio.kandroid.d
import com.pawegio.kandroid.e
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ReceiveChannel
import moxy.InjectViewState
import moxy.MvpPresenter
import org.koin.core.KoinComponent
import org.koin.core.get
import org.koin.core.inject
import org.koin.core.qualifier.named
import ru.hryasch.coachnotes.domain.common.PersonId
import ru.hryasch.coachnotes.domain.group.data.Group
import ru.hryasch.coachnotes.domain.person.data.Person
import ru.hryasch.coachnotes.domain.person.interactors.PersonInteractor
import ru.hryasch.coachnotes.fragments.PeopleView
import ru.hryasch.coachnotes.people.presenters.PeoplePresenter
import java.util.stream.Collectors

@ExperimentalCoroutinesApi
@InjectViewState
class PeoplePresenterImpl: MvpPresenter<PeopleView>(), PeoplePresenter, KoinComponent
{
    private val peopleInteractor: PersonInteractor by inject()

    private val peopleRecvChannel: ReceiveChannel<List<Person>> = get(named("recvPeopleList"))
    private val groupsRecvChannel: ReceiveChannel<List<Group>> = get(named("recvGroupsList"))
    private val subscriptions: Job = Job()

    init
    {
        e("init people presenter")
        loadingState()

        val peopleList = GlobalScope.async { peopleInteractor.getPeopleList() }
        val groupNames = GlobalScope.async { peopleInteractor.getGroupNames() }

        GlobalScope.launch(Dispatchers.Main)
        {
            viewState.setPeopleList(peopleList.await().sorted(), groupNames.await())
            subscribeOnPeopleChanges()
            subscribeOnGroupChanges()
        }
    }

    private fun loadingState()
    {
        viewState.setPeopleList(null, null)
    }

    override fun onDestroy()
    {
        subscriptions.cancel()
        peopleRecvChannel.cancel()

        super.onDestroy()
    }

    @ExperimentalCoroutinesApi
    private fun subscribeOnPeopleChanges()
    {
        GlobalScope.launch(Dispatchers.IO + subscriptions)
        {
            while (true)
            {
                val newData = peopleRecvChannel.receive()
                d("PeoplePresenterImpl <AllPeople>: RECEIVED $newData")

                withContext(Dispatchers.Main)
                {
                    viewState.setPeopleList(newData.sorted())
                }
            }
        }
    }

    @ExperimentalCoroutinesApi
    private fun subscribeOnGroupChanges()
    {
        GlobalScope.launch(Dispatchers.IO + subscriptions)
        {
            while (true)
            {
                val newData = groupsRecvChannel.receive()
                d("PeoplePresenterImpl <AllGroups>: RECEIVED $newData")

                withContext(Dispatchers.Main)
                {
                    val groupsMap = newData.parallelStream().collect(Collectors.toMap(Group::id, Group::name))
                    viewState.setPeopleList(null, groupsMap)
                }
            }
        }
    }
}