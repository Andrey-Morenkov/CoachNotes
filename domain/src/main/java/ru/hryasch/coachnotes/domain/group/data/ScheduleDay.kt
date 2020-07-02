package ru.hryasch.coachnotes.domain.group.data

import java.io.Serializable

class ScheduleDay(val dayName: String, val dayPosition0: Int) : Serializable, Comparable<ScheduleDay>
{
    var startTime: String = ""
    var endTime: String = ""

    override fun toString(): String
    {
        return "$dayName $startTime-$endTime"
    }

    override fun compareTo(other: ScheduleDay): Int
    {
        return dayPosition0 - other.dayPosition0
    }

    fun isNotBlank(): Boolean
    {
        return !startTime.isBlank()
    }
}