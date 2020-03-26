package ru.hryasch.coachnotes.fragments.impl

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SortedList
import moxy.MvpAppCompatFragment
import moxy.presenter.InjectPresenter
import org.koin.android.ext.android.get
import org.koin.core.KoinComponent
import org.koin.core.parameter.parametersOf
import ru.hryasch.coachnotes.R
import ru.hryasch.coachnotes.domain.common.GroupId
import ru.hryasch.coachnotes.domain.person.data.Person
import ru.hryasch.coachnotes.fragments.api.PeopleView
import ru.hryasch.coachnotes.people.PeopleAdapter
import ru.hryasch.coachnotes.people.presenters.impl.PeoplePresenterImpl

class PeopleListFragment : MvpAppCompatFragment(), PeopleView, KoinComponent
{
    @InjectPresenter
    lateinit var presenter: PeoplePresenterImpl

    private lateinit var peopleAdapter: PeopleAdapter

    private lateinit var peopleView: RecyclerView
    private lateinit var peopleLoading: ProgressBar
    private lateinit var addNewPerson: ImageButton

    lateinit var navController: NavController

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View?
    {
        val layout = inflater.inflate(R.layout.fragment_persons, container, false)

        peopleView = layout.findViewById(R.id.peopleRecyclerViewPeopleList)
        peopleLoading = layout.findViewById(R.id.peopleProgressBarLoading)
        addNewPerson = layout.findViewById(R.id.peopleButtonAddPerson)

        navController = container!!.findNavController()

        val toolbar: Toolbar = layout.findViewById(R.id.peopleToolbar)
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        (activity as AppCompatActivity).supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        (activity as AppCompatActivity).supportActionBar!!.setDisplayShowHomeEnabled(true)

        toolbar.setNavigationOnClickListener {
            navController.navigateUp()
        }

        return layout
    }

    override fun setPeopleList(peopleList: List<Person>?, groupNames: Map<GroupId, String>?)
    {
        if (peopleList == null)
        {
            peopleView.visibility = View.INVISIBLE
            peopleLoading.visibility = View.VISIBLE
        }
        else
        {
            peopleView.visibility = View.VISIBLE
            peopleLoading.visibility = View.INVISIBLE

            peopleAdapter = get { parametersOf(peopleList, groupNames) }
            peopleView.adapter = peopleAdapter
            peopleView.layoutManager = LinearLayoutManager(context)
            peopleView.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }
    }

    override fun refreshData()
    {
        TODO("Not yet implemented")
    }
}