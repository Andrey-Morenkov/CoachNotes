package ru.hryasch.coachnotes.fragments.impl

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.Toolbar
import androidx.core.widget.NestedScrollView
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.pawegio.kandroid.visible
import moxy.MvpAppCompatFragment
import moxy.presenter.InjectPresenter
import org.koin.core.KoinComponent
import ru.hryasch.coachnotes.R
import ru.hryasch.coachnotes.activity.MainActivity
import ru.hryasch.coachnotes.domain.common.GroupId
import ru.hryasch.coachnotes.domain.person.data.Person
import ru.hryasch.coachnotes.fragments.PersonView
import ru.hryasch.coachnotes.people.PersonParamsAdapter
import ru.hryasch.coachnotes.people.presenters.impl.PersonPresenterImpl
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class PersonInfoFragment : MvpAppCompatFragment(), PersonView, KoinComponent
{
    @InjectPresenter
    lateinit var presenter: PersonPresenterImpl

    private lateinit var navController: NavController

    private lateinit var editPerson: ImageButton

    private lateinit var surnameName: TextView
    private lateinit var patronymic: TextView
    private lateinit var isPaid: AppCompatImageView
    private lateinit var age: TextView
    private lateinit var groupName: TextView
    private lateinit var tagsLayout: LinearLayout
    private lateinit var viewPager: ViewPager2

    private lateinit var currentPerson: Person

    private lateinit var contentView: NestedScrollView
    private lateinit var loadingBar: ProgressBar

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View?
    {
        val layout = inflater.inflate(R.layout.fragment_person_info, container, false)

        (activity as MainActivity).hideBottomNavigation()

        editPerson = layout.findViewById(R.id.personInfoImageButtonEditPerson)

        surnameName = layout.findViewById(R.id.personInfoTextViewNameSurname)
        patronymic = layout.findViewById(R.id.personInfoTextViewPatronymic)
        isPaid = layout.findViewById(R.id.personInfoImageViewIsPaid)
        age = layout.findViewById(R.id.personInfoTextViewAge)
        groupName = layout.findViewById(R.id.personInfoTextViewGroupName)
        tagsLayout = layout.findViewById(R.id.personInfoTagsLayout)
        viewPager = layout.findViewById(R.id.personInfoViewPager)

        contentView = layout.findViewById(R.id.personInfoContentView)
        loadingBar = layout.findViewById(R.id.personInfoProgressBarLoading)

        loadingState()

        tagsLayout.visible = false

        navController = container!!.findNavController()

        val toolbar: Toolbar = layout.findViewById(R.id.personInfoToolbar)
        (activity as AppCompatActivity).supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        (activity as AppCompatActivity).supportActionBar!!.setDisplayShowHomeEnabled(true)

        toolbar.setNavigationOnClickListener {
            navController.navigateUp()
        }

        presenter.applyInitialArgumentPersonAsync(PersonInfoFragmentArgs.fromBundle(requireArguments()).personData)

        return layout
    }

    override fun setPersonData(person: Person, groupNames: Map<GroupId, String>)
    {
        contentView.visible = true
        loadingBar.visible = false

        currentPerson = person

        viewPager.adapter = PersonParamsAdapter(this, person)

        surnameName.text = getString(R.string.person_info_header_name_surname_pattern, person.surname, person.name)

        if (person.patronymic == null)
        {
            patronymic.visibility = View.INVISIBLE
        }
        else
        {
            patronymic.visibility = View.VISIBLE
            patronymic.text = person.patronymic
        }

        if (!person.isPaid)
        {
            isPaid.visibility = View.INVISIBLE
        }

        if (person.fullBirthday == null)
        {
            age.text = getString(R.string.person_info_header_age_only_year_pattern, person.birthdayYear)
        }
        else
        {
            val birthdayDate = LocalDate.of(person.fullBirthday!!.year, person.fullBirthday!!.month.value, person.fullBirthday!!.dayOfMonth)
            val nowDate = LocalDate.now()
            val diffYears = ChronoUnit.YEARS.between(birthdayDate, nowDate)

            age.text = getString(R.string.person_info_header_age_pattern, person.fullBirthday!!.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")), diffYears)
        }

        groupName.text = groupNames[person.groupId] ?: "Нет группы"

        editPerson.setOnClickListener {
            val action = PersonInfoFragmentDirections.actionPersonInfoFragmentToPersonEditFragment(currentPerson)
            navController.navigate(action)
        }
    }

    override fun loadingState()
    {
        contentView.visible = false
        loadingBar.visible = true
    }
}