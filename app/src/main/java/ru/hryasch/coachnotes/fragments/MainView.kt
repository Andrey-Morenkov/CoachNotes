package ru.hryasch.coachnotes.fragments

import androidx.fragment.app.Fragment
import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.SingleStateStrategy
import moxy.viewstate.strategy.StateStrategyType

interface MainView: MvpView
{
    @StateStrategyType(SingleStateStrategy::class)
    fun loadFragments(fragments: List<Fragment>)

    @StateStrategyType(AddToEndSingleStrategy::class)
    fun pickFragment(fragmentPos: Int)
}