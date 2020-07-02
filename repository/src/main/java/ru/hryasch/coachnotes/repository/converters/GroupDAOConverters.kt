package ru.hryasch.coachnotes.repository.converters

import ru.hryasch.coachnotes.domain.group.data.Group
import ru.hryasch.coachnotes.domain.group.data.GroupImpl
import ru.hryasch.coachnotes.domain.group.data.ScheduleDay
import ru.hryasch.coachnotes.repository.dao.GroupDAO
import ru.hryasch.coachnotes.repository.dao.ScheduleDayDAO
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
    val scheduleDay = ScheduleDay(this.name!!, this.position0!!)
    scheduleDay.startTime = this.startTime!!
    scheduleDay.endTime   = this.finishTime!!

    return scheduleDay
}

fun ScheduleDay.toDAO(): ScheduleDayDAO
{
    val dao = ScheduleDayDAO(this.dayName, this.dayPosition0)
    dao.startTime  = this.startTime
    dao.finishTime = this.endTime

    return dao
}