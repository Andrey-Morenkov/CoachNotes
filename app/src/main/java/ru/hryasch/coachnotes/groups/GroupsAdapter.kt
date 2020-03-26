package ru.hryasch.coachnotes.groups

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SortedList
import com.soywiz.klock.DateTime
import ru.hryasch.coachnotes.R
import ru.hryasch.coachnotes.domain.group.data.Group

class GroupsAdapter(groupsList: List<Group>): RecyclerView.Adapter<GroupViewHolder>()
{
    private val groupsList: List<Group> = groupsList.sorted()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder
    {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.preview_group, parent, false)
        return GroupViewHolder(view)
    }

    override fun getItemCount(): Int = groupsList.size

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int)
    {
        holder.bind(groupsList[position])
    }

}

class GroupViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
{
    private var name: TextView              = itemView.findViewById(R.id.groupTextViewName)
    private var peopleCount: TextView       = itemView.findViewById(R.id.groupTextViewPeopleCount)
    private var peopleAbsoluteAge: TextView = itemView.findViewById(R.id.groupTextViewAbsoluteAge)
    private var peopleRelativeAge: TextView = itemView.findViewById(R.id.groupTextViewRelativeAge)
    private var paidLabel: ImageView        = itemView.findViewById(R.id.label_paid)

    fun bind(group: Group)
    {
        name.text = group.name
        peopleCount.text = group.membersList.size.toString()

        val groupAge = group.availableAbsoluteAge
        if (groupAge == null)
        {
            peopleAbsoluteAge.text = itemView.context.getString(R.string.group_absolute_age_single_pattern, "?")
            peopleRelativeAge.text = itemView.context.getString(R.string.group_relative_age_single_pattern, "?")
        }
        else
        {
            val now = DateTime.nowLocal()
            if (groupAge.isSingle())
            {
                peopleAbsoluteAge.text = itemView.context.getString(R.string.group_absolute_age_single_pattern, groupAge.first.toString())
                peopleRelativeAge.text = itemView.context.getString(R.string.group_relative_age_single_pattern, (now.yearInt - groupAge.first).toString())
            }
            else
            {
                peopleAbsoluteAge.text = itemView.context.getString(R.string.group_absolute_age_range_pattern, groupAge.first.toString(), groupAge.last.toString())
                peopleRelativeAge.text = itemView.context.getString(R.string.group_relative_age_range_pattern, (now.yearInt - groupAge.last).toString(), (now.yearInt - groupAge.first).toString())
            }
        }

        if (group.isPaid)
        {
            paidLabel.visibility = View.VISIBLE
        }
        else
        {
            paidLabel.visibility = View.INVISIBLE
        }
    }
}

private fun IntRange.isSingle(): Boolean = start == endInclusive