package ru.hryasch.coachnotes.people

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.koin.core.KoinComponent
import ru.hryasch.coachnotes.R
import ru.hryasch.coachnotes.domain.person.data.RelativeInfo
import java.util.*


class PersonRelativesAdapter(private val relativesList: MutableList<RelativeInfo>, private val context: Context): RecyclerView.Adapter<PersonRelativeViewHolder>()
{
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PersonRelativeViewHolder
    {
        return PersonRelativeViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.element_single_parent_info, parent, false), context)
    }

    override fun getItemCount(): Int
    {
        return relativesList.size
    }

    override fun onBindViewHolder(holder: PersonRelativeViewHolder, position: Int)
    {
        holder.bind(relativesList[position])
    }
}

class PersonRelativeViewHolder(itemView: View, private val context: Context): RecyclerView.ViewHolder(itemView), KoinComponent
{
    private val name: TextView = itemView.findViewById(R.id.personInfoTextViewParentName)
    private val type: TextView = itemView.findViewById(R.id.personInfoTextViewParentType)
    private val phonesContainer: RecyclerView = itemView.findViewById(R.id.personInfoRecyclerViewParentPhoneContainer)
    private val phonesList: MutableList<String> = LinkedList()

    fun bind(relativeInfo: RelativeInfo)
    {
        name.text = relativeInfo.name
        type.text = relativeInfo.type.toString()
        phonesList.addAll(relativeInfo.getPhones())
        phonesContainer.adapter = PersonRelativePhonesAdapter(phonesList, context)
        phonesContainer.layoutManager = LinearLayoutManager(itemView.context)
    }
}

class PersonRelativePhonesAdapter(private val phonesList: MutableList<String>, private val context: Context): RecyclerView.Adapter<PersonRelativePhoneViewHolder>()
{
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PersonRelativePhoneViewHolder
    {
        return PersonRelativePhoneViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.element_person_info_parent_phone, parent, false), context)
    }

    override fun getItemCount(): Int
    {
        return phonesList.size
    }

    override fun onBindViewHolder(holder: PersonRelativePhoneViewHolder, position: Int)
    {
        holder.bind(phonesList[position])
    }
}

class PersonRelativePhoneViewHolder(itemView: View, private val context: Context): RecyclerView.ViewHolder(itemView)
{
    private lateinit var phone: String
    private val phoneView: TextView = itemView.findViewById(R.id.personInfoTextViewParentPhone)
    private val sendSms: ImageButton = itemView.findViewById(R.id.personInfoImageButtonSendSms)

    fun bind(phoneString: String)
    {
        phone = phoneString
        phoneView.text = phone
        phoneView.setOnClickListener {
            // open phone
            val intent = Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phone, null))
            startActivity(context, intent, null)
        }
        sendSms.setOnClickListener {
            startActivity(context, Intent(Intent.ACTION_VIEW, Uri.fromParts("sms", phone, null)), null)
        }
    }
}