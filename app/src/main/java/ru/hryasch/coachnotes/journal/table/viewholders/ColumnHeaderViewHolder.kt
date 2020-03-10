package ru.hryasch.coachnotes.journal.table.viewholders

import android.view.View
import android.widget.TextView
import com.evrencoskun.tableview.adapter.recyclerview.holder.AbstractViewHolder
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.koin.core.qualifier.named
import ru.hryasch.coachnotes.R

class ColumnHeaderViewHolder(columnHeaderItem: View) : AbstractViewHolder(columnHeaderItem), KoinComponent
{
    private val daysOfWeek: Array<String> by inject(named("daysOfWeek_RU"))

    private val dayNumber: TextView = columnHeaderItem.findViewById(R.id.journalColumnHeaderDayNumber)
    private val dayOfWeek: TextView = columnHeaderItem.findViewById(R.id.journalColumnHeaderDayOfWeek)

    fun setModel(model: ColumnHeaderModel)
    {
        dayNumber.text = model.data.timestamp.day.toString()
        dayOfWeek.text = daysOfWeek[model.data.timestamp.dayOfWeek.index0]
    }
}