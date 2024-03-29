package ru.hryasch.coachnotes.groups

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ru.hryasch.coachnotes.R
import ru.hryasch.coachnotes.domain.group.data.Group
import java.time.ZonedDateTime

class GroupsAdapter(private val sortedGroupsList: List<Group>,
                    private val listener: GroupClickListener): RecyclerView.Adapter<GroupViewHolder>()
{
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder
    {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.preview_group, parent, false)
        return GroupViewHolder(view, listener)
    }

    override fun getItemCount(): Int = sortedGroupsList.size

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int)
    {
        holder.bind(sortedGroupsList[position])
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

        val ageLow = group.availableAbsoluteAgeLow
        val ageHigh = group.availableAbsoluteAgeHigh

        if (ageLow == null && ageHigh == null)
        {
            peopleAbsoluteAge.text = itemView.context.getString(R.string.group_absolute_age_single_pattern, "?")
            peopleRelativeAge.text = itemView.context.getString(R.string.group_relative_age_single_pattern, "?")
        }
        else
        {
            val now = ZonedDateTime.now()
            if (ageHigh == null)
            {
                peopleAbsoluteAge.text = itemView.context.getString(R.string.group_absolute_age_single_pattern, ageLow!!.toString())
                peopleRelativeAge.text = itemView.context.getString(R.string.group_relative_age_single_pattern, (now.year - ageLow).toString())
            }
            else
            {
                peopleAbsoluteAge.text = itemView.context.getString(R.string.group_absolute_age_range_pattern, ageLow!!.toString(), ageHigh.toString())
                peopleRelativeAge.text = itemView.context.getString(R.string.group_relative_age_range_pattern, (now.year - ageHigh).toString(), (now.year - ageLow).toString())
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