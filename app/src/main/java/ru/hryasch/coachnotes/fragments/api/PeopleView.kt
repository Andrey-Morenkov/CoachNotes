package ru.hryasch.coachnotes.fragments.api

import androidx.recyclerview.widget.SortedList
import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.OneExecutionStateStrategy
import moxy.viewstate.strategy.StateStrategyType
import ru.hryasch.coachnotes.domain.common.GroupId
import ru.hryasch.coachnotes.domain.person.data.Person

interface PeopleView: MvpView
{
    @StateStrategyType(AddToEndSingleStrategy::class)
    fun setPeopleList(peopleList: List<Person>?, groupNames: Map<GroupId, String>? = null)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun refreshData()
}