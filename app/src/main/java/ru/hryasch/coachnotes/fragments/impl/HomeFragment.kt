package ru.hryasch.coachnotes.fragments.impl

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.alamkanak.weekview.WeekView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.pawegio.kandroid.e
import com.pawegio.kandroid.runOnUiThread
import com.pawegio.kandroid.visible
import kotlinx.coroutines.*
import moxy.MvpAppCompatFragment
import moxy.presenter.InjectPresenter
import org.koin.core.KoinComponent
import org.koin.core.get
import org.koin.core.qualifier.named
import ru.hryasch.coachnotes.R
import ru.hryasch.coachnotes.activity.MainActivity
import ru.hryasch.coachnotes.application.App
import ru.hryasch.coachnotes.domain.group.data.Group
import ru.hryasch.coachnotes.fragments.HomeView
import ru.hryasch.coachnotes.home.data.HomeScheduleCell
import ru.hryasch.coachnotes.home.impl.HomePresenterImpl
import java.time.ZonedDateTime
import java.time.format.TextStyle
import java.util.Calendar
import java.util.LinkedList
import java.util.Locale


@ExperimentalCoroutinesApi
class HomeFragment: MvpAppCompatFragment(), HomeView, KoinComponent
{
    @InjectPresenter
    lateinit var presenter: HomePresenterImpl

    // UI
    private lateinit var todayScheduleDate: TextView
    private lateinit var scheduleLoading: ProgressBar
    private lateinit var scheduleView: WeekView<HomeScheduleCell>

    // Dialogs
    private lateinit var groupHasNoMembersDialog: AlertDialog



    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View?
    {
        val layout = inflater.inflate(R.layout.fragment_home, container, false)
        todayScheduleDate = layout.findViewById(R.id.homeScheduleTextViewTodayDate)
        scheduleLoading = layout.findViewById(R.id.homeScheduleLoading)
        scheduleView = layout.findViewById(R.id.homeScheduleWeekView)

        setTodayDate()
        tuneScheduleView()
        createGroupHasNoMembersWarningDialog()

        return layout
    }

    override fun setScheduleCells(scheduleCells: List<HomeScheduleCell>?)
    {
        if (scheduleCells == null)
        {
            loadingState()
        }
        else
        {
            showingState(scheduleCells)
        }
    }


    private fun loadingState()
    {
        scheduleLoading.visible = true
        scheduleView.visible = false
    }

    private fun createGroupHasNoMembersWarningDialog()
    {
        groupHasNoMembersDialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle("Невозможно отобразить журнал")
            .setMessage("В группе нет учеников")
            .setPositiveButton(android.R.string.ok) { dialog, _ ->
                dialog.dismiss()
            }
            .create()
    }

    private fun showingState(scheduleCells: List<HomeScheduleCell>)
    {
        scheduleView.submit(scheduleCells)
        scheduleLoading.visible = false
        scheduleView.visible = true
    }

    private fun tuneScheduleView()
    {
        val now = Calendar.getInstance()
        val min = now.clone() as Calendar
        min.set(Calendar.DAY_OF_MONTH, 1)
        val max = now.clone() as Calendar
        max.set(Calendar.DAY_OF_MONTH, max.getActualMaximum(Calendar.DAY_OF_MONTH))

        scheduleView.minDate = min
        scheduleView.maxDate = max
        scheduleView.setOnEventClickListener { data, _ ->
            if (data.group.membersList.isEmpty())
            {
                groupHasNoMembersDialog.show()
            }
            else
            {
                (requireActivity() as MainActivity).navigateToJournalFragment(data.group)
            }
        }
    }

    private fun setTodayDate()
    {
        val dayOfWeekNames: Array<String> = get(named("daysOfWeekLong_RU"))
        val today = ZonedDateTime.now()
        val todayDayOfWeek = dayOfWeekNames[today.dayOfWeek.value - 1].toLowerCase(Locale.getDefault())
        todayScheduleDate.text = getString(R.string.home_screen_today_schedule_date, todayDayOfWeek, today.dayOfMonth, today.month.getDisplayName(TextStyle.FULL_STANDALONE, Locale.getDefault()), today.year)

        todayScheduleDate.setOnClickListener {
            scheduleView.goToToday()
            scheduleView.goToCurrentTime()
        }
    }
}

data class ScheduleDayInfo(val group: Group,
                           val startTime: Calendar,
                           val endTime: Calendar)
{
    override fun toString(): String
    {
        return "Group ${group.name} (${group.id}), start = ${startTime.get(Calendar.HOUR_OF_DAY)}:${startTime.get(Calendar.MINUTE)}, end = ${endTime.get(Calendar.HOUR_OF_DAY)}:${endTime.get(Calendar.MINUTE)}"
    }
}