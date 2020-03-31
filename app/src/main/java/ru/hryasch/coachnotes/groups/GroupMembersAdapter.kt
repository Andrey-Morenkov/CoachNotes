package ru.hryasch.coachnotes.groups

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import ru.hryasch.coachnotes.R
import ru.hryasch.coachnotes.domain.common.GroupId
import ru.hryasch.coachnotes.domain.person.data.Person

class GroupMembersAdapter(peopleList: List<Person>,
                          private val groupNames: Map<GroupId, String>,
                          private val listener: RemovePersonListener): RecyclerView.Adapter<GroupMemberViewHolder>()
{
    private val peopleList: List<Person> = peopleList.sorted()

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
    private var fullName: TextView = itemView.findViewById(R.id.personTextViewFullName)
    private var groupName: TextView = itemView.findViewById(R.id.personTextViewGroupName)
    private var paidLabel: ImageView = itemView.findViewById(R.id.label_paid)
    private val removePerson: ImageView = itemView.findViewById(R.id.personImageViewRemoveFromGroup)

    private lateinit var person: Person

    fun bind(person: Person, group: String?)
    {
        this.person = person

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