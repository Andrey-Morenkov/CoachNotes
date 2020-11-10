package ru.hryasch.coachnotes.fragments.impl

import android.os.Bundle
import android.util.Log.e
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pawegio.kandroid.e
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
    private lateinit var currentPeople: MutableList<Person>
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
        addNewPerson = layout.findViewById(R.id.peopleButtonAddPerson)
        noPeopleLabel = layout.findViewById(R.id.peopleTextViewNoData)
        noPeopleLabel.visibility = View.INVISIBLE

        toolbar = layout.findViewById(R.id.peopleToolbar)
        toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }

        addNewPerson.setOnClickListener {
            (requireActivity() as MainActivity).navigateToPersonEditFragment(null)
        }

        return layout
    }

    override fun setPeopleList(peopleList: List<Person>?, groupNames: Map<GroupId, String>?)
    {
        if (peopleList == null && groupNames == null)
        {
            peopleView.visibility = View.INVISIBLE
            peopleLoading.visibility = View.VISIBLE
            noPeopleLabel.visibility = View.INVISIBLE
            return
        }

        peopleView.visibility = View.VISIBLE
        peopleLoading.visibility = View.INVISIBLE

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
                toolbar.title = getString(R.string.persons_screen_toolbar_with_count_title, peopleList.size)
                currentPeople.clear()
                currentPeople.addAll(peopleList.sorted())
                peopleAdapter.notifyDataSetChanged()
            }
        }
        else
        {
            // First initialization
            currentGroupNames = groupNames ?: HashMap()
            currentPeople = peopleList?.sorted()?.toMutableList() ?: ArrayList()

            peopleAdapter = get { parametersOf(currentPeople, currentGroupNames, listener) }
            peopleView.adapter = peopleAdapter
            peopleView.layoutManager = LinearLayoutManager(context)
            peopleView.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }

        if (peopleAdapter.itemCount == 0)
        {
            noPeopleLabel.visibility = View.VISIBLE
        }
        else
        {
            noPeopleLabel.visibility = View.INVISIBLE
        }
    }

    override fun refreshData()
    {
        peopleAdapter.notifyDataSetChanged()
        toolbar.title = getString(R.string.persons_screen_toolbar_with_count_title, peopleAdapter.itemCount)
    }
}