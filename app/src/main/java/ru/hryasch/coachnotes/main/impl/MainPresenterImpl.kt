package ru.hryasch.coachnotes.main.impl

import androidx.fragment.app.Fragment
import com.pawegio.kandroid.e
import com.pawegio.kandroid.i
import kotlinx.coroutines.ExperimentalCoroutinesApi
import moxy.InjectViewState
import moxy.MvpPresenter
import ru.hryasch.coachnotes.fragments.MainView
import ru.hryasch.coachnotes.fragments.impl.GroupListFragment
import ru.hryasch.coachnotes.fragments.impl.HomeFragment
import ru.hryasch.coachnotes.fragments.impl.PeopleListFragment
import ru.hryasch.coachnotes.main.MainPresenter

@InjectViewState
class MainPresenterImpl: MvpPresenter<MainView>(), MainPresenter
{
    private var currentFragment: Int = 0
    private var fragmentsList: List<Fragment>? = null

    @ExperimentalCoroutinesApi
    override fun initPresenter()
    {
        if (fragmentsList == null)
        {
            generateFragments()
            viewState.loadFragments(fragmentsList!!)
            viewState.pickFragment(currentFragment)
        }
    }

    override fun onFragmentSwitched(newFragmentPos: Int)
    {
        currentFragment = newFragmentPos
        viewState.pickFragment(currentFragment)
    }

    @ExperimentalCoroutinesApi
    private fun generateFragments()
    {
        val fragments: MutableList<Fragment> = ArrayList(3)
        fragments.add(HomeFragment())
        i("generated HomeFragment")

        fragments.add(GroupListFragment())
        i("generated GroupListFragment")

        fragments.add(PeopleListFragment())
        i("generated PeopleListFragment")

        fragmentsList = fragments
    }
}