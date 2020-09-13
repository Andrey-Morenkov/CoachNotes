package ru.hryasch.coachnotes.groups.presenters.impl

import com.pawegio.kandroid.d
import com.pawegio.kandroid.e
import com.pawegio.kandroid.i
import com.pawegio.kandroid.w
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

    private var currentGroup: Group? = null
    private lateinit var groupNames: Map<GroupId, String>
    private lateinit var groupMembers: List<Person>

    private lateinit var specificGroupChannel: ReceiveChannel<Group>
    private val subscriptions: Job = Job()

    init
    {
        viewState.loadingState()
    }

    @ExperimentalCoroutinesApi
    override fun applyInitialArgumentGroupAsync(group: Group?)
    {
        // for prevent unnecessary apply group when fragment re-create
        e("try applyInitialArgumentGroupAsync")
        if (currentGroup != null)
        {
            e("return applyGroupDataAsync")
            return
        }
        w("call applyGroupDataAsync INITIAL")
        applyGroupDataAsync(group)
    }

    @ExperimentalCoroutinesApi
    override fun applyGroupDataAsync(group: Group?)
    {
        i("applyGroupDataAsync: $group")
        GlobalScope.launch(Dispatchers.Default)
        {
            currentGroup = group!!
            groupNames = groupInteractor.getGroupNames()
            groupMembers = groupInteractor.getPeopleListByGroup(currentGroup!!.id)

            withContext(Dispatchers.Main)
            {
                i("group presenter setGroupData: $currentGroup")
                viewState.setGroupData(currentGroup!!, groupMembers, groupNames)
                if (!::specificGroupChannel.isInitialized)
                {
                    specificGroupChannel = get(named("recvSpecificGroup")) { parametersOf(currentGroup!!.id) }
                    subscribeOnGroupChanges()
                    i("subscribed for group[${currentGroup!!.id}]")
                }
            }
        }
    }

    override fun deletePersonFromCurrentGroup(personId: PersonId)
    {
        GlobalScope.launch(Dispatchers.Default)
        {
            peopleInteractor.deletePersonFromGroup(personId, currentGroup!!.id)
        }
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

    override fun addPeopleToNewGroup(people: List<Person>)
    {
        people.forEach {
            i("addPeopleToGroup: $it -> $currentGroup")
        }

        GlobalScope.launch(Dispatchers.Default)
        {
            peopleInteractor.addOrUpdatePeople(people)
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
                d("GroupPresenterImpl <Group[${currentGroup!!.id}]>: RECEIVED $newData")

                withContext(Dispatchers.Main)
                {
                    w("call applyGroupDataAsync CHANNEL")
                    applyGroupDataAsync(newData)
                }
            }
        }
    }
}