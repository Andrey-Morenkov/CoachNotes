package ru.hryasch.coachnotes.fragments.impl

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import me.ibrahimsn.lib.OnItemSelectedListener
import me.ibrahimsn.lib.SmoothBottomBar
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
        private lateinit var layout: View
        private lateinit var navigation: SmoothBottomBar
        private lateinit var fragmentViewPager: NoScrollViewPager
    //Data
        private lateinit var fragmentAdapter: CustomMainAdapter



    @ExperimentalCoroutinesApi
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        if (::layout.isInitialized)
        {
            return layout
        }

        layout = inflater.inflate(R.layout.fragment_main, container, false)

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

        navigation.onItemSelectedListener = object: OnItemSelectedListener {
            override fun onItemSelect(pos: Int): Boolean
            {
                presenter.onFragmentSwitched(pos)
                return true
            }
        }
    }

    override fun pickFragment(fragmentPos: Int)
    {
        fragmentViewPager.currentItem = fragmentPos
        navigation.itemActiveIndex = fragmentPos
    }
}