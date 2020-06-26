package ru.hryasch.coachnotes.fragments.impl

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.navigation.NavController
import androidx.navigation.findNavController
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

    private lateinit var groupsAdapter: GroupsAdapter

    private lateinit var groupsView: RecyclerView
    private lateinit var groupsLoading: ProgressBar
    private lateinit var addNewGroup: ImageButton
    private lateinit var noGroupsLabel: TextView

    lateinit var navController: NavController

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View?
    {
        val layout = inflater.inflate(R.layout.fragment_groups, container, false)

        groupsView = layout.findViewById(R.id.groupsRecyclerViewGroupsList)
        groupsLoading = layout.findViewById(R.id.groupsProgressBarLoading)
        addNewGroup = layout.findViewById(R.id.groupsButtonAddGroup)
        noGroupsLabel = layout.findViewById(R.id.groupsTextViewNoData)

        navController = container!!.findNavController()

        val toolbar: Toolbar = layout.findViewById(R.id.groupsToolbar)
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        (activity as AppCompatActivity).supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        (activity as AppCompatActivity).supportActionBar!!.setDisplayShowHomeEnabled(true)

        noGroupsLabel.visibility = View.INVISIBLE

        toolbar.setNavigationOnClickListener {
            navController.navigateUp()
        }

        addNewGroup.setOnClickListener {
            val action = GroupListFragmentDirections.actionGroupListFragmentToGroupEditFragment()
            navController.navigate(action)
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
                    val action = GroupListFragmentDirections.actionGroupListFragmentToGroupInfoFragment(group)
                    navController.navigate(action)
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
        }
    }

    override fun refreshData()
    {
        groupsAdapter.notifyDataSetChanged()
    }
}