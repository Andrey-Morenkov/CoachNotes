package ru.hryasch.coachnotes.domain.group.data

import android.annotation.SuppressLint
import java.io.Serializable
import java.text.SimpleDateFormat
import java.time.LocalTime
import java.util.Locale

class ScheduleDay(val dayName: String, val dayPosition0: Int) : Serializable, Comparable<ScheduleDay>
{
    var startTime: LocalTime? = null
    var endTime:   LocalTime? = null

    companion object
    {
        @SuppressLint("ConstantLocale")
        val format = SimpleDateFormat("HH:mm", Locale.getDefault())
    }

    override fun toString(): String
    {
        return "$dayName $startTime-$endTime"
    }

    override fun compareTo(other: ScheduleDay): Int
    {
        if (dayPosition0 != other.dayPosition0)
        {
            return dayPosition0 - other.dayPosition0
        }

        if (startTime == null)
        {
            if (other.startTime == null)
            {
                return 0
            }
            return -1
        }

        if (startTime != other.startTime)
        {
            return startTime!!.compareTo(other.startTime)
        }
        return endTime!!.compareTo(other.endTime)
    }

    fun isNotBlank(): Boolean
    {
        return startTime != null
    }
}