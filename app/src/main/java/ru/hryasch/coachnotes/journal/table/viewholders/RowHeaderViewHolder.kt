package ru.hryasch.coachnotes.journal.table.viewholders

import android.view.View
import android.widget.TextView
import com.evrencoskun.tableview.adapter.recyclerview.holder.AbstractViewHolder
import ru.hryasch.coachnotes.R

class RowHeaderViewHolder(rowHeaderItem: View) : AbstractViewHolder(rowHeaderItem)
{
    private val surname: TextView = rowHeaderItem.findViewById(R.id.journalRowHeaderSurname)
    private val name: TextView = rowHeaderItem.findViewById(R.id.journalRowHeaderName)
    private val num: TextView = rowHeaderItem.findViewById(R.id.journalRowHeaderNum)

    fun setModel(model: RowHeaderModel)
    {
        surname.text = model.data.person.surname
        name.text = model.data.person.name
        num.text = model.data.number.toString()
    }
}