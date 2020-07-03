package ru.hryasch.coachnotes.groups

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ru.hryasch.coachnotes.R
import ru.hryasch.coachnotes.domain.common.GroupId
import ru.hryasch.coachnotes.domain.person.data.Person

class GroupMembersAdapter(private val peopleList: MutableList<Person>,
                          private val groupNames: Map<GroupId, String>,
                          private val listener: RemovePersonListener): RecyclerView.Adapter<GroupMemberViewHolder>()
{
    init
    {
        peopleList.sort()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupMemberViewHolder
    {
        return GroupMemberViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.preview_person, parent, false), listener)
    }

    override fun getItemCount(): Int = peopleList.size

    override fun onBindViewHolder(holder: GroupMemberViewHolder, position: Int)
    {
        holder.bind(peopleList[position], groupNames[peopleList[position].groupId])
    }

    interface RemovePersonListener
    {
        fun onPersonRemoveFromGroup(person: Person)
    }
}

class GroupMemberViewHolder(itemView: View, private val listener: GroupMembersAdapter.RemovePersonListener): RecyclerView.ViewHolder(itemView)
{
    private val fullName: TextView = itemView.findViewById(R.id.personTextViewFullName)
    private val groupName: TextView = itemView.findViewById(R.id.personTextViewGroupName)
    private val paidLabel: ImageView = itemView.findViewById(R.id.label_paid)
    private val removePerson: ImageView = itemView.findViewById(R.id.personImageViewRemoveFromGroup)

    fun bind(person: Person, group: String?)
    {
        fullName.text = itemView.context.getString(R.string.person_full_name_pattern, person.surname, person.name)
        groupName.text =
            if (person.groupId == null)
            {
                "Нет группы"
            }
            else
            {
                group ?: "Нет группы"
            }
        removePerson.visibility = View.VISIBLE
        removePerson.setOnClickListener {
            listener.onPersonRemoveFromGroup(person)
        }

        if (person.isPaid)
        {
            paidLabel.visibility = View.VISIBLE
        }
        else
        {
            paidLabel.visibility = View.INVISIBLE
        }
    }
}