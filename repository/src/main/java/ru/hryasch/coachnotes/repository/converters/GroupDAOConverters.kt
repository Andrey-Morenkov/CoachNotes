package ru.hryasch.coachnotes.repository.converters

import ru.hryasch.coachnotes.domain.group.data.Group
import ru.hryasch.coachnotes.domain.group.data.GroupImpl
import ru.hryasch.coachnotes.domain.group.data.ScheduleDay
import ru.hryasch.coachnotes.repository.dao.GroupDAO
import ru.hryasch.coachnotes.repository.dao.ScheduleDayDAO
import java.text.SimpleDateFormat
import java.time.LocalTime
import java.util.*

@JvmName("DAOGroupListConverter")
fun List<GroupDAO>.fromDAO(): List<Group>
{
    val groupList: MutableList<Group> = LinkedList()

    this.forEach { groupList.add(it.fromDAO()) }

    return groupList
}

fun GroupDAO.fromDAO(): Group
{
    val lowAge = this.availableAgeLow
    val highAge = this.availableAgeHigh

    val group = GroupImpl(this.id!!, this.name!!, isPaid = this.isPaid)
    if (lowAge != null)
    {
        val ageRange = IntRange(lowAge, highAge ?: lowAge)
        group.availableAbsoluteAge = ageRange
    }


    this.members.forEach {
        group.membersList.add(it)
    }

    this.scheduleDays.forEach {
        group.scheduleDays.add(it.fromDAO())
    }

    return group
}

fun Group.toDao(): GroupDAO
{
    val dao = GroupDAO(this.id, this.name, availableAgeLow = this.availableAbsoluteAge!!.first)

    dao.isPaid = isPaid

    if (this.availableAbsoluteAge!!.first != this.availableAbsoluteAge!!.last)
    {
        dao.availableAgeHigh = this.availableAbsoluteAge!!.last
    }

    this.membersList.forEach {
        dao.members.add(it)
    }

    this.scheduleDays.forEach {
        dao.scheduleDays.add(it.toDAO())
        dao.scheduleDaysCode0 += it.dayPosition0
    }

    return dao
}

fun ScheduleDayDAO.fromDAO(): ScheduleDay
{
    val startTimeCal = Calendar.getInstance().apply {
        timeInMillis = ScheduleDay.format.parse(startTime!!)!!.time
    }

    val endTimeCal = Calendar.getInstance().apply {
        timeInMillis = ScheduleDay.format.parse(finishTime!!)!!.time
    }

    return ScheduleDay(this.name!!, this.position0!!).apply {
        startTime = LocalTime.of(startTimeCal.get(Calendar.HOUR_OF_DAY), startTimeCal.get(Calendar.MINUTE))
        endTime = LocalTime.of(endTimeCal.get(Calendar.HOUR_OF_DAY), endTimeCal.get(Calendar.MINUTE))
    }
}

fun ScheduleDay.toDAO(): ScheduleDayDAO
{
    val startTimeCal = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, startTime!!.hour)
        set(Calendar.MINUTE, startTime!!.minute)
    }

    val endTimeCal = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, endTime!!.hour)
        set(Calendar.MINUTE, endTime!!.minute)
    }

    return ScheduleDayDAO(this.dayName, this.dayPosition0).apply {
        startTime  = ScheduleDay.format.format(startTimeCal.time)
        finishTime = ScheduleDay.format.format(endTimeCal.time)
    }
}