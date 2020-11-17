package ru.hryasch.coachnotes.main.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class CustomMainAdapter(fm: FragmentManager, val fragments: List<Fragment>): FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT)
{
    override fun getCount(): Int
    {
        return fragments.size
    }

    override fun getItem(position: Int): Fragment
    {
        return fragments[position]
    }
}