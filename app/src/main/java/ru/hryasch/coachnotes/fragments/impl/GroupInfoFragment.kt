package ru.hryasch.coachnotes.fragments.impl

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.pawegio.kandroid.i
import com.pawegio.kandroid.visible
import com.skydoves.powerspinner.PowerSpinnerView
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import moxy.MvpAppCompatFragment
import moxy.presenter.InjectPresenter
import org.koin.core.KoinComponent
import org.koin.core.get
import org.koin.core.parameter.parametersOf
import ru.hryasch.coachnotes.R
import ru.hryasch.coachnotes.application.App
import ru.hryasch.coachnotes.domain.common.GroupId
import ru.hryasch.coachnotes.domain.group.data.Group
import ru.hryasch.coachnotes.domain.person.data.Person
import ru.hryasch.coachnotes.fragments.GroupView
import ru.hryasch.coachnotes.groups.GroupMembersAdapter
import ru.hryasch.coachnotes.groups.isSingle
import ru.hryasch.coachnotes.groups.presenters.impl.GroupPresenterImpl
import ru.hryasch.coachnotes.repository.common.toAbsolute
import ru.hryasch.coachnotes.repository.common.toRelative
import java.util.*
import kotlin.collections.ArrayList

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
    private lateinit var noMembersData: TextView
    private lateinit var addMember: MaterialButton
    private lateinit var paymentType: TextView
    private lateinit var isPaid: ImageView

    private lateinit var currentGroup: Group
    private lateinit var currentMembers: MutableList<Person>

    private lateinit var contentView: NestedScrollView
    private lateinit var loadingBar: ProgressBar

    @ExperimentalCoroutinesApi
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
        noMembersData = layout.findViewById(R.id.groupInfoTextViewNoData)
        addMember = layout.findViewById(R.id.groupInfoButtonAddMember)
        paymentType = layout.findViewById(R.id.groupInfoTextViewIsPaid)
        isPaid = layout.findViewById(R.id.groupImageViewIsPaid)

        contentView = layout.findViewById(R.id.groupInfoContentView)
        loadingBar = layout.findViewById(R.id.groupInfoProgressBarLoading)

        noMembersData.visibility = View.INVISIBLE

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

        currentGroup = group
        currentMembers = members.toMutableList()

        name.text = group.name
        age1.text = group.availableAbsoluteAge!!.first.toString()
        age2.text = group.availableAbsoluteAge!!.last.toString()

        if (group.availableAbsoluteAge!!.isSingle())
        {
            age2.visibility = View.GONE
            ageRange.visibility = View.GONE
        }

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

        ageType.setOnSpinnerItemSelectedListener<String> { position, _ ->
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
                presenter.onDeletePersonFromCurrentGroupClicked(person)
            }
        }

        editGroup.setOnClickListener {
            val action = GroupInfoFragmentDirections.actionGroupInfoFragmentToGroupEditFragment(currentGroup)
            navController.navigate(action)
        }

        membersAdapter = get { parametersOf(currentMembers, groupNames, listener) }
        shortMembersList.adapter = membersAdapter
        shortMembersList.layoutManager = LinearLayoutManager(context)

        noMembersData.visible = ( membersAdapter.itemCount == 0 )

        addMember.setOnClickListener {
            presenter.onAddPeopleToGroupClicked()
        }
    }

    override fun loadingState()
    {
        contentView.visible = false
        loadingBar.visible = true
        noMembersData.visible = false
    }

    override fun showDeletePersonFromGroupNotification(person: Person?)
    {
        if (person == null)
        {
            return
        }

        val dialog = MaterialAlertDialogBuilder(this@GroupInfoFragment.context!!)
            .setTitle("Удалене ученика из группы")
            .setMessage("Вы уверены что хотите удалить ${person.surname} ${person.name} из группы ${currentGroup.name} ?")
            .setPositiveButton("Удалить") { dialog, _ ->
                dialog.cancel()
                currentMembers.remove(person)
                i("members after remove: ${currentMembers.size}")
                membersAdapter.notifyDataSetChanged()
                noMembersData.visible = ( membersAdapter.itemCount == 0 )
                membersCount.text = membersCount.text.toString().toInt().dec().toString()

                presenter.deletePersonFromCurrentGroup(person.id)
            }
            .setNegativeButton("Отмена") { dialog, _ ->
                dialog.cancel()
            }
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(App.getCtx(), R.color.colorAccent))
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(App.getCtx(), R.color.colorPrimaryLight))
        }

        dialog.show()
    }

    @ExperimentalCoroutinesApi
    override fun showAddPeopleToGroupNotification(people: List<Person>?)
    {
        if (people == null)
        {
            return
        }

        val checkedCountSendChannel = ConflatedBroadcastChannel<Int>()
        val checkedCountRecvChannel = checkedCountSendChannel.openSubscription()
        val channels = Job()

        val peopleItems = Array<String>(people.size) {""}
        val checkedItems: BooleanArray = BooleanArray(people.size) {false}
        var checkedCount = 0

        for ((i, person) in people.withIndex())
        {
            peopleItems[i] = "${person.surname} ${person.name}"
            checkedItems[i] = false
        }

        val dialog = MaterialAlertDialogBuilder(this@GroupInfoFragment.context!!)
            .setTitle("Добавить учеников в группу")
            .setMultiChoiceItems(peopleItems, checkedItems) { _, pos, isChecked ->
                checkedItems[pos] = isChecked
                if (isChecked)
                {
                    checkedCount++
                }
                else
                {
                    checkedCount--
                }

                GlobalScope.launch(Dispatchers.Main + channels)
                {
                    checkedCountSendChannel.send(checkedCount)
                }
            }
            .setPositiveButton("Добавить ($checkedCount)") { dialog, _ ->
                dialog.cancel()
                checkedCountRecvChannel.cancel()
                checkedCountSendChannel.cancel()
                channels.cancel()

                val peopleList: MutableList<Person> = LinkedList()
                for ((i, item) in checkedItems.withIndex())
                {
                    if (item)
                    {
                        val newPerson = people[i].apply {
                            groupId = currentGroup.id
                            isPaid = currentGroup.isPaid
                        }
                        peopleList.add(newPerson)
                        currentMembers.add(newPerson)
                    }
                }
                currentMembers.sort()
                membersAdapter.notifyDataSetChanged()
                noMembersData.visible = ( membersAdapter.itemCount == 0 )
                membersCount.text = currentMembers.size.toString()

                presenter.addPeopleToGroup(peopleList)
            }
            .setNegativeButton("Отмена") { dialog, _ ->
                dialog.cancel()

                checkedCountRecvChannel.cancel()
                checkedCountSendChannel.cancel()
                channels.cancel()
            }
            .create()

        GlobalScope.launch(Dispatchers.Main + channels)
        {
            while (true)
            {
                val text = "Добавить учеников (${checkedCountRecvChannel.receive()})"
                val button = dialog.getButton(DialogInterface.BUTTON_POSITIVE)
                button.text = text

                button.isEnabled = checkedCount != 0
            }
        }


        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(App.getCtx(), R.color.colorAccent))
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(App.getCtx(), R.color.colorPrimaryLight))
        }

        dialog.show()
    }
}