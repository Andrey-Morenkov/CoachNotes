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
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.ExperimentalCoroutinesApi
import moxy.MvpAppCompatFragment
import moxy.presenter.InjectPresenter
import org.koin.android.ext.android.get
import org.koin.core.parameter.parametersOf
import ru.hryasch.coachnotes.R
import ru.hryasch.coachnotes.activity.MainActivity
import ru.hryasch.coachnotes.domain.common.GroupId
import ru.hryasch.coachnotes.domain.person.data.Person
import ru.hryasch.coachnotes.fragments.PeopleView
import ru.hryasch.coachnotes.people.PeopleAdapter
import ru.hryasch.coachnotes.people.presenters.impl.PeoplePresenterImpl
import java.util.LinkedList
import java.util.stream.Collectors

class PeopleListFragment : MvpAppCompatFragment(), PeopleView
{
    @ExperimentalCoroutinesApi
    @InjectPresenter
    lateinit var presenter: PeoplePresenterImpl
    private lateinit var layout: View

    // Toolbar
    private lateinit var toolbar: Toolbar
    private lateinit var addNewPerson: MenuItem

    // UI
    private lateinit var peopleView: RecyclerView
    private lateinit var peopleLoading: ProgressBar
    private lateinit var noPeopleLabel: TextView

    // Adapters
    private lateinit var peopleAdapter: PeopleAdapter

    // Data
    private lateinit var currentGroupNames: Map<GroupId, String>
    private lateinit var currentFullPeople: List<Person>
    private val currentPeople: MutableList<Person> = ArrayList()

    private val listener =  object: PeopleAdapter.PersonClickListener {
        override fun onPersonClick(person: Person)
        {
            (requireActivity() as MainActivity).navigateToPersonInfoFragment(person)
        }
    }


    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View?
    {
        layout = inflater.inflate(R.layout.fragment_persons, container, false)

        peopleView = layout.findViewById(R.id.peopleRecyclerViewPeopleList)
        if (::peopleAdapter.isInitialized)
        {
            peopleView.adapter = peopleAdapter
            peopleView.layoutManager = LinearLayoutManager(context)
            peopleView.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }
        peopleLoading = layout.findViewById(R.id.peopleProgressBarLoading)
        noPeopleLabel = layout.findViewById(R.id.peopleTextViewNoData)
        noPeopleLabel.visibility = View.INVISIBLE

        initToolbar(layout)

        return layout
    }

    override fun setPeopleList(peopleList: List<Person>?, groupNames: Map<GroupId, String>?)
    {
        if (peopleList == null && groupNames == null)
        {
            loadingState()
            return
        }

        if (::peopleAdapter.isInitialized)
        {
            // Apply new runtime data
            if (groupNames != null)
            {
                // apply new group names
                currentGroupNames = groupNames
                peopleAdapter.updateGroups(currentGroupNames)
            }

            if (peopleList != null)
            {
                // apply new people
                currentFullPeople = peopleList
                currentPeople.clear()
                currentPeople.addAll(currentFullPeople)
            }
        }
        else
        {
            // First initialization
            currentGroupNames = groupNames ?: HashMap()
            currentFullPeople = peopleList?.sorted()?.toMutableList() ?: ArrayList()
            currentPeople.clear()
            currentPeople.addAll(currentFullPeople)

            peopleAdapter = get { parametersOf(currentPeople, currentGroupNames, listener) }
            peopleView.adapter = peopleAdapter
            peopleView.layoutManager = LinearLayoutManager(context)
            peopleView.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }

        peopleDataChanged()
    }

    override fun refreshData()
    {
        peopleDataChanged()
    }

    private fun loadingState()
    {
        peopleView.visibility = View.INVISIBLE
        peopleLoading.visibility = View.VISIBLE
        noPeopleLabel.visibility = View.INVISIBLE
    }

    private fun emptyState()
    {
        peopleView.visibility = View.INVISIBLE
        peopleLoading.visibility = View.INVISIBLE
        noPeopleLabel.visibility = View.VISIBLE
    }

    private fun contentState()
    {
        peopleView.visibility = View.VISIBLE
        peopleLoading.visibility = View.INVISIBLE
        noPeopleLabel.visibility = View.INVISIBLE
    }

    private fun initToolbar(view: View)
    {
        toolbar = view.findViewById(R.id.peopleToolbar)
        toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }
        toolbar.inflateMenu(R.menu.people_menu)
        addNewPerson = toolbar.menu.findItem(R.id.people_create_person_item)
        toolbar.setOnMenuItemClickListener {
            when (it.itemId)
            {
                R.id.people_create_person_item -> {
                    (requireActivity() as MainActivity).navigateToPersonEditFragment(null)
                    return@setOnMenuItemClickListener true
                }
            }

            return@setOnMenuItemClickListener false
        }
        val searchItem = toolbar.menu.findItem(R.id.people_find_item)
        val searchView = searchItem.actionView as SearchView
        searchView.queryHint = "Поиск учеников"
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
                                                      val filteredPeople = currentFullPeople
                                                          .parallelStream()
                                                          .filter { person ->
                                                              person.surname.contains(newText, true) ||
                                                              person.name.contains(newText, true) ||
                                                              person.patronymic?.contains(newText, true) ?: false}
                                                          .collect(Collectors.toList())
                                                      currentPeople.clear()
                                                      currentPeople.addAll(filteredPeople)
                                                  }
                                                  else
                                                  {
                                                      currentPeople.clear()
                                                      currentPeople.addAll(currentFullPeople)
                                                  }

                                                  peopleDataChanged()
                                                  return true
                                              }
                                          })
        val editTextView: EditText = searchView.findViewById(androidx.appcompat.R.id.search_src_text)
        editTextView.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorText))
        editTextView.setHintTextColor(ContextCompat.getColor(requireContext(), R.color.colorDisabledText))

        val searchClearIcon: AppCompatImageView = searchView.findViewById(androidx.appcompat.R.id.search_close_btn)
        searchClearIcon.setColorFilter(ContextCompat.getColor(requireContext(), R.color.colorText))
    }

    private fun peopleDataChanged()
    {
        peopleAdapter.notifyDataSetChanged()
        refreshViewState()
    }

    private fun refreshViewState()
    {
        toolbar.title = getString(R.string.persons_screen_toolbar_with_count_title, peopleAdapter.itemCount)
        if (peopleAdapter.itemCount > 0)
        {
            contentState()
        }
        else
        {
            emptyState()
        }
    }
}