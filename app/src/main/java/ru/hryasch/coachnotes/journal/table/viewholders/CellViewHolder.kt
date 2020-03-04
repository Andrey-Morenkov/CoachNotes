package ru.hryasch.coachnotes.journal.table.viewholders

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.content.Context
import androidx.core.content.ContextCompat

import com.evrencoskun.tableview.adapter.recyclerview.holder.AbstractViewHolder
import com.pawegio.kandroid.i

import ru.hryasch.coachnotes.R
import ru.hryasch.coachnotes.journal.table.AbsenceData
import ru.hryasch.coachnotes.journal.table.CellModel
import ru.hryasch.coachnotes.journal.table.PresenceData


class CellViewHolder (cellItem: View, private val context: Context) : AbstractViewHolder(cellItem)
{
    private val data : TextView = cellItem.findViewById(R.id.journalCellData)

    var currentModel: CellModel? = null

    fun setModel(model: CellModel)
    {
        i("Changing model: $currentModel -> $model")
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
                i("modifyLayout: detected PresenceData")
                data.setBackgroundColor(ContextCompat.getColor(context, R.color.colorJournalPresence))
            }

            is AbsenceData ->
            {
                (model.data as AbsenceData).mark?.let {
                    i("modifyLayout: detected AbsenceData MARK")
                    data.setBackgroundColor(ContextCompat.getColor(context, R.color.colorJournalAbsenceSpecial))
                } ?: let {
                    i("modifyLayout: detected AbsenceData NO MARK")
                    data.setBackgroundColor(ContextCompat.getColor(context, R.color.colorJournalAbsenceGeneral))
                }
            }
            else ->
            {
                data.setBackgroundColor(ContextCompat.getColor(context, R.color.colorJournalBackground))
                i("modifyLayout: detected null")
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

