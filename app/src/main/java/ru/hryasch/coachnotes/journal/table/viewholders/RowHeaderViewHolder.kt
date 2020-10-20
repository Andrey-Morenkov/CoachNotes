package ru.hryasch.coachnotes.journal.table.viewholders

import android.view.View
import android.widget.TextView
import com.evrencoskun.tableview.adapter.recyclerview.holder.AbstractViewHolder
import ru.hryasch.coachnotes.R
import ru.hryasch.coachnotes.journal.table.data.RowHeaderModel

class RowHeaderViewHolder(rowHeaderItem: View) : AbstractViewHolder(rowHeaderItem)
{
    private val surname: TextView = rowHeaderItem.findViewById(R.id.journalRowHeaderSurname)
    private val name: TextView = rowHeaderItem.findViewById(R.id.journalRowHeaderName)
    private val num: TextView = rowHeaderItem.findViewById(R.id.journalRowHeaderNum)

    private var internalId: Int = 0

    fun setModel(model: RowHeaderModel)
    {
        internalId = model.getId()

        surname.text = model.person.surname
        name.text = model.person.name
        num.text = model.index.toString()
    }

    fun getInternalId() = internalId
}