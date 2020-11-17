package ru.hryasch.coachnotes.fragments

import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.OneExecutionStateStrategy
import moxy.viewstate.strategy.SingleStateStrategy
import moxy.viewstate.strategy.StateStrategyType
import ru.hryasch.coachnotes.domain.group.data.Group
import ru.hryasch.coachnotes.domain.person.data.Person

interface PersonEditView: MvpView
{
    @StateStrategyType(SingleStateStrategy::class)
    fun setPersonData(person: Person, groups: List<Group>)

    @StateStrategyType(SingleStateStrategy::class)
    fun loadingState()

    @StateStrategyType(SingleStateStrategy::class)
    fun deletePersonFinished()

    @StateStrategyType(SingleStateStrategy::class)
    fun updateOrCreatePersonFinished()

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun similarPersonFound(existedPerson: Person)
}