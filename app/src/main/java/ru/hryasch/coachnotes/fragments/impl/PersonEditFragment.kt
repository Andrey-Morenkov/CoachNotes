package ru.hryasch.coachnotes.fragments.impl

import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.pawegio.kandroid.d
import com.pawegio.kandroid.i
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
import ru.hryasch.coachnotes.domain.common.GroupId
import ru.hryasch.coachnotes.domain.group.data.Group
import ru.hryasch.coachnotes.domain.person.data.Person
import ru.hryasch.coachnotes.fragments.PersonEditView
import ru.hryasch.coachnotes.people.data.OnDeleteRelativeInfoHolder
import ru.hryasch.coachnotes.people.data.RelativeInfoHolder
import ru.hryasch.coachnotes.people.presenters.impl.PersonEditPresenterImpl
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class PersonEditFragment : MvpAppCompatFragment(), PersonEditView, KoinComponent
{
    @InjectPresenter
    lateinit var presenter: PersonEditPresenterImpl
    private lateinit var navController: NavController

    // Common UI
    private lateinit var contentView: NestedScrollView
    private lateinit var loadingBar: ProgressBar
    private val additionalViews: MutableList<View> = LinkedList()

    // Toolbar
    private lateinit var saveOrCreatePerson: MaterialButton

    // Base section
    private lateinit var deletePerson: MaterialButton

    // General section
    private lateinit var surname: TextInputEditText
    private lateinit var name: TextInputEditText
    private lateinit var patronymicContainer: View // Additional View
    private lateinit var patronymic: TextInputEditText

    // Birthday section
        // UI
        private lateinit var birthdayTitle: View // Additional view
        private lateinit var birthdayDay: MaterialSpinner // Additional view
        private lateinit var birthdayMonth: MaterialSpinner // Additional view
        private lateinit var birthdayYear: MaterialSpinner
        private lateinit var relativeYears: TextView

        // Data
        private lateinit var daysOfMonthList: List<String>
        private lateinit var monthsList: List<String>
        private lateinit var yearsList: List<String>
        private var selectedDay: Int = -1
        private var selectedMonth: Int = -1
        private var selectedYear: Int = -1

    // Group section
        // UI
        private lateinit var groupChooser: MaterialSpinner
        private lateinit var clearGroup: ImageView

        // Data
        private val groupIdByPosition: MutableMap<Int, Pair<GroupId?, Boolean>> = HashMap()
        private val groupPositionById: MutableMap<GroupId?, Int> = HashMap()

    // Show more fields section
    private lateinit var showMoreButton: TextView

    // Relatives section
        // UI
        private lateinit var relativesSection: View // Additional view
        private lateinit var addNewRelativeButton: MaterialButton
        private lateinit var relativesInfoContainer: LinearLayout

        // Data
        private val relativesInfoList: MutableList<RelativeInfoHolder> = ArrayList()
        private lateinit var deleteRelativeInfoHolder: OnDeleteRelativeInfoHolder


    // Data
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
        inflateShowMoreSection(layout)
        inflateRelativesSection(layout)

        contentView = layout.findViewById(R.id.personEditContent)
        loadingBar = layout.findViewById(R.id.personEditProgressBarLoading)

        navController = container!!.findNavController()
        presenter.applyInitialArgumentPersonAsync(PersonEditFragmentArgs.fromBundle(requireArguments()).personData)

        setSaveOrCreateButtonDisabled()
        hideAdditionalViews()

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

        val groupNames = getGroupNamesSpinnerData(groups)
        groupChooser.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, groupNames)
        groupChooser.onItemSelectedListener = object : MaterialSpinner.OnItemSelectedListener
        {
            override fun onItemSelected(parent: MaterialSpinner,
                                        view: View?,
                                        position: Int,
                                        id: Long)
            {
                currentPerson.groupId = groupIdByPosition[position]?.first
                clearGroup.visible = true
            }

            override fun onNothingSelected(parent: MaterialSpinner)
            {
                clearGroup.visible = false
            }
        }
        groupChooser.selection = groupPositionById[person.groupId] ?: MaterialSpinner.INVALID_POSITION
        if (groupNames.isEmpty())
        {
            groupChooser.isEnabled = false
            groupChooser.boxBackgroundColor = ContextCompat.getColor(App.getCtx(), R.color.colorDisabledText)
        }


        saveOrCreatePerson.setOnClickListener {
            currentPerson.surname = surname.text.toString()
            currentPerson.name = name.text.toString()

            if (patronymicContainer.isVisible)
            {
                currentPerson.patronymic = patronymic.text?.toString()
            }
            else
            {
                currentPerson.patronymic = null
            }


            if (selectedDay > 0 && selectedMonth > 0 && selectedYear > 0)
            {
                currentPerson.fullBirthday = LocalDate.of(selectedYear, selectedMonth, selectedDay)
            }
            else
            {
                currentPerson.fullBirthday = null
            }
            currentPerson.birthdayYear = selectedYear


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


            currentPerson.relativeInfos.clear()
            for (relativeHolder in relativesInfoList)
            {
                if (relativeHolder.isBlank())
                {
                    continue
                }

                currentPerson.relativeInfos.add(relativeHolder.extractData())
            }

            presenter.updateOrCreatePerson()
        }
    }

    override fun deletePersonFinished()
    {
        // Jump to people list, not to person info
        navController.popBackStack()
        navController.navigateUp()
    }

    override fun updateOrCreatePersonFinished()
    {
        navController.navigateUp()
    }

    override fun loadingState()
    {
        contentView.visible = false
        loadingBar.visible = true
        saveOrCreatePerson.visible = false
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
        patronymicContainer = layout.findViewById(R.id.editPersonTextContainerPatronymic)
        additionalViews.add(patronymicContainer)

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
        birthdayTitle = layout.findViewById(R.id.personEditBirthdayTitle)
        additionalViews.add(birthdayTitle)

        birthdayDay = layout.findViewById(R.id.personEditBirthdaySpinnerDay)
        additionalViews.add(birthdayDay)

        birthdayMonth = layout.findViewById(R.id.personEditBirthdaySpinnerMonth)
        additionalViews.add(birthdayMonth)

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
        clearGroup = layout.findViewById(R.id.personEditImageViewClearGroup)
        clearGroup.visible = false
        clearGroup.setOnClickListener {
            groupChooser.selection = MaterialSpinner.INVALID_POSITION
        }
    }

    private fun inflateShowMoreSection(layout: View)
    {
        showMoreButton = layout.findViewById(R.id.personEditTextViewShowMoreFields)
        showMoreButton.setOnClickListener {
            showAdditionalViews()
        }
    }



    private fun checkIfAllAdditionalViewsVisibleHideShowMore()
    {
        for (view in additionalViews)
        {
            if (!view.isVisible)
            {
                return
            }
        }

        showMoreButton.visible = false
    }

    private fun showAdditionalViews()
    {
        additionalViews.forEach { v -> v.visible = true }
        showMoreButton.visible = false

        birthdayYear.hint = getString(R.string.person_edit_birthday_year_full)
    }

    private fun hideAdditionalViews()
    {
        additionalViews.forEach { v -> v.visible = false }
        showMoreButton.visible = true

        birthdayYear.hint = getString(R.string.person_edit_birthday_title_short)
    }

    private fun inflateRelativesSection(layout: View)
    {
        relativesSection = layout.findViewById(R.id.editPersonRelativesSection)
        additionalViews.add(relativesSection)

        addNewRelativeButton = layout.findViewById(R.id.editPersonRelativeInfoAddRelative)
        relativesInfoContainer = layout.findViewById(R.id.editPersonRelativeInfoContainer)

        deleteRelativeInfoHolder = object : OnDeleteRelativeInfoHolder {
            override fun onDeleteInfoHolder(position: Int)
            {
                relativesInfoList.removeAt(position)
                relativesInfoContainer.removeViewAt(position)
                updateViewsIndices()
            }
        }

        addNewRelativeButton.setOnClickListener { addRelativeView() }

        addRelativeView(0)
    }

    private fun updateViewsIndices()
    {
        for ((i, relativeHolder) in relativesInfoList.withIndex())
        {
            relativeHolder.updateIndex(i)
        }
    }

    private fun addRelativeView(position: Int? = null)
    {
        val newRelativeView = View.inflate(requireContext(), R.layout.element_person_edit_relative_info, null)
        if (position != null)
        {
            relativesInfoContainer.addView(newRelativeView, position)
        }
        else
        {
            relativesInfoContainer.addView(newRelativeView)
        }
        relativesInfoList.add(RelativeInfoHolder(requireContext(), newRelativeView, relativesInfoList.size).apply {
            this.onDeleteRelativeInfoHolder = deleteRelativeInfoHolder
        })
    }

    private fun setExistPersonData()
    {
        (activity as AppCompatActivity).supportActionBar!!.setTitle(R.string.person_edit_screen_toolbar_title)
        saveOrCreatePerson.text = getString(R.string.save)

        surname.text = SpannableStringBuilder(currentPerson.surname)
        name.text = SpannableStringBuilder(currentPerson.name)

        currentPerson.patronymic?.let {
            patronymic.text = SpannableStringBuilder(it)
            patronymicContainer.visible = true
        }

        selectedYear = currentPerson.birthdayYear
        birthdayYear.selection = yearsList.indexOf(selectedYear.toString())

        currentPerson.fullBirthday?.let {
            selectedDay = it.dayOfMonth
            selectedMonth = it.month.value
            selectedYear = it.year

            birthdayDay.selection = daysOfMonthList.indexOf(selectedDay.toString())
            birthdayMonth.selection = selectedMonth - 1
            birthdayYear.selection = yearsList.indexOf(selectedYear.toString())

            birthdayTitle.visible = true
            birthdayDay.visible = true
            birthdayMonth.visible = true
        }

        deletePerson.setOnClickListener {
            val dialog = MaterialAlertDialogBuilder(requireContext())
                .setMessage("Удалить ученика?")
                .setPositiveButton("Удалить") { dialog, _ ->
                    dialog.cancel()
                    presenter.deletePerson(currentPerson)
                }
                .setNegativeButton("Отмена") { dialog, _ -> dialog.cancel() }
                .create()

            dialog.show()
        }

        for (i in 1 until currentPerson.relativeInfos.size)
        {
            addRelativeView() // 1st relative view already added
        }

        if (currentPerson.relativeInfos.isNotEmpty())
        {
            for ((i, relativeHolder) in relativesInfoList.withIndex())
            {
                relativeHolder.applyExistData(currentPerson.relativeInfos[i])
            }
            relativesSection.visible = true
        }

        checkIfAllAdditionalViewsVisibleHideShowMore()
        checkRequiredFields()
    }

    private fun getGroupNamesSpinnerData(groups: List<Group>): List<String>
    {
        val groupsListItems = LinkedList<String>()

        val groupListSorted = groups.sorted()
        for ((i, group) in groupListSorted.withIndex())
        {
            groupIdByPosition[i] = Pair(group.id, group.isPaid)
            groupPositionById[group.id] = i

            var age1 = "?"
            var age2 = "?"

            group.availableAbsoluteAgeLow?.let {
                age1 = it.toString()
            }

            group.availableAbsoluteAgeHigh?.let {
                age2 = it.toString()
            }

            val paidSuffix = if (group.isPaid)
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
        if (!surname.text.isNullOrBlank() && !name.text.isNullOrBlank() && birthdayYear.selectedItem != null)
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
}