package ru.hryasch.coachnotes.fragments.impl

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.pawegio.kandroid.i
import com.pawegio.kandroid.visible
import ru.hryasch.coachnotes.R
import ru.hryasch.coachnotes.domain.person.data.Person

class TabPersonCommonInfoFragment(private val personData: Person): Fragment()
{
    // header
    private lateinit var birthday: TextView

    private lateinit var parentsCard: CardView
    private lateinit var commonCard: CardView

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View?
    {
        val layout = inflater.inflate(R.layout.tab_fragment_person_common, container, false)

        birthday = layout.findViewById(R.id.personInfoTextViewBirthday)
        parentsCard = layout.findViewById(R.id.personInfoParentsCard)
        commonCard = layout.findViewById(R.id.personInfoCommonCard)

        personData.birthday?.let {
            commonCard.visible = true
            birthday.text = it.format("dd/MM/yyyy")
        } ?: let { commonCard.visible = false }

        parentsCard.visible = false

        return layout
    }
}