package ru.hryasch.coachnotes.fragments.impl

import android.content.DialogInterface
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.updateLayoutParams
import androidx.core.widget.NestedScrollView
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.pawegio.kandroid.e
import com.pawegio.kandroid.i
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
import ru.hryasch.coachnotes.groups.presenters.impl.GroupPresenterImpl
import ru.hryasch.coachnotes.repository.common.toRelative
import java.util.LinkedList
import kotlin.math.roundToInt

class GroupInfoFragment : MvpAppCompatFragment(), GroupView, KoinComponent
{
    @InjectPresenter
    lateinit var presenter: GroupPresenterImpl
    private lateinit var navController: NavController

    // Common UI
    private lateinit var loadingBar: ProgressBar
    private lateinit var contentView: NestedScrollView

    // Toolbar
    private lateinit var toolbar: Toolbar
    private lateinit var editGroup: ImageButton
    private lateinit var groupJournalButton: ImageButton

    // Base section
    private lateinit var name: TextView

    // Ages & payment section
    private lateinit var absoluteAge: TextView
    private lateinit var relativeAge: TextView
    private lateinit var paymentType: TextView
    private lateinit var isPaid: ImageView

    // Group members action
        // UI
        private lateinit var membersView: View
        private lateinit var membersCount: TextView
        private lateinit var membersListView: RecyclerView
        private lateinit var membersAdapter: GroupMembersAdapter
        private lateinit var noMembersData: TextView
        private lateinit var addMember: AppCompatImageView

        // Dialogs
        private lateinit var addMemberVariantsDialog: AlertDialog


    // Data
    private lateinit var currentGroup: Group
    private lateinit var currentMembers: MutableList<Person>
    private var isFirstSetData = true



    @ExperimentalCoroutinesApi
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View?
    {
        val layout =  inflater.inflate(R.layout.fragment_group_info, container, false)

        inflateToolbarElements(layout)
        inflateBaseSection(layout)
        inflateAgesAndPaymentSection(layout)
        inflateMembersSection(layout)

        contentView = layout.findViewById(R.id.groupInfoContentView)
        loadingBar = layout.findViewById(R.id.groupInfoProgressBarLoading)

        navController = container!!.findNavController()
        presenter.applyInitialArgumentGroupAsync(GroupInfoFragmentArgs.fromBundle(requireArguments()).groupData)

        return layout
    }

