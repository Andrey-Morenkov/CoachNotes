package ru.hryasch.coachnotes.groups.presenters.impl

import com.pawegio.kandroid.e
import com.pawegio.kandroid.i
import com.pawegio.kandroid.w
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
import ru.hryasch.coachnotes.domain.group.interactors.SimilarGroupFoundException
import ru.hryasch.coachnotes.fragments.GroupEditView
import ru.hryasch.coachnotes.groups.presenters.GroupEditPresenter

@InjectViewState
class GroupEditPresenterImpl: MvpPresenter<GroupEditView>(), GroupEditPresenter, KoinComponent
{
    private val groupInteractor: GroupInteractor by inject()

    private var currentGroup: Group? = null
    private lateinit var originalGroupName: String

    init
    {
        viewState.loadingState()
    }

    override fun applyInitialArgumentGroupAsync(group: Group?)
    {
        // for prevent unnecessary apply group when fragment re-create
        if (currentGroup != null)
        {
            e("return applyGroupDataAsync")
            return
        }

        w("call applyGroupDataAsync INITIAL $group")
        applyGroupDataAsync(group)
    }

    override fun applyGroupDataAsync(group: Group?)
    {
        GlobalScope.launch(Dispatchers.Default) {
            currentGroup = group ?: GroupImpl.generateNew()
            originalGroupName = currentGroup!!.name

            withContext(Dispatchers.Main)
            {
                i("group edit presenter setGroupData: $currentGroup")
                viewState.setGroupData(currentGroup!!)
            }
        }
    }

    override fun updateOrCreateGroup()
    {
        i("updateOrCreateGroup: $currentGroup")

        GlobalScope.launch(Dispatchers.Default)
        {
            if (originalGroupName == currentGroup!!.name)
            {
                // Group name wasn't changed, no need to check
                updateOrCreateGroupForced()
            }
            else
            {
                try
                {
                    groupInteractor.addOrUpdateGroup(currentGroup!!)
                    withContext(Dispatchers.Main)
                    {
                        viewState.updateOrCreateGroupFinished()
                    }
                }
                catch (e: SimilarGroupFoundException)
                {
                    withContext(Dispatchers.Main)
                    {
                        viewState.similarGroupFound(e.existGroup)
                    }
                }
            }
        }
    }

    override fun updateOrCreateGroupForced()
    {
        i("updateOrCreateGroupForced: $currentGroup")

        GlobalScope.launch(Dispatchers.Default)
        {
            groupInteractor.addOrUpdateGroupForced(currentGroup!!)
            withContext(Dispatchers.Main)
            {
                viewState.updateOrCreateGroupFinished()
            }
        }
    }

    override fun deleteGroupAndRemoveAllPeopleFromThisGroup(group: Group)
    {
        viewState.loadingState()

        GlobalScope.launch(Dispatchers.Default)
        {
            groupInteractor.deleteGroupAndRemoveAllPeopleFromThisGroup(group)

            withContext(Dispatchers.Main)
            {
                viewState.deleteGroupFinished()
            }
        }
    }

    override fun deleteGroupAndMoveAllPeopleToAnotherGroup(group: Group, targetGroup: Group)
    {
        // TODO
        //viewState.loadingState()
    }

    override fun deleteGroupAnDeleteAllPeople(group: Group)
    {
        // TODO
        //viewState.loadingState()
    }
}