package ru.hryasch.coachnotes.fragments.impl

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.fxn.BubbleTabBar
import com.fxn.OnBubbleClickListener
import kotlinx.coroutines.ExperimentalCoroutinesApi
import moxy.MvpAppCompatFragment
import moxy.presenter.InjectPresenter
import org.koin.core.KoinComponent
import ru.hryasch.coachnotes.R
import ru.hryasch.coachnotes.fragments.MainView
import ru.hryasch.coachnotes.main.adapters.CustomMainAdapter
import ru.hryasch.coachnotes.main.impl.MainPresenterImpl
import ru.hryasch.coachnotes.main.view.NoScrollViewPager

class MainFragment: MvpAppCompatFragment(), MainView, KoinComponent
{
    @InjectPresenter
    lateinit var presenter: MainPresenterImpl

    // Views
        private lateinit var navigation: BubbleTabBar
        private lateinit var fragmentViewPager: NoScrollViewPager
    //Data
        private lateinit var fragmentAdapter: CustomMainAdapter

    @ExperimentalCoroutinesApi
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View?
    {
        val layout = inflater.inflate(R.layout.fragment_main, container, false)

        fragmentViewPager = layout.findViewById(R.id.homeSpace)
        navigation = layout.findViewById(R.id.homeNavigation)

        presenter.initPresenter()

        return layout
    }

    override fun loadFragments(fragments: List<Fragment>)
    {
        fragmentAdapter = CustomMainAdapter(childFragmentManager, fragments)
        fragmentViewPager.adapter = fragmentAdapter
        fragmentViewPager.offscreenPageLimit = fragments.size

        navigation.addBubbleListener(object: OnBubbleClickListener {
            override fun onBubbleClick(id: Int)
            {
                when(id)
                {
                    R.id.homeFragmentImpl   -> presenter.onFragmentSwitched(0)
                    R.id.groupListFragment  -> presenter.onFragmentSwitched(1)
                    R.id.peopleListFragment -> presenter.onFragmentSwitched(2)
                }
            }
        })
        navigation.setupBubbleTabBar(fragmentViewPager)
    }

    override fun pickFragment(fragmentPos: Int)
    {
        fragmentViewPager.currentItem = fragmentPos
        navigation.setSelected(fragmentPos, false)
    }
}