    override fun setGroupData(group: Group, members: List<Person>, groupNames: Map<GroupId, String>)
    {
        i("set group data: $group")
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

    @ExperimentalCoroutinesApi
    override fun showAddPeopleToGroupNotification(people: List<Person>?)
    {
        if (people == null || people.isEmpty())
        {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Добавить учеников в группу")
                .setMessage("Нет свободных учеников")
                .setPositiveButton(android.R.string.ok) { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
                .show()
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

        addMemberVariantsDialog.dismiss()
        dialog.show()
    }



    private fun inflateToolbarElements(layout: View)
    {
        editGroup = layout.findViewById(R.id.groupInfoImageButtonEditPerson)
        groupJournalButton = layout.findViewById(R.id.groupInfoImageButtonJournal)

        toolbar = layout.findViewById(R.id.groupInfoToolbar)
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
        membersView = layout.findViewById(R.id.groupInfoViewMembers)
        membersCount = layout.findViewById(R.id.groupInfoTextViewMembersCount)
        membersListView = layout.findViewById(R.id.groupInfoRecyclerViewMembers)
        noMembersData = layout.findViewById(R.id.groupInfoTextViewNoData)
        addMember = layout.findViewById(R.id.groupInfoButtonAddMember)

        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_person_to_group, null)
        addMemberVariantsDialog = MaterialAlertDialogBuilder(requireContext())
                                    .setView(dialogView)
                                    .create()

        dialogView.findViewById<LinearLayout>(R.id.addPersonNewPerson).setOnClickListener {
            addMemberVariantsDialog.cancel()
            val action = GroupInfoFragmentDirections.actionGroupInfoFragmentToPersonEditFragment(lockGroup = currentGroup.id)
            navController.navigate(action)
        }

        dialogView.findViewById<LinearLayout>(R.id.addPersonFindPerson).setOnClickListener {
            addMemberVariantsDialog.cancel()
            presenter.onAddPeopleToGroupClicked()
        }
    }

    private fun setToolbarActions()
    {
        editGroup.setOnClickListener {
            val action = GroupInfoFragmentDirections.actionGroupInfoFragmentToGroupEditFragment(currentGroup)
            navController.navigate(action)
        }

        groupJournalButton.setOnClickListener {
            val action = GroupInfoFragmentDirections.actionGroupInfoFragmentToJournalGroupFragment(currentGroup)
            navController.navigate(action)
        }
    }

    private fun setAges()
    {
        val ageLow  = currentGroup.availableAbsoluteAgeLow
        val ageHigh = currentGroup.availableAbsoluteAgeHigh

        if (ageLow == null)
        {
            // age not set
            absoluteAge.text = getString(R.string.group_param_ages_multiple, "?", "?")
            relativeAge.text = getString(R.string.group_param_ages_multiple, "?", "?")
            return
        }

        // age set
        if (ageHigh == null)
        {
            // set only 1 age
            absoluteAge.text = getString(R.string.group_param_ages_single, ageLow.toString())
            relativeAge.text = getString(
                R.string.group_param_ages_single, ageLow.toRelative()
                    .toString())
        }
        else
        {
            // set age range
            absoluteAge.text = getString(R.string.group_param_ages_multiple, ageLow.toString(), ageHigh.toString())
            relativeAge.text = getString(
                R.string.group_param_ages_multiple, ageHigh.toRelative()
                    .toString(), ageLow.toRelative().toString())
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
            isPaid.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_baseline_money_off_24))
        }
    }

    private fun setMembersSection(groupNames: Map<GroupId, String>)
    {
        membersCount.text = currentGroup.membersList.size.toString()

        val listener =  object: GroupMembersAdapter.PersonActionListener {
            override fun onPersonRemoveFromGroup(person: Person)
            {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Удалене ученика из группы")
                    .setMessage("Вы уверены что хотите удалить ${person.surname} ${person.name} из группы ${currentGroup.name} ?")
                    .setPositiveButton("Удалить") { dialog, _ ->
                        presenter.deletePersonFromCurrentGroup(person.id)
                        dialog.dismiss()
                    }
                    .setNegativeButton("Отмена") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .create()
                    .show()
            }

            override fun onPersonClicked(person: Person)
            {
                val action = GroupInfoFragmentDirections.actionGroupInfoFragmentToPersonInfoFragment(person)
                navController.navigate(action)
            }
        }

        membersAdapter = get { parametersOf(currentMembers, groupNames, listener) }

        membersListView.adapter = membersAdapter
        membersListView.layoutManager = LinearLayoutManager(context)
        remeasureMembersListHeight()

        addMember.setOnClickListener {
            addMemberVariantsDialog.show()

            // Hack to set custom dialog width
            addMemberVariantsDialog.window!!.setLayout(resources.getDimension(R.dimen.group_info_add_person_dialog_width).toInt(),
                                                       ViewGroup.LayoutParams.WRAP_CONTENT)
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

    private fun remeasureMembersListHeight()
    {
        membersView.updateLayoutParams {
            height = getScreenHeight()
        }
    }

    private fun getScreenHeight(): Int
    {
        val displayMetrics: DisplayMetrics = requireContext().resources.displayMetrics
        return displayMetrics.heightPixels - toolbar.height - resources.getDimension(R.dimen.default_indent).toInt() * 3
    }

    private fun noMembersState()
    {
        contentView.visible = true
        loadingBar.visible = false
        membersListView.visible = false
        noMembersData.visible = true
    }

    private fun hasMembersState()
    {
        contentView.visible = true
        loadingBar.visible = false
        membersListView.visible = true
        noMembersData.visible = false
    }
}