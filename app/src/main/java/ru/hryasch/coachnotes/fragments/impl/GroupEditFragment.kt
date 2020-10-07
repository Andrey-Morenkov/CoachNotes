package ru.hryasch.coachnotes.fragments.impl

import android.content.DialogInterface
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.pawegio.kandroid.toast
import com.pawegio.kandroid.visible
import com.tiper.MaterialSpinner
import kotlinx.coroutines.*
import moxy.MvpAppCompatFragment
import moxy.presenter.InjectPresenter
import org.koin.core.KoinComponent
import org.koin.core.get
import org.koin.core.inject
import org.koin.core.qualifier.named
import ru.hryasch.coachnotes.R
import ru.hryasch.coachnotes.activity.MainActivity
import ru.hryasch.coachnotes.domain.group.data.Group
import ru.hryasch.coachnotes.domain.group.data.ScheduleDay
import ru.hryasch.coachnotes.fragments.GroupEditView
import ru.hryasch.coachnotes.groups.data.ScheduleDayAdapter
import ru.hryasch.coachnotes.groups.presenters.impl.GroupEditPresenterImpl
import ru.hryasch.coachnotes.repository.common.toAbsolute
import ru.hryasch.coachnotes.repository.common.toRelative
import java.util.*

class GroupEditFragment : MvpAppCompatFragment(), GroupEditView, KoinComponent
{
    @InjectPresenter
    lateinit var presenter: GroupEditPresenterImpl
    private lateinit var navController: NavController
    private lateinit var currentGroup: Group

    // Toolbar
        // UI
        private lateinit var toolbar: Toolbar
        private lateinit var saveOrCreateGroup: MaterialButton

        // Dialogs
        private lateinit var saveOrCreateGroupWithoutScheduleWarningDialog: AlertDialog

    // Common
        // UI
        private lateinit var deleteGroup: MaterialButton
        private lateinit var contentView: NestedScrollView
        private lateinit var loadingBar: ProgressBar

        // Dialogs
        private lateinit var deleteGroupVariantsDialog: AlertDialog

    // General section
        // UI
        private lateinit var name: TextInputEditText
        private lateinit var paymentType: MaterialSpinner
        private lateinit var age1: MaterialSpinner
        private lateinit var age2: MaterialSpinner
        private lateinit var ageType: MaterialSpinner

        // Utility
        private val absoluteYears: List<String> by inject(named("absoluteAgesList"))
        private val relativeYears: List<String> by inject(named("relativeAgesList"))
        private val paymentTypes:  List<String> by inject(named("paymentTypes"))
        private val ageTypes:      List<String> by inject(named("ageTypes"))
        private lateinit var absoluteAgesAdapter: ArrayAdapter<String>
        private lateinit var relativeAgesAdapter: ArrayAdapter<String>

    // Schedule section
        // UI
        private lateinit var scheduleDaysView: RecyclerView

        // Utility
        private val scheduleDaysList: MutableList<ScheduleDay> = LinkedList()
        private lateinit var scheduleDaysAdapter: ScheduleDayAdapter

    private lateinit var setGroupDataJob: Job



    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View?
    {
        val layout = inflater.inflate(R.layout.fragment_edit_group, container, false)

        (activity as MainActivity).hideBottomNavigation()

        saveOrCreateGroup = layout.findViewById(R.id.groupEditButtonCreateOrSave)
        setSaveOrCreateButtonDisabled()
        deleteGroup = layout.findViewById(R.id.groupEditButtonRemoveGroup)
        deleteGroup.visible = false

        name = layout.findViewById(R.id.groupEditTextInputName)
        paymentType = layout.findViewById(R.id.groupEditSpinnerPaymentType)
        age1 = layout.findViewById(R.id.groupEditSpinnerAge1)
        age2 = layout.findViewById(R.id.groupEditSpinnerAge2)
        ageType = layout.findViewById(R.id.groupEditSpinnerAgeType)

        scheduleDaysView = layout.findViewById(R.id.group_edit_schedule_list)

        contentView = layout.findViewById(R.id.groupEditContent)
        loadingBar = layout.findViewById(R.id.groupEditProgressBarLoading)

        loadingState()

        navController = container!!.findNavController()

        toolbar = layout.findViewById(R.id.groupEditToolbar)
        toolbar.setNavigationOnClickListener {
            navController.navigateUp()
        }

        presenter.applyInitialArgumentGroupAsync(GroupEditFragmentArgs.fromBundle(requireArguments()).groupData)

        return layout
    }

