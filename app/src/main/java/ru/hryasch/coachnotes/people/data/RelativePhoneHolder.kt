package ru.hryasch.coachnotes.people.data

import android.view.View
import android.widget.ImageView
import com.google.android.material.textfield.TextInputEditText
import com.pawegio.kandroid.visible
import ru.hryasch.coachnotes.R
import ru.tinkoff.decoro.MaskImpl
import ru.tinkoff.decoro.slots.PredefinedSlots
import ru.tinkoff.decoro.watchers.MaskFormatWatcher

internal class RelativePhoneHolder(layout: View, private var position: Int)
{
    val phone: TextInputEditText = layout.findViewById(R.id.editPersonEditTextParentPhone)
    private val deletePhone: ImageView   = layout.findViewById(R.id.editPersonRelativeInfoImageViewRemovePhone)
    var onDeletePhoneListener: OnPhoneHolderDeleteListener? = null

    init
    {
        MaskFormatWatcher(MaskImpl.createTerminated(PredefinedSlots.RUS_PHONE_NUMBER)).installOn(phone)
        if (position == 0)
        {
            deletePhone.visible = false
        }
        else
        {
            deletePhone.setOnClickListener {
                onDeletePhoneListener?.onPhoneDelete(phone.text.toString(), position)
            }
        }
    }

    fun updateIndex(index: Int)
    {
        position = index
    }
}

internal interface OnPhoneHolderDeleteListener
{
    fun onPhoneDelete(phone: String, position: Int)
}