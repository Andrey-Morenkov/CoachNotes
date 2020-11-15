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

    private var originalGroup: Group? = null
    private var editingGroup: Group? = null

    init
    {
        viewState.loadingState()
    }

    override fun applyInitialArgumentGroupAsync(group: Group?)
    {
        // for prevent unnecessary apply group when fragment re-create
        if (editingGroup != null)
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
            originalGroup = group
            editingGroup = originalGroup?.copy() ?: GroupImpl.generateNew()

            withContext(Dispatchers.Main)
            {
                i("group edit presenter setGroupData: $editingGroup")
                viewState.setGroupData(editingGroup!!)
            }
        }
    }

    override fun updateOrCreateGroup()
    {
        i("updateOrCreateGroup: $editingGroup")

        GlobalScope.launch(Dispatchers.Default)
        {
            if (editingGroup!!.name == (originalGroup?.name ?: ""))
            {
                // Group name wasn't changed, no need to check
                updateOrCreateGroupForced()
            }
            else
            {
                try
                {
                    groupInteractor.addOrUpdateGroup(editingGroup!!)
                    withContext(Dispatchers.Main)
                    {
                        originalGroup?.applyData(editingGroup!!)
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
        i("updateOrCreateGroupForced: $editingGroup")

        GlobalScope.launch(Dispatchers.Default)
        {
            groupInteractor.addOrUpdateGroupForced(editingGroup!!)
            withContext(Dispatchers.Main)
            {
                originalGroup?.applyData(editingGroup!!)
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