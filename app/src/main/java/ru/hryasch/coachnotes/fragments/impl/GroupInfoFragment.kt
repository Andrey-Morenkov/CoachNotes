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
import com.pawegio.kandroid.e
import com.pawegio.kandroid.visible
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
import ru.hryasch.coachnotes.repository.common.toRelative
import java.util.*

class GroupInfoFragment : MvpAppCompatFragment(), GroupView, KoinComponent
{
    @InjectPresenter
    lateinit var presenter: GroupPresenterImpl

    private lateinit var navController: NavController

    private lateinit var loadingBar: ProgressBar
    private lateinit var contentView: NestedScrollView

    // Toolbar
    private lateinit var editGroup: ImageButton

    // Base section
    private lateinit var name: TextView

    // Ages & payment section
    private lateinit var absoluteAge: TextView
    private lateinit var relativeAge: TextView
    private lateinit var paymentType: TextView
    private lateinit var isPaid: ImageView

    // Group members action
    private lateinit var membersCount: TextView
    private lateinit var fullMembersList: MaterialButton
    private lateinit var shortMembersList: RecyclerView
    private lateinit var membersAdapter: GroupMembersAdapter
    private lateinit var noMembersData: TextView
    private lateinit var addMember: MaterialButton


    private lateinit var currentGroup: Group
    private lateinit var currentMembers: MutableList<Person>
    private var isFirstSetData = true


    @ExperimentalCoroutinesApi
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View?
    {
        val layout =  inflater.inflate(R.layout.fragment_group_info, container, false)

        inflateToolbarElements(layout)
        inflateBaseSection(layout)
        inflateAgesAndPaymentSection(layout)
        inflateMembersSection(layout)

        contentView = layout.findViewById(R.id.groupInfoContentView)
        loadingBar = layout.findViewById(R.id.groupInfoProgressBarLoading)

        navController = container!!.findNavController()

        presenter.applyGroupDataAsync(GroupInfoFragmentArgs.fromBundle(requireArguments()).groupData)

        return layout
    }

