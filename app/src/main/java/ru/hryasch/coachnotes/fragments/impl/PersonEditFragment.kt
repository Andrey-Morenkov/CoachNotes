package ru.hryasch.coachnotes.fragments.impl

import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.ProgressBar
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
import com.pawegio.kandroid.i
import com.pawegio.kandroid.visible
import com.skydoves.powerspinner.PowerSpinnerView
import com.soywiz.klock.DateFormat
import com.soywiz.klock.parse
import com.tiper.MaterialSpinner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import moxy.MvpAppCompatFragment
import moxy.presenter.InjectPresenter
import org.koin.core.KoinComponent
import ru.hryasch.coachnotes.R
import ru.hryasch.coachnotes.application.App
import ru.hryasch.coachnotes.domain.common.GroupId
import ru.hryasch.coachnotes.domain.group.data.Group
import ru.hryasch.coachnotes.domain.person.data.Person
import ru.hryasch.coachnotes.fragments.PersonEditView
import ru.hryasch.coachnotes.people.presenters.impl.PersonEditPresenterImpl
import java.util.*
import kotlin.collections.HashMap

class PersonEditFragment : MvpAppCompatFragment(), PersonEditView, KoinComponent
{
    @InjectPresenter
    lateinit var presenter: PersonEditPresenterImpl

    private lateinit var navController: NavController

    private lateinit var saveOrCreatePerson: MaterialButton
    private lateinit var deletePerson: MaterialButton

    private lateinit var surname: TextInputEditText
    private lateinit var name: TextInputEditText
    private lateinit var patronymic: TextInputEditText
    private lateinit var birthday: TextInputEditText
    private lateinit var groupChooser: MaterialSpinner

    private val groupIdByPosition: MutableMap<Int, Pair<GroupId?, Boolean>> = HashMap()
    private val groupPositionById: MutableMap<GroupId?, Int> = HashMap()

    private lateinit var contentView: NestedScrollView
    private lateinit var loadingBar: ProgressBar

    private lateinit var currentPerson: Person

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View?
    {
        val layout = inflater.inflate(R.layout.fragment_edit_person, container, false)

        saveOrCreatePerson = layout.findViewById(R.id.personEditButtonCreateOrSave)
        deletePerson = layout.findViewById(R.id.personEditButtonRemovePerson)

        surname = layout.findViewById(R.id.editPersonEditTextSurname)
        name = layout.findViewById(R.id.editPersonEditTextName)
        patronymic = layout.findViewById(R.id.editPersonEditTextPatronymic)
        birthday = layout.findViewById(R.id.editPersonEditTextBirthday)
        groupChooser = layout.findViewById(R.id.personEditSpinnerGroup)

        contentView = layout.findViewById(R.id.personEditContent)
        loadingBar = layout.findViewById(R.id.personEditProgressBarLoading)

        loadingState()

        navController = container!!.findNavController()

        val toolbar: Toolbar = layout.findViewById(R.id.personEditToolbar)
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        (activity as AppCompatActivity).supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        (activity as AppCompatActivity).supportActionBar!!.setDisplayShowHomeEnabled(true)

        toolbar.setNavigationOnClickListener {
            navController.navigateUp()
        }

        deletePerson.visible = false

        GlobalScope.launch(Dispatchers.Default)
        {
            presenter.applyPersonData(PersonEditFragmentArgs.fromBundle(arguments!!).personData)
        }

        return layout
    }

    override fun setPersonData(person: Person, groups: List<Group>)
    {
        contentView.visible = true
        loadingBar.visible = false

        currentPerson = person

        if (currentPerson.surname.isNotBlank())
        {
            setExistPersonData()
        }

        groupChooser.adapter = ArrayAdapter(context!!, android.R.layout.simple_list_item_1, getGroupNamesSpinnerData(groups))
        groupChooser.onItemSelectedListener = object: MaterialSpinner.OnItemSelectedListener
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

        i("person.groupId = ${person.groupId}")

        groupChooser.selection = groupPositionById[person.groupId]!!

        saveOrCreatePerson.setOnClickListener {
            currentPerson.surname = surname.text.toString()
            currentPerson.name = name.text.toString()
            currentPerson.patronymic = patronymic.text?.toString()

            if (birthday.text != null)
            {
                currentPerson.birthday = DateFormat("dd.MM.yyyy").parse(birthday.text!!.toString()).local.date
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

        val dialog = MaterialAlertDialogBuilder(this@PersonEditFragment.context!!)
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
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(App.getCtx(), R.color.colorAccent))
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(App.getCtx(), R.color.colorPrimaryLight))
        }

        dialog.show()
    }

    private fun setExistPersonData()
    {
        deletePerson.visible = true
        (activity as AppCompatActivity).supportActionBar!!.setTitle(R.string.person_edit_screen_toolbar_title)
        saveOrCreatePerson.text = context!!.getString(R.string.save)

        surname.text = SpannableStringBuilder(currentPerson.surname)
        name.text = SpannableStringBuilder(currentPerson.name)

        currentPerson.patronymic?.let {
            patronymic.text = SpannableStringBuilder(it)
        }

        currentPerson.birthday?.let {
            birthday.text = SpannableStringBuilder(it.format("dd.MM.yyyy"))
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
            groupIdByPosition[i+1] = Pair(group.id, group.isPaid)
            groupPositionById[group.id] = i+1

            var age1 = "?"
            var age2 = "?"

            if (group.availableAbsoluteAge != null)
            {
                age1 = group.availableAbsoluteAge!!.first.toString()
                age2 = group.availableAbsoluteAge!!.last.toString()
            }

            if (age1 == age2)
            {
                groupsListItems.add(context!!.getString(R.string.person_edit_screen_group_pattern_single, group.name, age1))
            }
            else
            {
                groupsListItems.add(context!!.getString(R.string.person_edit_screen_group_pattern_range, group.name, age1, age2))
            }
        }

        return groupsListItems
    }
}