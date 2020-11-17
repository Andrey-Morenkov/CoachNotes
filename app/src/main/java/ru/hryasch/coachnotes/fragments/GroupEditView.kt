package ru.hryasch.coachnotes.fragments

import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.OneExecutionStateStrategy
import moxy.viewstate.strategy.SingleStateStrategy
import moxy.viewstate.strategy.StateStrategyType
import ru.hryasch.coachnotes.domain.group.data.Group

interface GroupEditView: MvpView
{
    @StateStrategyType(SingleStateStrategy::class)
    fun setGroupData(group: Group)

    @StateStrategyType(SingleStateStrategy::class)
    fun loadingState()

    @StateStrategyType(SingleStateStrategy::class)
    fun deleteGroupFinished()

    @StateStrategyType(SingleStateStrategy::class)
    fun updateOrCreateGroupFinished()

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun similarGroupFound(existedGroup: Group)
}