package ru.hryasch.coachnotes.journal.viewholders

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.content.Context
import androidx.core.content.ContextCompat

import com.evrencoskun.tableview.adapter.recyclerview.holder.AbstractViewHolder

import ru.hryasch.coachnotes.R
import ru.hryasch.coachnotes.journal.AbsenceData
import ru.hryasch.coachnotes.journal.CellModel
import ru.hryasch.coachnotes.journal.PresenceData


class CellViewHolder (cellItem: View, private val context: Context) : AbstractViewHolder(cellItem)
{
    private val data : TextView = cellItem.findViewById(R.id.journalCellData)
    private val layout: ViewGroup = cellItem.findViewById(R.id.journalCellLayout)

    fun setModel(model: CellModel)
    {
        data.text = ""

        when (model.data)
        {
            is PresenceData -> layout.setBackgroundColor(ContextCompat.getColor(context, R.color.colorJournalPresence))
            is AbsenceData  ->
            {
                if ((model.data as AbsenceData).mark != null)
                {
                    layout.setBackgroundColor(ContextCompat.getColor(context, R.color.colorJournalAbsenceSpecial))
                    data.text = model.data!!.mark
                }
                else
                {
                    layout.setBackgroundColor(ContextCompat.getColor(context, R.color.colorJournalAbsenceGeneral))
                }
            }

            else -> layout.setBackgroundColor(ContextCompat.getColor(context, R.color.colorJournalBackground))
        }
    }
}

