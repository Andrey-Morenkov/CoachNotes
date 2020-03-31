package ru.hryasch.coachnotes.fragments

import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.SingleStateStrategy
import moxy.viewstate.strategy.StateStrategyType
import ru.hryasch.coachnotes.domain.common.GroupId
import ru.hryasch.coachnotes.domain.person.data.Person

interface PersonView: MvpView
{
    @StateStrategyType(SingleStateStrategy::class)
    fun setPersonData(person: Person, groupNames: Map<GroupId, String>)

    @StateStrategyType(SingleStateStrategy::class)
    fun loadingState()
}