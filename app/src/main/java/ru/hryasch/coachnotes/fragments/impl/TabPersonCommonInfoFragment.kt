package ru.hryasch.coachnotes.fragments.impl

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pawegio.kandroid.visible
import com.pawegio.kandroid.w
import kotlinx.android.synthetic.main.element_person_parent_info.*
import ru.hryasch.coachnotes.R
import ru.hryasch.coachnotes.domain.person.data.Person
import ru.hryasch.coachnotes.domain.person.data.RelativeInfo
import ru.hryasch.coachnotes.people.PersonRelativesAdapter
import java.util.*

class TabPersonCommonInfoFragment(private val personData: Person): Fragment()
{
    // Parents info section
    private lateinit var parentsInfoView: View
    private lateinit var parentsInfoContainer: RecyclerView
    private val parentsList: MutableList<RelativeInfo> = LinkedList()

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View?
    {
        val layout = inflater.inflate(R.layout.tab_fragment_person_common, container, false)

        parentsInfoView = layout.findViewById(R.id.personInfoParentsCard)
        parentsInfoContainer = layout.findViewById(R.id.personInfoParentsContainer)

        parentsList.clear()
        parentsList.addAll(personData.relativeInfos)
        if (parentsList.isEmpty())
        {
            parentsInfoView.visible = false
        }
        else
        {
            w("parentsList.size = ${parentsList.size}")
            parentsInfoView.visible = true
            parentsInfoContainer.adapter = PersonRelativesAdapter(parentsList, requireActivity())
            parentsInfoContainer.layoutManager = LinearLayoutManager(context)
        }

        return layout
    }
}