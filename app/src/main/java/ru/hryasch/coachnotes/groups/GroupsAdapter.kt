package ru.hryasch.coachnotes.groups

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SortedList
import com.soywiz.klock.DateTime
import ru.hryasch.coachnotes.R
import ru.hryasch.coachnotes.domain.group.data.Group

class GroupsAdapter(groupsList: List<Group>,
                    private val listener: GroupClickListener): RecyclerView.Adapter<GroupViewHolder>()
{
    private val groupsList: List<Group> = groupsList.sorted()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder
    {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.preview_group, parent, false)
        return GroupViewHolder(view, listener)
    }

    override fun getItemCount(): Int = groupsList.size

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int)
    {
        holder.bind(groupsList[position])
    }

    interface GroupClickListener
    {
        fun onGroupClick(group: Group)
    }
}

class GroupViewHolder(itemView: View, private val listener: GroupsAdapter.GroupClickListener) : RecyclerView.ViewHolder(itemView), View.OnClickListener
{
    private var name: TextView              = itemView.findViewById(R.id.groupTextViewName)
    private var peopleCount: TextView       = itemView.findViewById(R.id.groupTextViewPeopleCount)
    private var peopleAbsoluteAge: TextView = itemView.findViewById(R.id.groupTextViewAbsoluteAge)
    private var peopleRelativeAge: TextView = itemView.findViewById(R.id.groupTextViewRelativeAge)
    private var paidLabel: ImageView = itemView.findViewById(R.id.label_paid)

    private lateinit var currentGroup: Group

    init
    {
        itemView.setOnClickListener(this)
    }

    fun bind(group: Group)
    {
        currentGroup = group

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

    override fun onClick(p0: View?)
    {
        listener.onGroupClick(currentGroup)
    }
}

fun IntRange.isSingle(): Boolean = start == endInclusive