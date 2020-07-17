package ru.hryasch.coachnotes.fragments.impl

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.alamkanak.weekview.WeekView
import com.pawegio.kandroid.e
import com.pawegio.kandroid.visible
import kotlinx.coroutines.*
import moxy.MvpAppCompatFragment
import moxy.presenter.InjectPresenter
import org.koin.core.KoinComponent
import org.koin.core.get
import org.koin.core.qualifier.named
import ru.hryasch.coachnotes.R
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
class HomeFragment : MvpAppCompatFragment(), HomeView, KoinComponent
{
    @InjectPresenter
    lateinit var presenter: HomePresenterImpl

    private lateinit var navController: NavController

    // Schedule section
        // UI
            private lateinit var todayScheduleDate: TextView
            private lateinit var scheduleLoading: ProgressBar
            private lateinit var scheduleView: WeekView<HomeScheduleCell>
        // Classes
            private lateinit var scheduleGeneratingJob: Job


    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View?
    {
        navController = container!!.findNavController()

        val layout = inflater.inflate(R.layout.fragment_home, container, false)
        todayScheduleDate = layout.findViewById(R.id.homeScheduleTextViewTodayDate)
        scheduleLoading = layout.findViewById(R.id.homeScheduleLoading)
        scheduleView = layout.findViewById(R.id.homeScheduleWeekView)

        setTodayDate()
        tuneScheduleView()

        return layout
    }

    override fun setGroups(groups: List<Group>?)
    {
        if (groups == null)
        {
            loadingState()
        }
        else
        {
            showingState(groups)
        }
    }


    private fun loadingState()
    {
        scheduleLoading.visible = true
        scheduleView.visible = false
    }

    private fun showingState(groups: List<Group>)
    {
        loadingState()

        if (::scheduleGeneratingJob.isInitialized && scheduleGeneratingJob.isActive)
        {
            scheduleGeneratingJob.cancel()
        }

        scheduleGeneratingJob =
            GlobalScope.launch(Dispatchers.Unconfined)
            {
                val scheduleCells = prepareScheduleCells(groups)
                withContext(Dispatchers.Main)
                {
                    scheduleView.submit(scheduleCells)
                    scheduleLoading.visible = false
                    scheduleView.visible = true
                }
            }
    }

