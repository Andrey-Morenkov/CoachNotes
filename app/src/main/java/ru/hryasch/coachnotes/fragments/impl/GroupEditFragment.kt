package ru.hryasch.coachnotes.fragments.impl

import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ProgressBar
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
import com.google.android.material.textfield.TextInputEditText
import com.pawegio.kandroid.e
import com.pawegio.kandroid.visible
import com.tiper.MaterialSpinner
import moxy.MvpAppCompatFragment
import moxy.presenter.InjectPresenter
import org.koin.core.KoinComponent
import org.koin.core.get
import org.koin.core.qualifier.named
import ru.hryasch.coachnotes.R
import ru.hryasch.coachnotes.activity.MainActivity
import ru.hryasch.coachnotes.application.App
import ru.hryasch.coachnotes.domain.group.data.Group
import ru.hryasch.coachnotes.domain.group.data.ScheduleDay
import ru.hryasch.coachnotes.fragments.GroupEditView
import ru.hryasch.coachnotes.groups.data.ScheduleDayAdapter
import ru.hryasch.coachnotes.groups.presenters.impl.GroupEditPresenterImpl
import ru.hryasch.coachnotes.repository.common.toRelative
import java.time.ZonedDateTime
import java.util.*
import kotlin.collections.ArrayList

class GroupEditFragment : MvpAppCompatFragment(), GroupEditView, KoinComponent
{
    @InjectPresenter
    lateinit var presenter: GroupEditPresenterImpl

    private lateinit var navController: NavController

    private lateinit var saveOrCreateGroup: MaterialButton
    private lateinit var deleteGroup: MaterialButton

    private lateinit var name: TextInputEditText
    private lateinit var paymentType: MaterialSpinner
    private lateinit var age1: MaterialSpinner
    private lateinit var age2: MaterialSpinner
    private lateinit var ageType: MaterialSpinner

    private lateinit var scheduleDaysView: RecyclerView
    private val scheduleDaysList: MutableList<ScheduleDay> = LinkedList()
    private lateinit var scheduleDaysAdapter: ScheduleDayAdapter

    private lateinit var contentView: NestedScrollView
    private lateinit var loadingBar: ProgressBar

    private lateinit var currentGroup: Group

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View?
    {
        val layout = inflater.inflate(R.layout.fragment_edit_group, container, false)

        (activity as MainActivity).hideBottomNavigation()

        saveOrCreateGroup = layout.findViewById(R.id.groupEditButtonCreateOrSave)
        deleteGroup = layout.findViewById(R.id.groupEditButtonRemoveGroup)

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

        val toolbar: Toolbar = layout.findViewById(R.id.groupEditToolbar)
        (activity as AppCompatActivity).supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        (activity as AppCompatActivity).supportActionBar!!.setDisplayShowHomeEnabled(true)

        toolbar.setNavigationOnClickListener {
            navController.navigateUp()
        }

        presenter.applyInitialArgumentGroupAsync(GroupEditFragmentArgs.fromBundle(requireArguments()).groupData)

        deleteGroup.visible = false
        setSaveOrCreateButtonDisabled()

        return layout
    }

    override fun setGroupData(group: Group)
    {
        contentView.visible = true
        loadingBar.visible = false

        currentGroup = group

        paymentType.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, generatePaymentTypes())
        age1.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, generateAbsoluteYears())
        age2.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, generateAbsoluteYears())
        ageType.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, generateAgeTypes())

        ageType.selection = 0

        if (group.name.isNotBlank())
        {
            setExistGroupData()
        }

        name.addTextChangedListener(object: TextWatcher
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

                                    })

        paymentType.onItemSelectedListener = object: MaterialSpinner.OnItemSelectedListener
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

        age1.onItemSelectedListener = object: MaterialSpinner.OnItemSelectedListener
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

        ageType.onItemSelectedListener = object: MaterialSpinner.OnItemSelectedListener
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

                        age1.adapter = ArrayAdapter(context!!, android.R.layout.simple_list_item_1, generateAbsoluteYears())
                        age2.adapter = ArrayAdapter(context!!, android.R.layout.simple_list_item_1, generateAbsoluteYears())

                        if (newAge1Selection != null && newAge2Selection != null)
                        {
                            age1.selection = newAge2Selection.toInt()
                            age2.selection = newAge1Selection.toInt()
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

                        age1.adapter = ArrayAdapter(context!!, android.R.layout.simple_list_item_1, generateRelativeYears())
                        age2.adapter = ArrayAdapter(context!!, android.R.layout.simple_list_item_1, generateRelativeYears())

                        if (newAge1Selection != null && newAge2Selection != null)
                        {
                            age1.selection = newAge2Selection.toInt().toRelative()
                            age2.selection = newAge1Selection.toInt().toRelative()
                        }
                        else
                        {
                            age1.selectedItem?.let {
                                age1.selection = newAge1Selection?.toRelative()?.toInt() ?: MaterialSpinner.INVALID_POSITION
                            }

                            age2.selectedItem?.let {
                                age2.selection = newAge2Selection?.toRelative()?.toInt() ?: MaterialSpinner.INVALID_POSITION
                            }
                        }
                    }
                }
            }

            override fun onNothingSelected(parent: MaterialSpinner)
            {
            }
        }

        saveOrCreateGroup.setOnClickListener {
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
                e("save scheduleDay: $scheduleDay")
                if (scheduleDay.isNotBlank())
                {
                    currentGroup.scheduleDays.add(scheduleDay)
                }
            }

            presenter.updateOrCreateGroup()
        }

        setSchedule()

        checkRequiredFields()
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

    override fun showDeleteGroupNotification(group: Group?)
    {
        if (group == null)
        {
            return
        }

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setMessage("Удалить группу и все связанные с ней журналы?")
            .setPositiveButton("Удалить") { dialog, _ ->
                dialog.cancel()
                presenter.deleteGroup(currentGroup)
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

    private fun setExistGroupData()
    {
        deleteGroup.visible = true
        (activity as AppCompatActivity).supportActionBar!!.setTitle(R.string.group_edit_screen_toolbar_title)
        saveOrCreateGroup.text = getString(R.string.save)

        name.text = SpannableStringBuilder(currentGroup.name)
        paymentType.selection = currentGroup.isPaid.toInt()

        currentGroup.availableAbsoluteAge?.let {
            age1.selection = it.first.toRelative()
            age2.selection = it.last.toRelative()
        }

        deleteGroup.setOnClickListener {
            presenter.onDeleteGroupClicked()
        }
    }

    private fun setSchedule()
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
        scheduleDaysView.adapter = scheduleDaysAdapter
        scheduleDaysView.layoutManager = LinearLayoutManager(context)
    }

    private fun generateAbsoluteYears(): List<String>
    {
        val currYear = ZonedDateTime.now().year
        val ages = ArrayList<String>(50)
        for (i in 0 until 50)
        {
            ages.add("${currYear - i}")
        }

        return ages
    }

    private fun generateRelativeYears(): List<String>
    {
        val ages = ArrayList<String>(50)
        for (i in 0 until 50)
        {
            ages.add("$i")
        }

        return ages
    }

    private fun generatePaymentTypes(): List<String> = listOf(getString(R.string.group_param_payment_free),
                                                              getString(R.string.group_param_payment_paid))

    private fun generateAgeTypes(): List<String> = listOf(getString(R.string.age_type_absolute),
                                                          getString(R.string.age_type_relative))

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