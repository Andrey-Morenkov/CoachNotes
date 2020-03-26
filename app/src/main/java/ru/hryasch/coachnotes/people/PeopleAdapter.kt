package ru.hryasch.coachnotes.people

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SortedList
import ru.hryasch.coachnotes.R
import ru.hryasch.coachnotes.domain.common.GroupId
import ru.hryasch.coachnotes.domain.person.data.Person

class PeopleAdapter(peopleList: List<Person>, private val groupNames: Map<GroupId, String>): RecyclerView.Adapter<PersonViewHolder>()
{
    private val peopleList: List<Person> = peopleList.sorted()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PersonViewHolder
    {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.preview_person, parent, false)

        val card: CardView = view.findViewById(R.id.cardPersonPreview)
        card.setCardBackgroundColor(ContextCompat.getColor(view.context, android.R.color.transparent))
        card.elevation = 0f
        val lp = card.layoutParams as (ViewGroup.MarginLayoutParams)
        lp.bottomMargin = 0
        card.layoutParams = lp

        card.requestLayout()

        return PersonViewHolder(view)
    }

    override fun getItemCount(): Int = peopleList.size

    override fun onBindViewHolder(holder: PersonViewHolder, position: Int)
    {
        holder.bind(peopleList[position], groupNames[peopleList[position].groupId])
    }
}

class PersonViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
{
    private var fullName:  TextView  = itemView.findViewById(R.id.personTextViewFullName)
    private var groupName: TextView  = itemView.findViewById(R.id.personTextViewGroupName)
    private var paidLabel: ImageView = itemView.findViewById(R.id.label_paid)

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