    private suspend fun prepareScheduleCells(groups: List<Group>): List<HomeScheduleCell>
    {
        if (groups.isEmpty())
        {
            return LinkedList()
        }

        val groupDataByDays: MutableMap<Int, MutableList<ScheduleDayInfo>> = HashMap() // <DayOfWeek0, List<scheduleInfos>>
        for (i in 0 until 7)
        {
            groupDataByDays[i] = LinkedList()
        }

        for (group in groups)
        {
            for (scheduleDay in group.scheduleDays)
            {
                val startTimeCal = Calendar.getInstance()
                with(startTimeCal)
                {
                    set(Calendar.HOUR_OF_DAY, scheduleDay.startTime!!.hour)
                    set(Calendar.MINUTE, scheduleDay.startTime!!.minute)
                }

                val endTimeCal = Calendar.getInstance()
                with(endTimeCal)
                {
                    set(Calendar.HOUR_OF_DAY, scheduleDay.endTime!!.hour)
                    set(Calendar.MINUTE, scheduleDay.endTime!!.minute)
                }

                groupDataByDays[scheduleDay.dayPosition0]!!.add(ScheduleDayInfo(group, startTimeCal, endTimeCal))
            }
        }

        groupDataByDays.forEach {
            e("day = ${it.key}, data = ${it.value}")
        }

        val result: MutableList<HomeScheduleCell> = LinkedList()
        var id: Long = 1
        val currentMonth = Calendar.getInstance()
        for (day in 1 .. currentMonth.getActualMaximum(Calendar.DAY_OF_MONTH))
        {
            currentMonth.set(Calendar.DAY_OF_MONTH, day)
            when(currentMonth.get(Calendar.DAY_OF_WEEK))
            {
                Calendar.MONDAY ->
                {
                    groupDataByDays[0]!!.forEach {
                        it.startTime.set(Calendar.DAY_OF_MONTH, day)
                        it.endTime.set(Calendar.DAY_OF_MONTH, day)
                        result.add(HomeScheduleCell(id, it.group.name, it.group.id, it.startTime.clone() as Calendar, it.endTime.clone() as Calendar, ContextCompat.getColor(App.getCtx(), R.color.colorScheduleCell)))
                        id++
                    }
                }

                Calendar.TUESDAY ->
                {
                    groupDataByDays[1]!!.forEach {
                        it.startTime.set(Calendar.DAY_OF_MONTH, day)
                        it.endTime.set(Calendar.DAY_OF_MONTH, day)
                        result.add(HomeScheduleCell(id, it.group.name, it.group.id, it.startTime.clone() as Calendar, it.endTime.clone() as Calendar, ContextCompat.getColor(App.getCtx(), R.color.colorScheduleCell)))
                        id++
                    }
                }

                Calendar.WEDNESDAY ->
                {
                    groupDataByDays[2]!!.forEach {
                        it.startTime.set(Calendar.DAY_OF_MONTH, day)
                        it.endTime.set(Calendar.DAY_OF_MONTH, day)
                        result.add(HomeScheduleCell(id, it.group.name, it.group.id, it.startTime.clone() as Calendar, it.endTime.clone() as Calendar, ContextCompat.getColor(App.getCtx(), R.color.colorScheduleCell)))
                        id++
                    }
                }

                Calendar.THURSDAY ->
                {
                    groupDataByDays[3]!!.forEach {
                        it.startTime.set(Calendar.DAY_OF_MONTH, day)
                        it.endTime.set(Calendar.DAY_OF_MONTH, day)
                        result.add(HomeScheduleCell(id, it.group.name, it.group.id, it.startTime.clone() as Calendar, it.endTime.clone() as Calendar, ContextCompat.getColor(App.getCtx(), R.color.colorScheduleCell)))
                        id++
                    }
                }

                Calendar.FRIDAY ->
                {
                    groupDataByDays[4]!!.forEach {
                        it.startTime.set(Calendar.DAY_OF_MONTH, day)
                        it.endTime.set(Calendar.DAY_OF_MONTH, day)
                        result.add(HomeScheduleCell(id, it.group.name, it.group.id, it.startTime.clone() as Calendar, it.endTime.clone() as Calendar, ContextCompat.getColor(App.getCtx(), R.color.colorScheduleCell)))
                        id++
                    }
                }

                Calendar.SATURDAY ->
                {
                    groupDataByDays[5]!!.forEach {
                        it.startTime.set(Calendar.DAY_OF_MONTH, day)
                        it.endTime.set(Calendar.DAY_OF_MONTH, day)
                        result.add(HomeScheduleCell(id, it.group.name, it.group.id, it.startTime.clone() as Calendar, it.endTime.clone() as Calendar, ContextCompat.getColor(App.getCtx(), R.color.colorScheduleCell)))
                        id++
                    }
                }

                Calendar.SUNDAY ->
                {
                    groupDataByDays[6]!!.forEach {
                        it.startTime.set(Calendar.DAY_OF_MONTH, day)
                        it.endTime.set(Calendar.DAY_OF_MONTH, day)
                        result.add(HomeScheduleCell(id, it.group.name, it.group.id, it.startTime.clone() as Calendar, it.endTime.clone() as Calendar, ContextCompat.getColor(App.getCtx(), R.color.colorScheduleCell)))
                        id++
                    }
                }
            }
        }

        return result
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
        scheduleView.setOnEventClickListener { data, rect ->
            // go to schedule
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

    private fun updateJournalPickerDialog(groups: List<Group>?)
    {
        /*journalPickerDialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle("Выберите группу")
            .setItems(dataArray) { dialog, position ->
                val action = HomeFragmentDirections.actionHomeFragmentImplToJournalGroupFragment(sortedGroups[position])
                navController.navigate(action)
                dialog.cancel()
            }
            .create()

         */
    }

    private data class ScheduleDayInfo(
        val group: Group,
        val startTime: Calendar,
        val endTime: Calendar
    )
    {
        override fun toString(): String
        {
            return "Group ${group.name} (${group.id}), start = ${startTime.get(Calendar.HOUR_OF_DAY)}:${startTime.get(Calendar.MINUTE)}, end = ${endTime.get(Calendar.HOUR_OF_DAY)}:${endTime.get(Calendar.MINUTE)}"
        }
    }
}