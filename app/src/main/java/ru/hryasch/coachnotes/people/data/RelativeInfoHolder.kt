package ru.hryasch.coachnotes.people.data

import android.content.Context
import android.text.SpannableStringBuilder
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import com.google.android.material.textfield.TextInputEditText
import com.tiper.MaterialSpinner
import org.koin.core.KoinComponent
import org.koin.core.get
import org.koin.core.qualifier.named
import ru.hryasch.coachnotes.R
import ru.hryasch.coachnotes.domain.person.data.ParentType
import ru.hryasch.coachnotes.domain.person.data.RelativeInfo
import java.util.*

internal class RelativeInfoHolder(val context: Context, val layout: View, private val holderPosition: Int): KoinComponent
{
    private val relativesNames: Array<String> = get(named("relatives_RU"))

    private val nameHolder: TextInputEditText = layout.findViewById(R.id.editPersonEditTextParentFullName)
    private val typeHolder: MaterialSpinner   = layout.findViewById(R.id.personEditParentType)
    private val addPhoneButton: ImageView     = layout.findViewById(R.id.editPersonRelativeInfoImageViewAddPhone)
    private val deleteButton: ImageView       = layout.findViewById(R.id.editPersonImageViewDeleteRelativeInfo)

    private val phonesContainer: LinearLayout = layout.findViewById(R.id.relativeInfoPhonesContainer)
    private val phonesList: MutableList<RelativePhoneHolder> = Collections.emptyList()

    var onPhoneDeleteListener: OnPhoneDeleteListener? = null
    var onPhoneAddListener: OnPhoneAddListener? = null
    var onDeleteRelativeInfoHolder: OnDeleteRelativeInfoHolder? = null

    private val onPhoneHolderDeleteListener = object: OnPhoneHolderDeleteListener {
        override fun onPhoneDelete(phone: String, position: Int)
        {
            onPhoneDeleteListener?.onDeletePhone(phone, holderPosition, position)
            phonesList.removeAt(position)
            phonesContainer.removeViewAt(position)
        }
    }

    init
    {
        addPhoneView(0)
        initParentTypeHolder()
        initAddPhoneButton()
        initDeleteButton()
    }

    fun applyExistData(relativeInfo: RelativeInfo)
    {
        nameHolder.text = SpannableStringBuilder(relativeInfo.name)
        typeHolder.selection = relativesNames.indexOf(getParentTypeFullString(relativeInfo.type))
        for (i in 1 until relativeInfo.getPhones().size)
        {
            addPhoneView() // add additional phones (1st already here)
        }

        for ((i, phoneHolder) in phonesList.withIndex())
        {
            phoneHolder.phone.text = SpannableStringBuilder(relativeInfo.getPhones()[i])
        }
    }

    fun extractData(): RelativeInfo
    {
        val relativeInfo = RelativeInfo()

        if (nameHolder.text.isNullOrBlank())
        {
            relativeInfo.name = typeHolder.selectedItem!! as String
        }
        else
        {
            relativeInfo.name = nameHolder.text.toString()
        }

        relativeInfo.type = getFullInfoToParentType()
        for (phoneHolder in phonesList)
        {
            if (!phoneHolder.phone.text.isNullOrBlank())
            {
                relativeInfo.addPhone(phoneHolder.phone.text.toString())
            }
        }

        return relativeInfo
    }

    fun isBlank(): Boolean = !hasFilledPhoneHolders() && nameHolder.text.isNullOrBlank()

    fun areRequiredFieldsFilled(): Boolean
    {
        val hasFilledPhoneHolders = hasFilledPhoneHolders()
        val isNameHolderFilled = !nameHolder.text.isNullOrBlank()

        if (!hasFilledPhoneHolders && !isNameHolderFilled)
        {
            return true //blank
        }
        if (hasFilledPhoneHolders && !isNameHolderFilled)
        {
            return false
        }

        return true
    }

    private fun hasFilledPhoneHolders(): Boolean
    {
        for (phoneHolder in phonesList)
        {
            if (!phoneHolder.phone.text.isNullOrBlank())
            {
                return true
            }
        }
        return false
    }

    private fun addPhoneView(position: Int? = null)
    {
        val newPhoneView = View.inflate(context, R.layout.element_edit_person_relative_phone, null)
        if (position != null)
        {
            phonesContainer.addView(newPhoneView, position)
        }
        else
        {
            phonesContainer.addView(newPhoneView)
        }
        phonesList.add(RelativePhoneHolder(newPhoneView, phonesList.size).apply { onDeletePhoneListener = onPhoneHolderDeleteListener })
    }

    private fun initParentTypeHolder()
    {
        typeHolder.adapter = ArrayAdapter(context, android.R.layout.simple_list_item_1, relativesNames)
        typeHolder.selection = relativesNames.indexOf(context.getString(R.string.mother))
    }

    private fun initAddPhoneButton()
    {
        addPhoneButton.setOnClickListener {
            onPhoneAddListener?.onAddPhone(holderPosition)
            addPhoneView()
        }
    }

    private fun initDeleteButton()
    {
        deleteButton.setOnClickListener {
            onDeleteRelativeInfoHolder?.onDeleteInfoHolder(holderPosition)
        }
    }

    private fun getFullInfoToParentType(): ParentType
    {
        return when(typeHolder.selectedItem!!)
        {
            context.getString(R.string.mother)  -> ParentType.Mother
            context.getString(R.string.father)  -> ParentType.Father
            context.getString(R.string.grandMa) -> ParentType.GrandMother
            context.getString(R.string.grandFa) -> ParentType.GrandFather
            context.getString(R.string.aunt)    -> ParentType.Aunt
            context.getString(R.string.uncle)   -> ParentType.Uncle
            context.getString(R.string.brother) -> ParentType.Brother
            context.getString(R.string.sister)  -> ParentType.Sister
            else -> ParentType.Mother
        }
    }

    private fun getParentTypeFullString(parentType: ParentType): String
    {
        return when(parentType)
        {
            ParentType.Mother -> context.getString(R.string.mother)
            ParentType.Father -> context.getString(R.string.father)
            ParentType.GrandMother -> context.getString(R.string.grandMa)
            ParentType.GrandFather -> context.getString(R.string.grandFa)
            ParentType.Aunt -> context.getString(R.string.aunt)
            ParentType.Uncle -> context.getString(R.string.uncle)
            ParentType.Brother -> context.getString(R.string.brother)
            ParentType.Sister -> context.getString(R.string.sister)
            else -> context.getString(R.string.mother)
        }
    }
}

internal interface OnPhoneDeleteListener
{
    fun onDeletePhone(phone: String, infoHolderPosition: Int, phoneHolderPosition: Int)
}

internal interface OnPhoneAddListener
{
    fun onAddPhone(infoHolderPosition: Int)
}

internal interface OnDeleteRelativeInfoHolder
{
    fun onDeleteInfoHolder(position: Int)
}

