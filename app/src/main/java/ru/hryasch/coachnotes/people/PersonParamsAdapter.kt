package ru.hryasch.coachnotes.people

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.pawegio.kandroid.i
import ru.hryasch.coachnotes.domain.person.data.Person
import ru.hryasch.coachnotes.fragments.impl.TabPersonCommonInfoFragment

class PersonParamsAdapter(fragment: Fragment, private val person: Person): FragmentStateAdapter(fragment)
{
    init
    {
        i("new ADAPTER")
    }

    override fun getItemCount(): Int = 1

    override fun createFragment(position: Int): Fragment
    {
        return TabPersonCommonInfoFragment(person)
    }
}