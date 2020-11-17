package ru.hryasch.coachnotes.main

interface MainPresenter
{
    fun initPresenter()
    fun onFragmentSwitched(newFragmentPos: Int)
}