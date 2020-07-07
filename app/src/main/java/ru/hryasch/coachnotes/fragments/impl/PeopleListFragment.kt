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
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pawegio.kandroid.e
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
    @InjectPresenter
    lateinit var presenter: PeoplePresenterImpl

    private lateinit var peopleAdapter: PeopleAdapter
    private lateinit var navController: NavController

    private lateinit var addNewPerson: ImageButton

    private lateinit var peopleView: RecyclerView
    private lateinit var peopleLoading: ProgressBar
    private lateinit var noPeopleLabel: TextView

    private lateinit var currentGroupNames: Map<GroupId, String>

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View?
    {
        val layout = inflater.inflate(R.layout.fragment_persons, container, false)

        peopleView = layout.findViewById(R.id.peopleRecyclerViewPeopleList)
        peopleLoading = layout.findViewById(R.id.peopleProgressBarLoading)
        addNewPerson = layout.findViewById(R.id.peopleButtonAddPerson)
        noPeopleLabel = layout.findViewById(R.id.peopleTextViewNoData)

        navController = container!!.findNavController()

        val toolbar: Toolbar = layout.findViewById(R.id.peopleToolbar)
        (activity as AppCompatActivity).supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        (activity as AppCompatActivity).supportActionBar!!.setDisplayShowHomeEnabled(true)

        noPeopleLabel.visibility = View.INVISIBLE

        toolbar.setNavigationOnClickListener {
            navController.navigateUp()
        }

        addNewPerson.setOnClickListener {
            val action = PeopleListFragmentDirections.actionPersonListFragmentToPersonEditFragment()
            navController.navigate(action)
        }

        return layout
    }

    override fun onStart()
    {
        super.onStart()
        (activity as MainActivity).showBottomNavigation()
    }

    override fun setPeopleList(peopleList: List<Person>?, groupNames: Map<GroupId, String>?)
    {
        if (peopleList == null)
        {
            peopleView.visibility = View.INVISIBLE
            peopleLoading.visibility = View.VISIBLE
            noPeopleLabel.visibility = View.INVISIBLE
        }
        else
        {
            peopleView.visibility = View.VISIBLE
            peopleLoading.visibility = View.INVISIBLE

            val listener =  object: PeopleAdapter.PersonClickListener {
                override fun onPersonClick(person: Person)
                {
                    val action = PeopleListFragmentDirections.actionPeopleListFragmentToPersonInfoFragment(person)
                    navController.navigate(action)
                }
            }

            if (::currentGroupNames.isInitialized)
            {
                if (groupNames != null)
                {
                    currentGroupNames = groupNames
                }
            }
            else
            {
                currentGroupNames = groupNames ?: HashMap()
            }
            peopleAdapter = get { parametersOf(peopleList, currentGroupNames, listener) }

            peopleView.adapter = peopleAdapter
            peopleView.layoutManager = LinearLayoutManager(context)
            peopleView.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))

            if (peopleAdapter.itemCount == 0)
            {
                noPeopleLabel.visibility = View.VISIBLE
            }
            else
            {
                noPeopleLabel.visibility = View.INVISIBLE
            }
        }
    }

    override fun refreshData()
    {
        peopleAdapter.notifyDataSetChanged()
    }
}