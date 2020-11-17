package ru.hryasch.coachnotes.journal.table.viewholders

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.evrencoskun.tableview.adapter.recyclerview.holder.AbstractViewHolder
import com.pawegio.kandroid.visible
import ru.hryasch.coachnotes.R
import ru.hryasch.coachnotes.journal.table.data.RowHeaderModel

class RowHeaderViewHolder(rowHeaderItem: View) : AbstractViewHolder(rowHeaderItem)
{
    private val surname: TextView = rowHeaderItem.findViewById(R.id.journalRowHeaderSurname)
    private val name: TextView = rowHeaderItem.findViewById(R.id.journalRowHeaderName)
    private val num: TextView = rowHeaderItem.findViewById(R.id.journalRowHeaderNum)
    private val foreground: ImageView = rowHeaderItem.findViewById(R.id.journalRowHeaderNoExist)

    private var internalId: Int = 0

    fun setModel(model: RowHeaderModel)
    {
        internalId = model.getId()

        surname.text = model.person.surname
        name.text = model.person.name
        num.text = model.index.toString()
        if (model.index > 0)
        {
            // Actual person
            num.visibility = View.VISIBLE
            foreground.visible = false
            surname.setTextColor(ContextCompat.getColor(surname.context, R.color.colorText))
            name.setTextColor(ContextCompat.getColor(surname.context, R.color.colorPrimaryLight))
        }
        else
        {
            // Removed person
            num.visibility = View.INVISIBLE
            foreground.visible = true
            surname.setTextColor(ContextCompat.getColor(surname.context, R.color.colorDisabledText))
            name.setTextColor(ContextCompat.getColor(surname.context, R.color.colorDisabledText))
        }
    }

    fun getInternalId() = internalId
}