package ru.hryasch.coachnotes.groups.presenters.impl

import com.pawegio.kandroid.i
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ReceiveChannel
import moxy.InjectViewState
import moxy.MvpPresenter
import org.koin.core.KoinComponent
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

    init
    {
        viewState.loadingState()
    }

    @ExperimentalCoroutinesApi
    override suspend fun applyGroupData(group: Group?)
    {
        currentGroup = group ?: GroupImpl(groupInteractor.getMaxGroupId() + 1, "")
        groupNames = groupInteractor.getGroupNames()
        groupMembers = groupInteractor.getPeopleListByGroup(currentGroup.id)

        withContext(Dispatchers.Main)
        {
            viewState.setGroupData(currentGroup, groupMembers, groupNames)
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
}