    override fun setGroupData(group: Group, members: List<Person>, groupNames: Map<GroupId, String>)
    {
        e("setGroupData: $group")
        currentGroup = group
        currentMembers = members.toMutableList()

        name.text = group.name

        setToolbarActions()
        setAges()
        setPayment()
        setMembersSection(groupNames)

        isFirstSetData = false
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

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle("Удалене ученика из группы")
            .setMessage("Вы уверены что хотите удалить ${person.surname} ${person.name} из группы ${currentGroup.name} ?")
            .setPositiveButton("Удалить") { dialog, _ ->
                presenter.deletePersonFromCurrentGroup(person.id)
                dialog.cancel()
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

        val peopleItems  = Array(people.size) {""}
        val checkedItems = BooleanArray(people.size) {false}
        var checkedCount = 0

        for ((i, person) in people.withIndex())
        {
            peopleItems[i] = "${person.surname} ${person.name}"
            checkedItems[i] = false
        }

        val dialog = MaterialAlertDialogBuilder(requireContext())
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

                presenter.addPeopleToNewGroup(peopleList)

                dialog.cancel()
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
                val text = "Добавить (${checkedCountRecvChannel.receive()})"
                val button = dialog.getButton(DialogInterface.BUTTON_POSITIVE)
                button.text = text

                if (checkedCount == 0)
                {
                    button.isEnabled = false
                    button.setTextColor(ContextCompat.getColor(App.getCtx(), R.color.colorPrimaryLight))
                }
                else
                {
                    button.isEnabled = true
                    button.setTextColor(ContextCompat.getColor(App.getCtx(), R.color.colorAccent))
                }
            }
        }


        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(App.getCtx(), R.color.colorPrimaryLight))
        }

        dialog.show()
    }



    private fun inflateToolbarElements(layout: View)
    {
        editGroup = layout.findViewById(R.id.groupInfoImageButtonEditPerson)

        val toolbar: Toolbar = layout.findViewById(R.id.groupInfoToolbar)
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        (activity as AppCompatActivity).supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        (activity as AppCompatActivity).supportActionBar!!.setDisplayShowHomeEnabled(true)

        toolbar.setNavigationOnClickListener {
            navController.navigateUp()
        }
    }

    private fun inflateBaseSection(layout: View)
    {
        name = layout.findViewById(R.id.groupInfoTextViewName)
    }

    private fun inflateAgesAndPaymentSection(layout: View)
    {
        absoluteAge = layout.findViewById(R.id.groupInfoTextViewAbsoluteAge)
        relativeAge = layout.findViewById(R.id.groupInfoTextViewRelativeAge)
        paymentType = layout.findViewById(R.id.groupInfoTextViewIsPaid)
        isPaid = layout.findViewById(R.id.groupImageViewIsPaid)
    }

    private fun inflateMembersSection(layout: View)
    {
        membersCount = layout.findViewById(R.id.groupInfoTextViewMembersCount)
        fullMembersList = layout.findViewById(R.id.groupInfoButtonAllMembersList)
        shortMembersList = layout.findViewById(R.id.groupInfoRecyclerViewMembers)
        noMembersData = layout.findViewById(R.id.groupInfoTextViewNoData)
        addMember = layout.findViewById(R.id.groupInfoButtonAddMember)
    }

    private fun setToolbarActions()
    {
        editGroup.setOnClickListener {
            val action = GroupInfoFragmentDirections.actionGroupInfoFragmentToGroupEditFragment(currentGroup)
            navController.navigate(action)
        }
    }

    private fun setAges()
    {
        val age1 = currentGroup.availableAbsoluteAge?.first ?: -1
        val age2 = currentGroup.availableAbsoluteAge?.last ?: -1

        if (age1 == -1)
        {
            // age not set
            absoluteAge.text = getString(R.string.group_param_ages_multiple, "?", "?")
            relativeAge.text = getString(R.string.group_param_ages_multiple, "?", "?")
            return
        }

        // age set
        if (currentGroup.availableAbsoluteAge!!.isSingle())
        {
            // set only 1 age
            absoluteAge.text = getString(R.string.group_param_ages_single, age1.toString())
            relativeAge.text = getString(R.string.group_param_ages_single, age1.toRelative().toString())
        }
        else
        {
            // set age range
            absoluteAge.text = getString(R.string.group_param_ages_multiple, age1.toString(), age2.toString())
            relativeAge.text = getString(R.string.group_param_ages_multiple, age1.toRelative().toString(), age2.toRelative().toString())
        }
    }

    private fun setPayment()
    {
        if (currentGroup.isPaid)
        {
            paymentType.text = getString(R.string.group_param_payment_paid)
            paymentType.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorPaid))
            isPaid.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_money_active))
        }
        else
        {
            paymentType.text = getString(R.string.group_param_payment_free)
            paymentType.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorText))
            isPaid.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_money))
        }
    }

    private fun setMembersSection(groupNames: Map<GroupId, String>)
    {
        e("setMembers section: $currentGroup")
        membersCount.text = currentGroup.membersList.size.toString()

        val listener =  object: GroupMembersAdapter.RemovePersonListener {
            override fun onPersonRemoveFromGroup(person: Person)
            {
                presenter.onDeletePersonFromCurrentGroupClicked(person)
            }
        }

        membersAdapter = get { parametersOf(currentMembers, groupNames, listener) }

        shortMembersList.adapter = membersAdapter
        shortMembersList.layoutManager = LinearLayoutManager(context)

        addMember.setOnClickListener {
            presenter.onAddPeopleToGroupClicked()
        }

        if (membersAdapter.itemCount == 0)
        {
            noMembersState()
        }
        else
        {
            hasMembersState()
        }
    }

    private fun noMembersState()
    {
        contentView.visible = true
        loadingBar.visible = false
        shortMembersList.visible = false
        noMembersData.visible = true
    }

    private fun hasMembersState()
    {
        contentView.visible = true
        loadingBar.visible = false
        shortMembersList.visible = true
        noMembersData.visible = false
    }
}