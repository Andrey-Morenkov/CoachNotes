package ru.hryasch.coachnotes.fragments.impl

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pawegio.kandroid.i
import kotlinx.coroutines.ExperimentalCoroutinesApi
import moxy.MvpAppCompatFragment
import moxy.presenter.InjectPresenter
import org.koin.core.KoinComponent
import org.koin.core.get
import org.koin.core.parameter.parametersOf
import ru.hryasch.coachnotes.R
import ru.hryasch.coachnotes.activity.MainActivity
import ru.hryasch.coachnotes.domain.group.data.Group
import ru.hryasch.coachnotes.fragments.GroupsView
import ru.hryasch.coachnotes.groups.GroupsAdapter
import ru.hryasch.coachnotes.groups.presenters.impl.GroupsPresenterImpl
import java.util.stream.Collectors


class GroupListFragment: MvpAppCompatFragment(), GroupsView, KoinComponent
{
    @ExperimentalCoroutinesApi
    @InjectPresenter
    lateinit var presenter: GroupsPresenterImpl
    private lateinit var layout: View

    // Toolbar
    private lateinit var toolbar: Toolbar
    private lateinit var addNewGroup: MenuItem

    // UI
    private lateinit var groupsView: RecyclerView
    private lateinit var groupsLoading: ProgressBar
    private lateinit var noGroupsLabel: TextView

    // Adapters
    private lateinit var groupsAdapter: GroupsAdapter

    // Data
    private lateinit var currentFullGroups: List<Group>
    private val currentGroups: MutableList<Group> = ArrayList()
    private val listener =  object: GroupsAdapter.GroupClickListener {
        override fun onGroupClick(group: Group)
        {
            (requireActivity() as MainActivity).navigateToGroupInfoFragment(group)
        }
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View?
    {
        layout = inflater.inflate(R.layout.fragment_groups, container, false)

        groupsView = layout.findViewById(R.id.groupsRecyclerViewGroupsList)
        if (::groupsAdapter.isInitialized)
        {
            groupsView.adapter = groupsAdapter
            groupsView.layoutManager = LinearLayoutManager(context)
        }
        groupsLoading = layout.findViewById(R.id.groupsProgressBarLoading)
        noGroupsLabel = layout.findViewById(R.id.groupsTextViewNoData)
        noGroupsLabel.visibility = View.INVISIBLE

        initToolbar(layout)

        return layout
    }

    override fun setGroupsList(groupsList: List<Group>?)
    {
        if (groupsList == null)
        {
            loadingState()
            return
        }

        if (::groupsAdapter.isInitialized)
        {
            // Apply runtime data
            if (groupsList != null)
            {
                // apply new people
                currentFullGroups = groupsList
                currentGroups.clear()
                currentGroups.addAll(currentFullGroups)
            }
        }
        else
        {
            // First initialization
            currentFullGroups = groupsList
            currentGroups.clear()
            currentGroups.addAll(currentFullGroups)

            groupsAdapter = get { parametersOf(currentGroups, listener) }
            groupsView.adapter = groupsAdapter
            groupsView.layoutManager = LinearLayoutManager(context)
        }

        groupDataChanged()
    }

    override fun refreshData()
    {
        groupDataChanged()
    }

    private fun loadingState()
    {
        groupsView.visibility = View.INVISIBLE
        groupsLoading.visibility = View.VISIBLE
        noGroupsLabel.visibility = View.INVISIBLE
    }

    private fun emptyState()
    {
        groupsView.visibility = View.INVISIBLE
        groupsLoading.visibility = View.INVISIBLE
        noGroupsLabel.visibility = View.VISIBLE
    }

    private fun contentState()
    {
        groupsView.visibility = View.VISIBLE
        groupsLoading.visibility = View.INVISIBLE
        noGroupsLabel.visibility = View.INVISIBLE
    }

    private fun initToolbar(view: View)
    {
        toolbar = view.findViewById(R.id.groupsToolbar)
        toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }
        toolbar.inflateMenu(R.menu.groups_menu)
        addNewGroup = toolbar.menu.findItem(R.id.groups_create_group_item)
        toolbar.setOnMenuItemClickListener {
            when (it.itemId)
            {
                R.id.groups_create_group_item -> {
                    (requireActivity() as MainActivity).navigateToGroupEditFragment(null)
                    return@setOnMenuItemClickListener true
                }
            }

            return@setOnMenuItemClickListener false
        }
        val searchItem = toolbar.menu.findItem(R.id.groups_find_item)
        val searchView = searchItem.actionView as SearchView
        searchView.queryHint = "Поиск групп"
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener
                                          {
                                              override fun onQueryTextSubmit(query: String?): Boolean
                                              {
                                                  return false
                                              }

                                              override fun onQueryTextChange(newText: String?): Boolean
                                              {
                                                  if (newText != null)
                                                  {
                                                      val filteredGroup = currentFullGroups
                                                          .parallelStream()
                                                          .filter { group -> group.name.contains(newText, true) }
                                                          .collect(Collectors.toList())
                                                      currentGroups.clear()
                                                      currentGroups.addAll(filteredGroup)
                                                  }
                                                  else
                                                  {
                                                      currentGroups.clear()
                                                      currentGroups.addAll(currentFullGroups)
                                                  }

                                                  groupDataChanged()
                                                  return true
                                              }
                                          })
        val editTextView: EditText = searchView.findViewById(androidx.appcompat.R.id.search_src_text)
        editTextView.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorText))
        editTextView.setHintTextColor(ContextCompat.getColor(requireContext(), R.color.colorDisabledText))

        val searchClearIcon: AppCompatImageView = searchView.findViewById(androidx.appcompat.R.id.search_close_btn)
        searchClearIcon.setColorFilter(ContextCompat.getColor(requireContext(), R.color.colorText))
    }

    private fun groupDataChanged()
    {
        groupsAdapter.notifyDataSetChanged()
        refreshViewState()
    }

    private fun refreshViewState()
    {
        toolbar.title = getString(R.string.groups_screen_toolbar_with_count_title, groupsAdapter.itemCount)
        if (groupsAdapter.itemCount > 0)
        {
            contentState()
        }
        else
        {
            emptyState()
        }
    }
}