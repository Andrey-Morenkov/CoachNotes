package ru.hryasch.coachnotes.journal.viewholders

import android.view.View
import android.widget.TextView
import com.evrencoskun.tableview.adapter.recyclerview.holder.AbstractViewHolder
import ru.hryasch.coachnotes.R
import ru.hryasch.coachnotes.journal.RowHeaderModel

class RowHeaderViewHolder(rowHeaderItem: View) : AbstractViewHolder(rowHeaderItem)
{
    private val surname: TextView = rowHeaderItem.findViewById(R.id.journalRowHeaderSurname)
    private val name: TextView = rowHeaderItem.findViewById(R.id.journalRowHeaderName)

    fun setModel(model: RowHeaderModel)
    {
        surname.text = model.data.surname
        name.text = model.data.name
    }
}