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
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.pawegio.kandroid.d
import com.pawegio.kandroid.i
import com.pawegio.kandroid.visible
import com.soywiz.klock.Date
import com.tiper.MaterialSpinner
import moxy.MvpAppCompatFragment
import moxy.presenter.InjectPresenter
import org.koin.core.KoinComponent
import org.koin.core.get
import org.koin.core.qualifier.named
import ru.hryasch.coachnotes.R
import ru.hryasch.coachnotes.application.App
import ru.hryasch.coachnotes.domain.common.GroupId
import ru.hryasch.coachnotes.domain.group.data.Group
import ru.hryasch.coachnotes.domain.person.data.Person
import ru.hryasch.coachnotes.fragments.PersonEditView
import ru.hryasch.coachnotes.people.presenters.impl.PersonEditPresenterImpl
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.collections.HashMap

class PersonEditFragment : MvpAppCompatFragment(), PersonEditView, KoinComponent
{
    @InjectPresenter
    lateinit var presenter: PersonEditPresenterImpl

    private lateinit var navController: NavController

    private lateinit var contentView: NestedScrollView
    private lateinit var loadingBar: ProgressBar

    // Toolbar
    private lateinit var saveOrCreatePerson: MaterialButton

    // Base section
    private lateinit var deletePerson: MaterialButton

    // General section
    private lateinit var surname: TextInputEditText
    private lateinit var name: TextInputEditText
    private lateinit var patronymic: TextInputEditText

    // Birthday section
    private lateinit var birthdayDay: MaterialSpinner
    private lateinit var birthdayMonth: MaterialSpinner
    private lateinit var birthdayYear: MaterialSpinner
    private lateinit var relativeYears: TextView
    private lateinit var daysOfMonthList: List<String>
    private lateinit var monthsList: List<String>
    private lateinit var yearsList: List<String>
    private var selectedDay: Int = -1
    private var selectedMonth: Int = -1
    private var selectedYear: Int = -1

    // Group section
    private lateinit var groupChooser: MaterialSpinner
    private val groupIdByPosition: MutableMap<Int, Pair<GroupId?, Boolean>> = HashMap()
    private val groupPositionById: MutableMap<GroupId?, Int> = HashMap()

    // Relatives section


    private lateinit var currentPerson: Person


    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View?
    {
        val layout = inflater.inflate(R.layout.fragment_edit_person, container, false)

        inflateToolbarElements(layout)
        inflateBaseSection(layout)
        inflateGeneralSection(layout)
        inflateBirthdaySection(layout)
        inflateGroupSection(layout)

        contentView = layout.findViewById(R.id.personEditContent)
        loadingBar = layout.findViewById(R.id.personEditProgressBarLoading)

        navController = container!!.findNavController()

        presenter.applyPersonDataAsync(PersonEditFragmentArgs.fromBundle(requireArguments()).personData)

        setSaveOrCreateButtonDisabled()

        return layout
    }

    override fun setPersonData(person: Person, groups: List<Group>)
    {
        currentPerson = person

        val isExistPerson = currentPerson.surname.isNotBlank()
        if (isExistPerson)
        {
            setExistPersonData()
        }

        showingState(!isExistPerson)

        groupChooser.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, getGroupNamesSpinnerData(groups))
        groupChooser.onItemSelectedListener = object : MaterialSpinner.OnItemSelectedListener
        {
            override fun onItemSelected(parent: MaterialSpinner,
                                        view: View?,
                                        position: Int,
                                        id: Long)
            {
                currentPerson.groupId = groupIdByPosition[position]?.first
            }

            override fun onNothingSelected(parent: MaterialSpinner)
            {
            }
        }

        i("selected person.groupId = ${person.groupId}")

        groupChooser.selection = groupPositionById[person.groupId]!!

