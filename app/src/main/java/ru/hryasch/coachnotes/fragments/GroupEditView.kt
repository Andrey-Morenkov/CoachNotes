package ru.hryasch.coachnotes.fragments

import moxy.MvpView
import moxy.viewstate.strategy.SingleStateStrategy
import moxy.viewstate.strategy.StateStrategyType
import ru.hryasch.coachnotes.domain.group.data.Group

interface GroupEditView: MvpView
{
    @StateStrategyType(SingleStateStrategy::class)
    fun setGroupData(group: Group)

    @StateStrategyType(SingleStateStrategy::class)
    fun loadingState()
}