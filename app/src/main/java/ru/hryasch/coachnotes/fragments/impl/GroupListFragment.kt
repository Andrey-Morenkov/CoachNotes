package ru.hryasch.coachnotes.fragments.impl

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.ExperimentalCoroutinesApi
import moxy.MvpAppCompatFragment
import moxy.presenter.InjectPresenter
import org.koin.core.KoinComponent
import org.koin.core.get
import org.koin.core.parameter.parametersOf
import ru.hryasch.coachnotes.R
import ru.hryasch.coachnotes.domain.group.data.Group
import ru.hryasch.coachnotes.fragments.GroupsView
import ru.hryasch.coachnotes.groups.GroupsAdapter
import ru.hryasch.coachnotes.groups.presenters.impl.GroupsPresenterImpl

class GroupListFragment: MvpAppCompatFragment(), GroupsView, KoinComponent
{
    @ExperimentalCoroutinesApi
    @InjectPresenter
    lateinit var presenter: GroupsPresenterImpl

    // Toolbar
    private lateinit var toolbar: Toolbar
    private lateinit var addNewGroup: ImageButton

    // UI
    private lateinit var groupsView: RecyclerView
    private lateinit var groupsLoading: ProgressBar
    private lateinit var noGroupsLabel: TextView

    // Adapters
    private lateinit var groupsAdapter: GroupsAdapter



    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View?
    {
        val layout = inflater.inflate(R.layout.fragment_groups, container, false)

        groupsView = layout.findViewById(R.id.groupsRecyclerViewGroupsList)
        groupsLoading = layout.findViewById(R.id.groupsProgressBarLoading)
        addNewGroup = layout.findViewById(R.id.groupsButtonAddGroup)
        noGroupsLabel = layout.findViewById(R.id.groupsTextViewNoData)
        noGroupsLabel.visibility = View.INVISIBLE

        toolbar = layout.findViewById(R.id.groupsToolbar)
        toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }

        addNewGroup.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .add(R.id.mainFragmentSpace, GroupEditFragment(), null)
                .addToBackStack(null)
                .commit()
        }

        return layout
    }

    override fun setGroupsList(groupsList: List<Group>?)
    {
        if (groupsList == null)
        {
            groupsView.visibility = View.INVISIBLE
            groupsLoading.visibility = View.VISIBLE
            noGroupsLabel.visibility = View.INVISIBLE
        }
        else
        {
            groupsView.visibility = View.VISIBLE
            groupsLoading.visibility = View.INVISIBLE

            val listener =  object: GroupsAdapter.GroupClickListener {
                override fun onGroupClick(group: Group)
                {
                    requireActivity().supportFragmentManager.beginTransaction()
                        .add(R.id.mainFragmentSpace, GroupInfoFragment().apply {
                            arguments = Bundle().apply {
                                putSerializable("group", group)
                            }
                        }, null)
                        .addToBackStack(null)
                        .commit()
                }
            }

            groupsAdapter = get { parametersOf(groupsList, listener) }
            groupsView.adapter = groupsAdapter
            groupsView.layoutManager = LinearLayoutManager(context)

            if (groupsAdapter.itemCount == 0)
            {
                noGroupsLabel.visibility = View.VISIBLE
            }
            else
            {
                noGroupsLabel.visibility = View.INVISIBLE
            }

            toolbar.title = getString(R.string.groups_screen_toolbar_with_count_title, groupsAdapter.itemCount)
        }
    }

    override fun refreshData()
    {
        groupsAdapter.notifyDataSetChanged()
        toolbar.title = getString(R.string.groups_screen_toolbar_with_count_title, groupsAdapter.itemCount)
    }
}