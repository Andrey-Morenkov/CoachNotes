package ru.hryasch.coachnotes.journal.table.viewholders

import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.evrencoskun.tableview.adapter.recyclerview.holder.AbstractViewHolder
import com.soywiz.klock.Date
import com.soywiz.klock.DateTime
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.koin.core.qualifier.named
import ru.hryasch.coachnotes.R
import ru.hryasch.coachnotes.application.App

class ColumnHeaderViewHolder(columnHeaderItem: View) : AbstractViewHolder(columnHeaderItem), KoinComponent
{
    private val daysOfWeek: Array<String> by inject(named("daysOfWeek_RU"))

    private val dayNumber: TextView = columnHeaderItem.findViewById(R.id.journalColumnHeaderDayNumber)
    private val dayOfWeek: TextView = columnHeaderItem.findViewById(R.id.journalColumnHeaderDayOfWeek)

    fun setModel(model: ColumnHeaderModel)
    {
        dayNumber.text = model.data.timestamp.day.toString()

        val dayIndex = model.data.timestamp.dayOfWeek.index0Monday
        dayOfWeek.text = daysOfWeek[dayIndex]

        dayNumber.setTextColor(ContextCompat.getColor(App.getCtx(), R.color.colorText))
        dayOfWeek.setTextColor(ContextCompat.getColor(App.getCtx(), R.color.colorPrimaryLight))

        colorizeWeekend(dayIndex)
        colorizeToday(model.data.timestamp)
    }

    private fun colorizeWeekend(dayIndex: Int)
    {
        if ((dayIndex == 5) || (dayIndex == 6))
        {
            dayOfWeek.setTextColor(ContextCompat.getColor(App.getCtx(), R.color.colorJournalWeekend))
        }
    }

    private fun colorizeToday(date: Date)
    {
        if (DateTime.nowLocal().local.date == date)
        {
            dayNumber.setTextColor(ContextCompat.getColor(App.getCtx(), R.color.colorJournalPresence))
            dayOfWeek.setTextColor(ContextCompat.getColor(App.getCtx(), R.color.colorJournalPresence))
        }
    }
}