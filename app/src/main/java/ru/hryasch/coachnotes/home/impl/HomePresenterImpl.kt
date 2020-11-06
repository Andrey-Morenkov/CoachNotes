package ru.hryasch.coachnotes.home.impl

import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import com.pawegio.kandroid.d
import com.pawegio.kandroid.e
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ReceiveChannel
import moxy.InjectViewState
import moxy.MvpPresenter
import org.koin.core.KoinComponent
import org.koin.core.get
import org.koin.core.inject
import org.koin.core.qualifier.named
import ru.hryasch.coachnotes.R
import ru.hryasch.coachnotes.application.App
import ru.hryasch.coachnotes.repository.global.GlobalSettings
import ru.hryasch.coachnotes.domain.group.data.Group
import ru.hryasch.coachnotes.domain.home.HomeInteractor
import ru.hryasch.coachnotes.fragments.CoachData
import ru.hryasch.coachnotes.fragments.HomeView
import ru.hryasch.coachnotes.fragments.impl.ScheduleDayInfo
import ru.hryasch.coachnotes.home.HomePresenter
import ru.hryasch.coachnotes.home.data.HomeScheduleCell
import java.util.Calendar
import java.util.LinkedList

@ExperimentalCoroutinesApi
@InjectViewState
class HomePresenterImpl: MvpPresenter<HomeView>(), HomePresenter, KoinComponent
{
    private val coachData: CoachData = readCoachData()

    private val homeInteractor: HomeInteractor by inject()
    private val groupsRecvChannel: ReceiveChannel<List<Group>>  = get(named("recvGroupsList"))
    private val subscriptions: Job = Job()

    init
    {
        viewState.setCoachData(coachData)

        loadingState()

        GlobalScope.launch(Dispatchers.Default)
        {
            val groups = homeInteractor.getAllGroups()

            val cells = prepareScheduleCells(groups)
            withContext(Dispatchers.Main)
            {
                viewState.setScheduleCells(cells)
                subscribeOnGroupsChanges()
            }
        }
    }

    override fun onDestroy()
    {
        groupsRecvChannel.cancel()
        subscriptions.cancel()

        super.onDestroy()
    }



    private fun loadingState()
    {
        viewState.setScheduleCells(null)
    }

    private fun readCoachData() = CoachData(GlobalSettings.Coach.getFullNameString(),
                                            GlobalSettings.Coach.getRole()!!)


    @ExperimentalCoroutinesApi
    private fun subscribeOnGroupsChanges()
    {
        GlobalScope.launch(Dispatchers.IO + subscriptions)
        {
            while (true)
            {
                val newData = groupsRecvChannel.receive()
                d("HomePresenterImpl <AllGroups>: RECEIVED (count = ${newData.size})")


                val cells = prepareScheduleCells(newData)
                withContext(Dispatchers.Main)
                {
                    viewState.setScheduleCells(cells)
                }
            }
        }
    }

    private suspend fun prepareScheduleCells(groups: List<Group>?): List<HomeScheduleCell>
    {
        if (groups == null || groups.isEmpty())
        {
            return ArrayList()
        }

        e("prepareScheduleCells of groups: $groups")

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
                Calendar.MONDAY    ->
                {
                    groupDataByDays[0]!!.forEach {
                        it.startTime.set(Calendar.DAY_OF_MONTH, day)
                        it.endTime.set(Calendar.DAY_OF_MONTH, day)
                        result.add(HomeScheduleCell(id, it.group, it.startTime.clone() as Calendar, it.endTime.clone() as Calendar, getScheduleDayColor(it.group.isPaid)))
                        id++
                    }
                }

                Calendar.TUESDAY   ->
                {
                    groupDataByDays[1]!!.forEach {
                        it.startTime.set(Calendar.DAY_OF_MONTH, day)
                        it.endTime.set(Calendar.DAY_OF_MONTH, day)
                        result.add(HomeScheduleCell(id, it.group, it.startTime.clone() as Calendar, it.endTime.clone() as Calendar, getScheduleDayColor(it.group.isPaid)))
                        id++
                    }
                }

                Calendar.WEDNESDAY ->
                {
                    groupDataByDays[2]!!.forEach {
                        it.startTime.set(Calendar.DAY_OF_MONTH, day)
                        it.endTime.set(Calendar.DAY_OF_MONTH, day)
                        result.add(HomeScheduleCell(id, it.group, it.startTime.clone() as Calendar, it.endTime.clone() as Calendar, getScheduleDayColor(it.group.isPaid)))
                        id++
                    }
                }

                Calendar.THURSDAY  ->
                {
                    groupDataByDays[3]!!.forEach {
                        it.startTime.set(Calendar.DAY_OF_MONTH, day)
                        it.endTime.set(Calendar.DAY_OF_MONTH, day)
                        result.add(HomeScheduleCell(id, it.group, it.startTime.clone() as Calendar, it.endTime.clone() as Calendar, getScheduleDayColor(it.group.isPaid)))
                        id++
                    }
                }

                Calendar.FRIDAY    ->
                {
                    groupDataByDays[4]!!.forEach {
                        it.startTime.set(Calendar.DAY_OF_MONTH, day)
                        it.endTime.set(Calendar.DAY_OF_MONTH, day)
                        result.add(HomeScheduleCell(id, it.group, it.startTime.clone() as Calendar, it.endTime.clone() as Calendar, getScheduleDayColor(it.group.isPaid)))
                        id++
                    }
                }

                Calendar.SATURDAY  ->
                {
                    groupDataByDays[5]!!.forEach {
                        it.startTime.set(Calendar.DAY_OF_MONTH, day)
                        it.endTime.set(Calendar.DAY_OF_MONTH, day)
                        result.add(HomeScheduleCell(id, it.group, it.startTime.clone() as Calendar, it.endTime.clone() as Calendar, getScheduleDayColor(it.group.isPaid)))
                        id++
                    }
                }

                Calendar.SUNDAY    ->
                {
                    groupDataByDays[6]!!.forEach {
                        it.startTime.set(Calendar.DAY_OF_MONTH, day)
                        it.endTime.set(Calendar.DAY_OF_MONTH, day)
                        result.add(HomeScheduleCell(id, it.group, it.startTime.clone() as Calendar, it.endTime.clone() as Calendar, getScheduleDayColor(it.group.isPaid)))
                        id++
                    }
                }
            }
        }

        return result
    }

    @ColorInt
    private fun getScheduleDayColor(isPaidGroup: Boolean): Int
    {
        return if (isPaidGroup)
        {
            ContextCompat.getColor(App.getCtx(), R.color.colorScheduleCellPaidGroup)
        }
        else
        {
            ContextCompat.getColor(App.getCtx(), R.color.colorScheduleCellFreeGroup)
        }
    }
}