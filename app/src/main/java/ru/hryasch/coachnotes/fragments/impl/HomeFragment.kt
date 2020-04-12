package ru.hryasch.coachnotes.fragments.impl

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.pawegio.kandroid.visible
import com.soywiz.klock.DateTime
import com.soywiz.klock.KlockLocale
import com.soywiz.klock.locale.russian
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import moxy.MvpAppCompatFragment
import moxy.presenter.InjectPresenter
import org.koin.core.KoinComponent
import org.koin.core.get
import org.koin.core.qualifier.named
import ru.hryasch.coachnotes.R
import ru.hryasch.coachnotes.domain.group.data.Group
import ru.hryasch.coachnotes.domain.person.data.Person
import ru.hryasch.coachnotes.fragments.HomeView
import ru.hryasch.coachnotes.home.impl.HomePresenterImpl
import java.util.*

class HomeFragment : MvpAppCompatFragment(), HomeView, KoinComponent
{
    @InjectPresenter
    lateinit var presenter: HomePresenterImpl

    private lateinit var personsButton:  CardView
    private lateinit var groupsButton:   CardView
    private lateinit var journalsButton: CardView

    private lateinit var personsCount:      TextView
    private lateinit var groupsCount:       TextView
    private lateinit var todayScheduleDate: TextView

    private lateinit var personsCountLoading: ProgressBar
    private lateinit var groupsCountLoading:  ProgressBar

    private lateinit var navController: NavController

    private var journalPickerDialog: AlertDialog? = null

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View?
    {
        val layout = inflater.inflate(R.layout.fragment_home, container, false)

        val dayOfWeekNames: Array<String> = get(named("daysOfWeekLong_RU"))

        personsButton  = layout.findViewById(R.id.homePersonsCard)
        groupsButton   = layout.findViewById(R.id.homeGroupsCard)
        journalsButton = layout.findViewById(R.id.homeJournalsCard)

        personsCount = layout.findViewById(R.id.homeTextViewPersonsCount)
        groupsCount  = layout.findViewById(R.id.homeTextViewGroupsCount)
        todayScheduleDate = layout.findViewById(R.id.homeTextViewTodayScheduleDate)

        personsCountLoading = layout.findViewById(R.id.homeProgressBarPersonsCount)
        groupsCountLoading = layout.findViewById(R.id.homeProgressBarGroupsCount)

        navController = container!!.findNavController()

        personsButton.setOnClickListener {
            navController.navigate(R.id.action_homeFragmentImpl_to_personListFragment)
        }

        groupsButton.setOnClickListener {
            navController.navigate(R.id.action_homeFragmentImpl_to_groupListFragment)
        }

        journalsButton.setOnClickListener {
            journalPickerDialog?.show()
        }

        val today = DateTime.nowLocal()
        val todayDayOfWeek = dayOfWeekNames[today.dayOfWeek.index0Monday].toLowerCase(Locale("ru"))

        todayScheduleDate.text = container.context.getString(R.string.home_screen_today_schedule_date, todayDayOfWeek, today.dayOfMonth, today.month.localName(KlockLocale.russian), today.local.date.year)

        return layout
    }

    override fun setPersonsCount(count: Int?)
    {
        if (count == null)
        {
            personsCount.visibility = View.INVISIBLE
            personsCountLoading.visible = true
        }
        else
        {
            personsCount.visibility = View.VISIBLE
            personsCountLoading.visible = false
            personsCount.text = count.toString()
        }
    }

    override fun setGroups(groups: List<Group>?)
    {
        if (groups == null)
        {
            groupsCount.visibility = View.INVISIBLE
            groupsCountLoading.visible = true
        }
        else
        {
            groupsCount.visibility = View.VISIBLE
            groupsCountLoading.visible = false
            groupsCount.text = groups.size.toString()

            updateJournalPickerDialog(groups)
        }
    }

    private fun updateJournalPickerDialog(groups: List<Group>?)
    {
        if (groups == null)
        {
            journalPickerDialog = null
        }
        else
        {
            val sortedGroups = groups.sorted()
            val dataArray: Array<String> = Array(groups.size) { "" }
            for ((i, group) in sortedGroups.withIndex())
            {
                dataArray[i] = "${group.name} (${group.availableAbsoluteAge?.first} - ${group.availableAbsoluteAge?.last} г.р)"
            }

            journalPickerDialog = MaterialAlertDialogBuilder(this@HomeFragment.context!!)
                .setTitle("Выберите группу")
                .setItems(dataArray) { dialog, position ->
                    val action = HomeFragmentDirections.actionHomeFragmentImplToJournalGroupFragment(sortedGroups[position])
                    navController.navigate(action)
                    dialog.cancel()
                }
                .create()
        }
    }
}