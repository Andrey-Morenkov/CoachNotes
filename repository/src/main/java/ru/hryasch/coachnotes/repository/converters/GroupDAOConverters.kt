package ru.hryasch.coachnotes.repository.converters

import ru.hryasch.coachnotes.domain.group.data.Group
import ru.hryasch.coachnotes.domain.group.data.GroupImpl
import ru.hryasch.coachnotes.domain.group.data.ScheduleDay
import ru.hryasch.coachnotes.repository.dao.DeletedGroupDAO
import ru.hryasch.coachnotes.repository.dao.GroupDAO
import ru.hryasch.coachnotes.repository.dao.ScheduleDayDAO
import java.time.LocalTime
import java.util.Calendar
import java.util.Date
import java.util.LinkedList

@JvmName("DAOGroupListConverter")
fun List<GroupDAO>.fromDAO(): List<Group>
{
    val groupList: MutableList<Group> = LinkedList()
    this.forEach { groupList.add(it.fromDAO()) }

    return groupList
}

@JvmName("DeletedDAOGroupListConverter")
fun List<DeletedGroupDAO>.fromDAO(): List<Group>
{
    val groupList: MutableList<Group> = LinkedList()
    this.forEach { groupList.add(it.fromDAO()) }

    return groupList
}



fun GroupDAO.fromDAO(): Group
{
    val group = GroupImpl(this.id!!, this.name!!).apply {
        isPaid = this@fromDAO.isPaid
        deletedTimestamp = null
    }

    group.availableAbsoluteAgeLow  = this.availableAgeLow
    group.availableAbsoluteAgeHigh = this.availableAgeHigh

    this.members.forEach {
        group.membersList.add(it)
    }

    this.scheduleDays.forEach {
        group.scheduleDays.add(it.fromDAO())
    }

    return group
}

fun DeletedGroupDAO.fromDAO(): Group
{
    val group = GroupImpl(this.id!!, this.name!!).apply {
        isPaid = this@fromDAO.isPaid
        deletedTimestamp = this@fromDAO.deleteTimestamp
    }

    group.availableAbsoluteAgeLow = this.availableAgeLow
    group.availableAbsoluteAgeHigh = this.availableAgeHigh

    this.members.forEach {
        group.membersList.add(it)
    }

    this.scheduleDays.forEach {
        group.scheduleDays.add(it.fromDAO())
    }

    return group
}



fun Group.toDao(): GroupDAO?
{
    if (this.deletedTimestamp != null)
    {
        return null
    }

    val dao = GroupDAO(this.id, this.name, this.isPaid, this.availableAbsoluteAgeLow, this.availableAbsoluteAgeHigh)

    this.membersList.forEach {
        dao.members.add(it)
    }

    this.scheduleDays.forEach {
        dao.scheduleDays.add(it.toDAO())
        dao.scheduleDaysCode0 += it.dayPosition0
    }

    return dao
}

fun Group.toDeletedDao(): DeletedGroupDAO?
{
    if (this.deletedTimestamp == null)
    {
        return null
    }

    return DeletedGroupDAO(this.toDao()!!, this.deletedTimestamp!!)
}



fun ScheduleDayDAO.fromDAO(): ScheduleDay
{
    val startHHmmTime = startTime!!.split(":")
    val endHHmmTime = finishTime!!.split(":")

    return ScheduleDay(this.name!!, this.position0!!).apply {
        startTime = LocalTime.of(startHHmmTime[0].toInt(), startHHmmTime[1].toInt())
        endTime = LocalTime.of(endHHmmTime[0].toInt(), endHHmmTime[1].toInt())
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