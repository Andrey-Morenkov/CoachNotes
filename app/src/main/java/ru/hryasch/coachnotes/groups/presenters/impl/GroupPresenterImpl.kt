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
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
import ru.hryasch.coachnotes.domain.common.GroupId
import ru.hryasch.coachnotes.domain.common.PersonId
import ru.hryasch.coachnotes.domain.group.data.Group
import ru.hryasch.coachnotes.domain.group.data.GroupImpl
import ru.hryasch.coachnotes.domain.group.interactors.GroupInteractor
import ru.hryasch.coachnotes.domain.person.data.Person
import ru.hryasch.coachnotes.domain.person.interactors.PersonInteractor
import ru.hryasch.coachnotes.fragments.GroupView
import ru.hryasch.coachnotes.groups.presenters.GroupPresenter

@InjectViewState
class GroupPresenterImpl : MvpPresenter<GroupView>(), GroupPresenter, KoinComponent
{
    private val groupInteractor: GroupInteractor by inject()
    private val peopleInteractor: PersonInteractor by inject()

    private lateinit var currentGroup: Group
    private lateinit var groupNames: Map<GroupId, String>
    private lateinit var groupMembers: List<Person>

    private lateinit var specificGroupChannel: ReceiveChannel<Group>
    private val subscriptions: Job = Job()

    init
    {
        viewState.loadingState()
    }

    @ExperimentalCoroutinesApi
    override fun applyGroupDataAsync(group: Group?)
    {
        GlobalScope.launch(Dispatchers.Default)
        {
            currentGroup = group!!
            groupNames = groupInteractor.getGroupNames()
            groupMembers = groupInteractor.getPeopleListByGroup(currentGroup.id)

            withContext(Dispatchers.Main)
            {
                viewState.setGroupData(currentGroup, groupMembers, groupNames)
                if (!::specificGroupChannel.isInitialized)
                {
                    specificGroupChannel = get(named("recvSpecificGroup")) { parametersOf(currentGroup.id) }
                    subscribeOnGroupChanges()
                    i("subscribed for group[${currentGroup.id}]")
                }
            }
        }
    }

    override fun onDeletePersonFromCurrentGroupClicked(person: Person)
    {
        viewState.showDeletePersonFromGroupNotification(person)
    }

    override fun deletePersonFromCurrentGroup(personId: PersonId)
    {
        GlobalScope.launch(Dispatchers.Default)
        {
            peopleInteractor.deletePersonFromGroup(personId, currentGroup.id)
        }

        viewState.showDeletePersonFromGroupNotification(null)
    }

    override fun onAddPeopleToGroupClicked()
    {
        GlobalScope.launch(Dispatchers.Default)
        {
            val peopleWithoutGroup = peopleInteractor.getPeopleWithoutGroup()?.sorted()

            withContext(Dispatchers.Main)
            {
                viewState.showAddPeopleToGroupNotification(peopleWithoutGroup)
            }
        }
    }

    override fun addPeopleToGroup(people: List<Person>)
    {
        people.forEach {
            i("addPeopleToGroup: $it -> ${currentGroup.id}")
        }

        GlobalScope.launch(Dispatchers.Default)
        {
            people.forEach {
                peopleInteractor.addOrUpdatePerson(it)
            }
            groupInteractor.addOrUpdateGroup(currentGroup)
        }

        viewState.showAddPeopleToGroupNotification(null)
    }

    override fun onDestroy()
    {
        subscriptions.cancel()
        specificGroupChannel.cancel()

        super.onDestroy()
    }

    @ExperimentalCoroutinesApi
    private fun subscribeOnGroupChanges()
    {
        GlobalScope.launch(Dispatchers.IO + subscriptions)
        {
            while (true)
            {
                val newData = specificGroupChannel.receive()
                d("GroupPresenterImpl <recvSpecificGroup[${currentGroup.id}]>: RECEIVED")

                withContext(Dispatchers.Main)
                {
                    applyGroupDataAsync(newData)
                }
            }
        }
    }
}