    override fun setGroupData(group: Group)
    {
        if (::setGroupDataJob.isInitialized && setGroupDataJob.isActive)
        {
            setGroupDataJob.cancel()
        }

        if (group.name.isNotBlank())
        {
            toolbar.setTitle(R.string.group_edit_screen_toolbar_title)
            saveOrCreateGroup.text = getString(R.string.save)
        }
        currentGroup = group

        setGroupDataJob =
            GlobalScope.launch(Dispatchers.Main)
            {
                delay(500) //hotfix for animation
                setAgesAdapters()
                createSaveOrCreateGroupWithoutScheduleWarningDialog(group.name.isBlank())
                createDeleteGroupVariantsDialog()
                if (group.name.isNotBlank())
                {
                    setExistGroupData()
                }
                setListeners()
                setSchedule()

                withContext(Dispatchers.Main)
                {
                    checkRequiredFields()
                    showingState()
                }
            }
    }

    override fun loadingState()
    {
        contentView.visible = false
        loadingBar.visible = true
    }

    override fun deleteGroupFinished()
    {
        navController.popBackStack()
        navController.navigateUp()
    }

    override fun updateOrCreateGroupFinished()
    {
        navController.navigateUp()
    }



    private suspend fun setAgesAdapters()
    {
        absoluteAgesAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, absoluteYears)
        relativeAgesAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, relativeYears)
        val paymentAdapter = getPaymentTypesAdapter()
        val ageAdapter = getAgeTypesAdapter()

        withContext(Dispatchers.Main)
        {
            paymentType.adapter = paymentAdapter
            age1.adapter = absoluteAgesAdapter
            age2.adapter = absoluteAgesAdapter
            ageType.adapter = ageAdapter
            ageType.selection = 0
        }
    }

    private suspend fun setListeners()
    {
        withContext(Dispatchers.Main)
        {
            name.addTextChangedListener(getDefaultTextChangedListener())
            paymentType.onItemSelectedListener = getDefaultItemSelectedListener()
            age1.onItemSelectedListener = getDefaultItemSelectedListener()
            // age2 is not required => no need to add listener
            ageType.onItemSelectedListener = getAgeTypeItemSelectedListener()

            saveOrCreateGroup.setOnClickListener {
                var hasScheduleDays = false
                for (scheduleDay in scheduleDaysList)
                {
                    if (scheduleDay.isNotBlank())
                    {
                        hasScheduleDays = true
                        break
                    }
                }

                if (!hasScheduleDays)
                {
                    saveOrCreateGroupWithoutScheduleWarningDialog.show()
                }
                else
                {
                    createOrUpdateGroupAction()
                }
            }
        }
    }

    private suspend fun setSchedule()
    {
        val dayOfWeekNames: Array<String> = get(named("daysOfWeekLong_RU"))
        for ((i, dayOfWeek) in dayOfWeekNames.withIndex())
        {
            scheduleDaysList.add(ScheduleDay(dayOfWeek, i))
        }

        for (existedDayOfWeek in currentGroup.scheduleDays)
        {
            val scheduleDay = scheduleDaysList[existedDayOfWeek.dayPosition0]
            scheduleDay.startTime = existedDayOfWeek.startTime
            scheduleDay.endTime = existedDayOfWeek.endTime
        }

        scheduleDaysAdapter = ScheduleDayAdapter(scheduleDaysList, requireContext())

        withContext(Dispatchers.Main)
        {
            scheduleDaysView.adapter = scheduleDaysAdapter
            scheduleDaysView.layoutManager = LinearLayoutManager(context)
        }
    }



    private fun showingState()
    {
        contentView.visible = true
        loadingBar.visible = false
    }

    private fun getDefaultTextChangedListener(): TextWatcher
    {
        return object: TextWatcher
        {
            override fun afterTextChanged(s: Editable?)
            {
                checkRequiredFields()
            }

            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            )
            {
            }

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            )
            {
            }
        }
    }

    private fun getDefaultItemSelectedListener(): MaterialSpinner.OnItemSelectedListener
    {
        return object: MaterialSpinner.OnItemSelectedListener
        {
            override fun onItemSelected(
                parent: MaterialSpinner,
                view: View?,
                position: Int,
                id: Long
            )
            {
                checkRequiredFields()
            }

            override fun onNothingSelected(parent: MaterialSpinner)
            {
            }
        }
    }

    private fun getAgeTypeItemSelectedListener(): MaterialSpinner.OnItemSelectedListener
    {
        return object: MaterialSpinner.OnItemSelectedListener
        {
            override fun onItemSelected(parent: MaterialSpinner,
                                        view: View?,
                                        position: Int,
                                        id: Long)
            {
                when(position)
                {
                    // absolute
                    0 ->
                    {
                        val newAge1Selection = age1.selectedItem?.toString()?.toInt()
                        val newAge2Selection = age2.selectedItem?.toString()?.toInt()

                        newAge1Selection?.let {
                            if (it > 1000) return
                        }

                        newAge2Selection?.let {
                            if (it > 1000) return
                        }

                        age1.adapter = absoluteAgesAdapter
                        age2.adapter = absoluteAgesAdapter

                        if (newAge1Selection != null && newAge2Selection != null)
                        {
                            age1.selection = absoluteYears.indexOf(newAge2Selection.toAbsolute().toString())
                            age2.selection = absoluteYears.indexOf(newAge1Selection.toAbsolute().toString())
                        }
                        else
                        {
                            age1.selectedItem?.let {
                                age1.selection = newAge1Selection?.toInt() ?: MaterialSpinner.INVALID_POSITION
                            }

                            age2.selectedItem?.let {
                                age2.selection = newAge2Selection?.toInt() ?: MaterialSpinner.INVALID_POSITION
                            }
                        }
                    }

                    // relative
                    1 ->
                    {
                        val newAge1Selection = age1.selectedItem?.toString()?.toInt()
                        val newAge2Selection = age2.selectedItem?.toString()?.toInt()

                        newAge1Selection?.let {
                            if (it < 1000) return
                        }

                        newAge2Selection?.let {
                            if (it < 1000) return
                        }

                        age1.adapter = relativeAgesAdapter
                        age2.adapter = relativeAgesAdapter

                        if (newAge1Selection != null && newAge2Selection != null)
                        {
                            age1.selection = relativeYears.indexOf(newAge2Selection.toRelative().toString())
                            age2.selection = relativeYears.indexOf(newAge1Selection.toRelative().toString())
                        }
                        else
                        {
                            age1.selectedItem?.let {
                                age1.selection = relativeYears.indexOf(newAge1Selection!!.toRelative().toString())
                            }

                            age2.selectedItem?.let {
                                age2.selection = relativeYears.indexOf(newAge2Selection!!.toRelative().toString())
                            }
                        }
                    }
                }
            }

            override fun onNothingSelected(parent: MaterialSpinner)
            {
            }
        }
    }

    private fun getPaymentTypesAdapter() =  ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, paymentTypes)
    private fun getAgeTypesAdapter() = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, ageTypes)

    private suspend fun setExistGroupData()
    {
        withContext(Dispatchers.Main)
        {
            deleteGroup.visible = true

            name.text = SpannableStringBuilder(currentGroup.name)
            paymentType.selection = currentGroup.isPaid.toInt()

            currentGroup.availableAbsoluteAge?.let {
                age1.selection = absoluteYears.indexOf(it.first.toString())
                age2.selection = absoluteYears.indexOf(it.last.toString())
            }

            deleteGroup.setOnClickListener {
                deleteGroupVariantsDialog.show()

                // Hack to set custom dialog width
                deleteGroupVariantsDialog.window!!.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, resources.getDimension(R.dimen.group_edit_delete_group_dialog_height).toInt())
            }
        }
    }


    private fun createDeleteGroupVariantsDialog()
    {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_delete_group, null)
        deleteGroupVariantsDialog = MaterialAlertDialogBuilder(requireContext())
                                       .setView(dialogView)
                                       .setTitle("Удалить группу, а так же ...")
                                       .create()

        dialogView.findViewById<LinearLayout>(R.id.deleteGroupRemoveAllPeopleFromGroup).setOnClickListener {
            presenter.deleteGroupAndRemoveAllPeopleFromThisGroup(currentGroup)
            deleteGroupVariantsDialog.dismiss()
        }

        dialogView.findViewById<LinearLayout>(R.id.deleteGroupMoveAllPeopleToAnotherGroup).setOnClickListener {
            // TODO
            //presenter.deleteGroupAndMoveAllPeopleToAnotherGroup(currentGroup, currentGroup)
            toast("Еще не реализовано")
            deleteGroupVariantsDialog.dismiss()
        }

        dialogView.findViewById<LinearLayout>(R.id.deleteGroupDeleteAllPeople).setOnClickListener {
            // TODO
            //presenter.deleteGroupAnDeleteAllPeople(currentGroup)
            toast("Еще не реализовано")
            deleteGroupVariantsDialog.dismiss()
        }
    }

    private suspend fun createSaveOrCreateGroupWithoutScheduleWarningDialog(isCreatingNewGroup: Boolean)
    {
        withContext(Dispatchers.Main)
        {
            val builder =  MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.group_edit_screen_no_schedule_title)
                .setMessage(R.string.group_edit_screen_no_schedule_message)
                .setPositiveButton(R.string.group_edit_screen_no_schedule_cancel) { dialog, _ -> dialog.dismiss()}

            val saveOrUpdateListener = DialogInterface.OnClickListener { dialog, _ ->
                createOrUpdateGroupAction()
                dialog.dismiss()
            }

            if (isCreatingNewGroup)
            {
                builder.setNegativeButton(R.string.group_edit_screen_no_schedule_force_create_button, saveOrUpdateListener)
            }
            else
            {
                builder.setNegativeButton(R.string.group_edit_screen_no_schedule_force_update_button, saveOrUpdateListener)
            }

            saveOrCreateGroupWithoutScheduleWarningDialog = builder.create()
        }
    }

    private fun createOrUpdateGroupAction()
    {
        currentGroup.name = name.text.toString()
        currentGroup.isPaid = paymentType.selection.toBoolean()

        ageType.selection = 0
        val ageStart = age1.selectedItem?.toString()?.toInt()
        val ageFinish = age2.selectedItem?.toString()?.toInt()

        if (ageStart != null)
        {
            if (ageFinish != null)
            {
                currentGroup.availableAbsoluteAge = ageStart .. ageFinish
            }
            else
            {
                currentGroup.availableAbsoluteAge = ageStart .. ageStart
            }
        }
        else
        {
            currentGroup.availableAbsoluteAge = null
        }

        currentGroup.scheduleDays.clear()
        for (scheduleDay in scheduleDaysList)
        {
            if (scheduleDay.isNotBlank())
            {
                currentGroup.scheduleDays.add(scheduleDay)
            }
        }

        presenter.updateOrCreateGroup()
    }

    private fun checkRequiredFields()
    {
        if (!name.text.isNullOrBlank() && (paymentType.selectedItem != null) && (age1.selectedItem != null))
        {
            setSaveOrCreateButtonEnabled()
        }
        else
        {
            setSaveOrCreateButtonDisabled()
        }
    }

    private fun setSaveOrCreateButtonEnabled()
    {
        saveOrCreateGroup.isEnabled = true
        saveOrCreateGroup.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.colorAccent))
        saveOrCreateGroup.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorText))
    }

    private fun setSaveOrCreateButtonDisabled()
    {
        saveOrCreateGroup.isEnabled = false
        saveOrCreateGroup.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.colorAccentDisabled))
        saveOrCreateGroup.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorDisabledText))
    }
}

private fun Boolean.toInt(): Int = if (this) 1 else 0

private fun Int.toBoolean(): Boolean = this != 0