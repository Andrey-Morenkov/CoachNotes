package ru.hryasch.coachnotes.journal.table.viewholders

import android.view.View
import android.widget.TextView
import android.content.Context
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat

import com.evrencoskun.tableview.adapter.recyclerview.holder.AbstractViewHolder

import ru.hryasch.coachnotes.R
import ru.hryasch.coachnotes.domain.journal.data.AbsenceData
import ru.hryasch.coachnotes.domain.journal.data.PresenceData


class CellViewHolder (cellItem: View, private val context: Context) : AbstractViewHolder(cellItem)
{
    private val data : TextView = cellItem.findViewById(R.id.journalCellData)
    private val layout: ConstraintLayout = cellItem.findViewById(R.id.journalCellLayout)

    var currentModel: CellModel? = null

    fun setModel(model: CellModel)
    {
        currentModel = model

        modifyData(model)
        modifyLayout(model)
    }

    private fun modifyLayout(model: CellModel)
    {
        when (model.data)
        {
            is PresenceData ->
            {
                data.setBackgroundColor(ContextCompat.getColor(context, R.color.colorJournalPresence))
            }

            is AbsenceData ->
            {
                (model.data as AbsenceData).mark?.let {
                    data.setBackgroundColor(ContextCompat.getColor(context, R.color.colorJournalAbsenceSpecial))
                } ?: let {
                    data.setBackgroundColor(ContextCompat.getColor(context, R.color.colorJournalAbsenceGeneral))
                }
            }
            else ->
            {
                data.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))
            }
        }
    }

    private fun modifyData(model: CellModel?)
    {
        data.text = ""

        if (model?.data is AbsenceData)
        {
            (model.data as AbsenceData).mark?.let { data.text = it }
        }
    }
}

