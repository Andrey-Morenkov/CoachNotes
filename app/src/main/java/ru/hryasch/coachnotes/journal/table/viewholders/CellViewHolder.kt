package ru.hryasch.coachnotes.journal.table.viewholders

import android.view.View
import android.widget.TextView
import android.content.Context
import android.content.res.ColorStateList
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding
import androidx.core.widget.ImageViewCompat

import com.evrencoskun.tableview.adapter.recyclerview.holder.AbstractViewHolder
import com.pawegio.kandroid.visible

import ru.hryasch.coachnotes.R
import ru.hryasch.coachnotes.domain.journal.data.AbsenceData
import ru.hryasch.coachnotes.domain.journal.data.NoExistData
import ru.hryasch.coachnotes.domain.journal.data.PresenceData
import ru.hryasch.coachnotes.domain.journal.data.UnknownData
import ru.hryasch.coachnotes.journal.table.data.CellModel


class CellViewHolder (cellItem: View, private val context: Context) : AbstractViewHolder(cellItem)
{
    private val dataCommon : TextView           = cellItem.findViewById(R.id.journalCellData)
    private val dataSpecial: AppCompatImageView = cellItem.findViewById(R.id.journalCellDataSpecial)

    lateinit var currentModel: CellModel

    fun setModel(model: CellModel)
    {
        currentModel = model

        when (currentModel.data)
        {
            is AbsenceData, is PresenceData -> applyCommonModel()
            is NoExistData, is UnknownData  -> applySpecialModel()
            else -> applyEmptyModel()
        }
    }

    fun getInternalId(): String
    {
        return currentModel.id
    }

    private fun applyCommonModel()
    {
        dataCommon.visible = true
        dataSpecial.visible = false

        modifyData()
        modifyLayout()
    }

    private fun applySpecialModel()
    {
        dataCommon.visible = false
        dataSpecial.visible = true

        modifyForeground()
    }

    private fun applyEmptyModel()
    {
        dataCommon.visible = true
        dataSpecial.visible = false

        dataCommon.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))
        dataCommon.text = ""
    }

    private fun modifyLayout()
    {
        when (currentModel.data)
        {
            is PresenceData ->
            {
                dataCommon.setBackgroundColor(ContextCompat.getColor(context, R.color.colorJournalPresence))
            }

            is AbsenceData ->
            {
                (currentModel.data as AbsenceData).mark?.also {
                    dataCommon.setBackgroundColor(ContextCompat.getColor(context, R.color.colorJournalAbsenceSpecial))
                } ?: also {
                    dataCommon.setBackgroundColor(ContextCompat.getColor(context, R.color.colorJournalAbsenceGeneral))
                }
            }
        }
    }

    private fun modifyData()
    {
        dataCommon.text = ""

        if (currentModel.data is AbsenceData)
        {
            (currentModel.data as AbsenceData).mark?.let { dataCommon.text = it }
        }
    }

    private fun modifyForeground()
    {
        when (currentModel.data)
        {
            is NoExistData ->
            {
                ImageViewCompat.setImageTintList(dataSpecial, null)
                dataSpecial.setPadding(0)
                dataSpecial.setImageResource(R.drawable.ic_baseline_texture_24)
            }

            is UnknownData ->
            {
                ImageViewCompat.setImageTintList(dataSpecial, ColorStateList.valueOf(ContextCompat.getColor(context, R.color.colorJournalAbsenceGeneral)))
                dataSpecial.setPadding(context.resources.getDimensionPixelSize(R.dimen.journal_cell_default_padding))
                dataSpecial.setImageResource(R.drawable.ic_attention)
            }
        }
    }
}

