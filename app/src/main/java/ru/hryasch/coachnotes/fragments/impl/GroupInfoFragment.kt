package ru.hryasch.coachnotes.fragments.impl

import android.graphics.ColorFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.pawegio.kandroid.visible
import com.skydoves.powerspinner.PowerSpinnerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import moxy.MvpAppCompatFragment
import moxy.presenter.InjectPresenter
import org.koin.core.KoinComponent
import org.koin.core.get
import org.koin.core.parameter.parametersOf
import ru.hryasch.coachnotes.R
import ru.hryasch.coachnotes.domain.common.GroupId
import ru.hryasch.coachnotes.domain.group.data.Group
import ru.hryasch.coachnotes.domain.person.data.Person
import ru.hryasch.coachnotes.fragments.GroupView
import ru.hryasch.coachnotes.groups.GroupMembersAdapter
import ru.hryasch.coachnotes.groups.isSingle
import ru.hryasch.coachnotes.groups.presenters.impl.GroupPresenterImpl
import ru.hryasch.coachnotes.repository.common.toAbsolute
import ru.hryasch.coachnotes.repository.common.toRelative

class GroupInfoFragment : MvpAppCompatFragment(), GroupView, KoinComponent
{
    @InjectPresenter
    lateinit var presenter: GroupPresenterImpl

    private lateinit var navController: NavController
    private lateinit var membersAdapter: GroupMembersAdapter

    private lateinit var editGroup: ImageButton

    private lateinit var name: TextView
    private lateinit var age1: TextView
    private lateinit var age2: TextView
    private lateinit var ageRange: ImageView
    private lateinit var ageType: PowerSpinnerView
    private lateinit var membersCount: TextView
    private lateinit var fullMembersList: MaterialButton
    private lateinit var shortMembersList: RecyclerView
    private lateinit var addMember: MaterialButton
    private lateinit var paymentType: TextView
    private lateinit var isPaid: ImageView

    private lateinit var contentView: NestedScrollView
    private lateinit var loadingBar: ProgressBar

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View?
    {
        val layout =  inflater.inflate(R.layout.fragment_group_info, container, false)

        editGroup = layout.findViewById(R.id.groupInfoImageButtonEditPerson)

        name = layout.findViewById(R.id.groupInfoTextViewName)
        age1 = layout.findViewById(R.id.groupInfoTextViewAge1)
        age2 = layout.findViewById(R.id.groupInfoTextViewAge2)
        ageRange = layout.findViewById(R.id.groupInfoImageViewRangeAges)
        ageType = layout.findViewById(R.id.groupInfoSpinnerAgeType)
        membersCount = layout.findViewById(R.id.groupInfoTextViewMembersCount)
        fullMembersList = layout.findViewById(R.id.groupInfoButtonAllMembersList)
        shortMembersList = layout.findViewById(R.id.groupInfoRecyclerViewMembers)
        addMember = layout.findViewById(R.id.groupInfoButtonAddMember)
        paymentType = layout.findViewById(R.id.groupInfoTextViewIsPaid)
        isPaid = layout.findViewById(R.id.groupImageViewIsPaid)

        contentView = layout.findViewById(R.id.groupInfoContentView)
        loadingBar = layout.findViewById(R.id.groupInfoProgressBarLoading)

        loadingState()

        ageType.setItems(listOf(context!!.getString(R.string.age_type_absolute), context!!.getString(R.string.age_type_relative)))
        ageType.selectItemByIndex(0)
        ageType.lifecycleOwner = this

        navController = container!!.findNavController()

        val toolbar: Toolbar = layout.findViewById(R.id.groupInfoToolbar)
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        (activity as AppCompatActivity).supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        (activity as AppCompatActivity).supportActionBar!!.setDisplayShowHomeEnabled(true)

        toolbar.setNavigationOnClickListener {
            navController.navigateUp()
        }

        GlobalScope.launch(Dispatchers.Default)
        {
            presenter.applyGroupData(GroupInfoFragmentArgs.fromBundle(arguments!!).groupData)
        }

        return layout
    }

    override fun setGroupData(group: Group, members: List<Person>, groupNames: Map<GroupId, String>)
    {
        contentView.visible = true
        loadingBar.visible = false

        name.text = group.name
        age1.text = group.availableAbsoluteAge!!.first.toString()
        age2.text = group.availableAbsoluteAge!!.last.toString()

        if (group.availableAbsoluteAge!!.isSingle())
        {
            age2.visibility = View.GONE
            ageRange.visibility = View.GONE
        }

        val adapter =

        if (group.isPaid)
        {
            paymentType.text = context!!.getString(R.string.group_param_payment_paid)
            paymentType.setTextColor(ContextCompat.getColor(context!!, R.color.colorPaid))
            isPaid.setImageDrawable(ContextCompat.getDrawable(context!!, R.drawable.ic_money_active))
        }
        else
        {
            paymentType.text = context!!.getString(R.string.group_param_payment_free)
            paymentType.setTextColor(ContextCompat.getColor(context!!, R.color.colorText))
            isPaid.setImageDrawable(ContextCompat.getDrawable(context!!, R.drawable.ic_money))
        }

        ageType.setOnSpinnerItemSelectedListener<String> { position, text ->
            when(position)
            {
                0 -> {
                    if (age1.text.toString().toInt() < 1000)
                    {
                        age1.text = age1.text.toString().toInt().toAbsolute().toString()
                        age2.text = age2.text.toString().toInt().toAbsolute().toString()
                    }
                }
                1 -> {
                    if (age1.text.toString().toInt() > 1000)
                    {
                        age1.text = age1.text.toString().toInt().toRelative().toString()
                        age2.text = age2.text.toString().toInt().toRelative().toString()
                    }
                }
            }
        }

        membersCount.text = group.membersList.size.toString()

        val listener =  object: GroupMembersAdapter.RemovePersonListener {
            override fun onPersonRemoveFromGroup(person: Person)
            {
                // тут поменять группу id у чела на null и все заапдейтить
            }
        }

        membersAdapter = get { parametersOf(members, groupNames, listener) }
        shortMembersList.adapter = membersAdapter
        shortMembersList.layoutManager = LinearLayoutManager(context)
    }

    override fun loadingState()
    {
        contentView.visible = false
        loadingBar.visible = true
    }
}