package ru.hryasch.coachnotes.journal.viewholders

import android.view.View
import android.widget.TextView
import com.evrencoskun.tableview.adapter.recyclerview.holder.AbstractViewHolder
import ru.hryasch.coachnotes.R
import ru.hryasch.coachnotes.journal.ColumnHeaderModel

class ColumnHeaderViewHolder(columnHeaderItem: View) : AbstractViewHolder(columnHeaderItem)
{
    private val dayNumber: TextView = columnHeaderItem.findViewById(R.id.journalColumnHeaderDayNumber)
    private val dayOfWeek: TextView = columnHeaderItem.findViewById(R.id.journalColumnHeaderDayOfWeek)

    fun setModel(model: ColumnHeaderModel)
    {
        dayNumber.text = model.data.day.toString()
        dayOfWeek.text = model.data.dayOfWeek
    }
}