package ru.hryasch.coachnotes.home.data

import com.alamkanak.weekview.WeekViewDisplayable
import com.alamkanak.weekview.WeekViewEvent
import ru.hryasch.coachnotes.domain.common.GroupId
import ru.hryasch.coachnotes.domain.group.data.Group
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class HomeScheduleCell(
    val id: Long,
    val group: Group,
    val startTime: Calendar,
    val endTime: Calendar,
    val color: Int
): WeekViewDisplayable<HomeScheduleCell>
{
    override fun toWeekViewEvent(): WeekViewEvent<HomeScheduleCell>
    {
        val style = WeekViewEvent.Style.Builder()
            .setBackgroundColor(color)
            .setTextStrikeThrough(false)
            .build()

        val startTimeSubtitle = SimpleDateFormat("HH:mm", Locale.getDefault()).format(startTime.time)
        val endTimeSubtitle   = SimpleDateFormat("HH:mm", Locale.getDefault()).format(endTime.time)
        val subtitle = "$startTimeSubtitle - $endTimeSubtitle"

        return WeekViewEvent.Builder(this)
            .setId(id)
            .setTitle(group.name)
            .setStartTime(startTime)
            .setEndTime(endTime)
            .setLocation(subtitle)
            .setAllDay(false)
            .setStyle(style)
            .build()
    }

    override fun toString(): String
    {
        return "ScheduleCell[id = $id, groupName = ${group.name}(${group.id}), startTime = ${startTime.get(Calendar.DAY_OF_MONTH)}.${startTime.get(Calendar.MONTH)}.${startTime.get(Calendar.YEAR)} ${startTime.get(Calendar.HOUR_OF_DAY)}:${startTime.get(Calendar.MINUTE)}, endTime = ${endTime.get(Calendar.DAY_OF_MONTH)}.${endTime.get(Calendar.MONTH)}.${endTime.get(Calendar.YEAR)} ${endTime.get(Calendar.HOUR_OF_DAY)}:${endTime.get(Calendar.MINUTE)}]"
    }
}