        saveOrCreatePerson.setOnClickListener {
            currentPerson.surname = surname.text.toString()
            currentPerson.name = name.text.toString()
            currentPerson.patronymic = patronymic.text?.toString()

            if (selectedDay > 0 && selectedMonth > 0 && selectedYear > 0)
            {
                currentPerson.birthday = Date.Companion.invoke(selectedYear, selectedMonth, selectedDay)
            }
            else
            {
                currentPerson.birthday = null
            }

            if (groupChooser.selection == MaterialSpinner.INVALID_POSITION)
            {
                currentPerson.groupId = null
                currentPerson.isPaid = false
            }
            else
            {
                currentPerson.groupId = groupIdByPosition[groupChooser.selection]!!.first
                currentPerson.isPaid = groupIdByPosition[groupChooser.selection]!!.second
            }

            presenter.updateOrCreatePerson()
        }
    }

    override fun loadingState()
    {
        contentView.visible = false
        loadingBar.visible = true
        saveOrCreatePerson.visible = false
    }

    override fun deletePersonFinished()
    {
        navController.popBackStack()
        navController.navigateUp()
    }

    override fun updateOrCreatePersonFinished()
    {
        navController.navigateUp()
    }

    override fun showDeletePersonNotification(person: Person?)
    {
        if (person == null)
        {
            return
        }

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setMessage("Удалить ученика?")
            .setPositiveButton("Удалить") { dialog, _ ->
                dialog.cancel()
                presenter.deletePerson(currentPerson)
            }
            .setNegativeButton("Отмена") { dialog, _ ->
                dialog.cancel()
            }
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setTextColor(ContextCompat.getColor(App.getCtx(), R.color.colorAccent))
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                .setTextColor(ContextCompat.getColor(App.getCtx(), R.color.colorPrimaryLight))
        }

        dialog.show()
    }


    private fun showingState(isNewPerson: Boolean)
    {
        contentView.visible = true
        loadingBar.visible = false
        saveOrCreatePerson.visible = true
        deletePerson.visible = !isNewPerson
    }

    private fun inflateToolbarElements(layout: View)
    {
        saveOrCreatePerson = layout.findViewById(R.id.personEditButtonCreateOrSave)

        val toolbar: Toolbar = layout.findViewById(R.id.personEditToolbar)
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        (activity as AppCompatActivity).supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        (activity as AppCompatActivity).supportActionBar!!.setDisplayShowHomeEnabled(true)

        toolbar.setNavigationOnClickListener {
            navController.navigateUp()
        }
    }

    private fun inflateBaseSection(layout: View)
    {
        deletePerson = layout.findViewById(R.id.personEditButtonRemovePerson)
    }

    private fun inflateGeneralSection(layout: View)
    {
        surname = layout.findViewById(R.id.editPersonEditTextSurname)
        name = layout.findViewById(R.id.editPersonEditTextName)
        patronymic = layout.findViewById(R.id.editPersonEditTextPatronymic)

        surname.addTextChangedListener(object: TextWatcher
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
    }

    private fun inflateBirthdaySection(layout: View)
    {
        birthdayDay = layout.findViewById(R.id.personEditBirthdaySpinnerDay)
        birthdayMonth = layout.findViewById(R.id.personEditBirthdaySpinnerMonth)
        birthdayYear = layout.findViewById(R.id.personEditBirthdaySpinnerYear)
        relativeYears = layout.findViewById(R.id.personEditRelativeAges)

        daysOfMonthList = get(named("monthDays"))
        val monthsArray: Array<String> = get(named("months_RU"))
        monthsList = monthsArray.toList()
        yearsList = get(named("absoluteAgesList"))

        birthdayDay.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, daysOfMonthList)
        birthdayMonth.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, monthsList)
        birthdayYear.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, yearsList)
        relativeYears.text = getString(R.string.person_edit_screen_relative_age_pattern, "?")

        birthdayDay.onItemSelectedListener = object: MaterialSpinner.OnItemSelectedListener {
            override fun onItemSelected(parent: MaterialSpinner,
                                        view: View?,
                                        position: Int,
                                        id: Long)
            {
                selectedDay = daysOfMonthList[position].toInt()
                d("selected day: $selectedDay")
                setRelativeAgeIfPossible()
                checkRequiredFields()
            }

            override fun onNothingSelected(parent: MaterialSpinner)
            {
            }
        }

        birthdayMonth.onItemSelectedListener = object: MaterialSpinner.OnItemSelectedListener {
            override fun onItemSelected(parent: MaterialSpinner,
                                        view: View?,
                                        position: Int,
                                        id: Long)
            {
                selectedMonth = position + 1
                d("selected month: ${monthsList[position]}, $selectedMonth")
                setRelativeAgeIfPossible()
                checkRequiredFields()
            }

            override fun onNothingSelected(parent: MaterialSpinner)
            {
            }
        }

        birthdayYear.onItemSelectedListener = object: MaterialSpinner.OnItemSelectedListener {
            override fun onItemSelected(parent: MaterialSpinner,
                                        view: View?,
                                        position: Int,
                                        id: Long)
            {
                selectedYear = yearsList[position].toInt()
                d("selected year = $selectedYear")
                setRelativeAgeIfPossible()
                checkRequiredFields()
            }

            override fun onNothingSelected(parent: MaterialSpinner)
            {
            }
        }
    }

    private fun inflateGroupSection(layout: View)
    {
        groupChooser = layout.findViewById(R.id.personEditSpinnerGroup)
    }

    private fun setExistPersonData()
    {
        (activity as AppCompatActivity).supportActionBar!!.setTitle(R.string.person_edit_screen_toolbar_title)
        saveOrCreatePerson.text = getString(R.string.save)

        surname.text = SpannableStringBuilder(currentPerson.surname)
        name.text = SpannableStringBuilder(currentPerson.name)

        currentPerson.patronymic?.let {
            patronymic.text = SpannableStringBuilder(it)
        }

        currentPerson.birthday?.let {
            selectedDay = it.day
            selectedMonth = it.month.index1
            selectedYear = it.year

            birthdayDay.selection = daysOfMonthList.indexOf(selectedDay.toString())
            birthdayMonth.selection = selectedMonth - 1
            birthdayYear.selection = yearsList.indexOf(selectedYear.toString())
        }

        deletePerson.setOnClickListener {
            presenter.onDeletePersonClicked()
        }
    }

    private fun getGroupNamesSpinnerData(groups: List<Group>): List<String>
    {
        val groupsListItems = LinkedList<String>()
        groupsListItems.add("Нет группы")
        groupIdByPosition[0] = Pair(null, false)
        groupPositionById[null] = 0

        val groupListSorted = groups.sorted()
        for ((i, group) in groupListSorted.withIndex())
        {
            groupIdByPosition[i + 1] = Pair(group.id, group.isPaid)
            groupPositionById[group.id] = i + 1

            var age1 = "?"
            var age2 = "?"

            if (group.availableAbsoluteAge != null)
            {
                age1 = group.availableAbsoluteAge!!.first.toString()
                age2 = group.availableAbsoluteAge!!.last.toString()
            }

            val paidSuffix =
                if (group.isPaid)
                {
                    " $"
                }
                else
                {
                    ""
                }

            if (age1 == age2)
            {
                groupsListItems.add(getString(R.string.person_edit_screen_group_pattern_single, group.name, age1) + paidSuffix)
            }
            else
            {
                groupsListItems.add(getString(R.string.person_edit_screen_group_pattern_range, group.name, age1, age2) + paidSuffix)
            }
        }

        return groupsListItems
    }

    private fun setRelativeAgeIfPossible()
    {
        if (selectedDay < 0 || selectedMonth < 0 || selectedYear < 0)
        {
            relativeYears.text = getString(R.string.person_edit_screen_relative_age_pattern, "?")
            return
        }

        val birthdayDate = LocalDate.of(selectedYear, selectedMonth, selectedDay)
        val nowDate = LocalDate.now()
        val diffYears = ChronoUnit.YEARS.between(birthdayDate, nowDate)
        i("diff years = $diffYears")
        relativeYears.text = getString(R.string.person_edit_screen_relative_age_pattern, diffYears.toString())
    }

    private fun checkRequiredFields()
    {
        if (!surname.text.isNullOrBlank() && !name.text.isNullOrBlank() && isBirthdaySet())
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
        saveOrCreatePerson.isEnabled = true
        saveOrCreatePerson.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.colorAccent))
        saveOrCreatePerson.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorText))
    }

    private fun setSaveOrCreateButtonDisabled()
    {
        saveOrCreatePerson.isEnabled = false
        saveOrCreatePerson.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.colorAccentDisabled))
        saveOrCreatePerson.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorDisabledText))
    }

    private fun isBirthdaySet(): Boolean
    {
        return (birthdayDay.selectedItem != null) &&
               (birthdayMonth.selectedItem != null) &&
               (birthdayYear.selectedItem != null)
    }
}