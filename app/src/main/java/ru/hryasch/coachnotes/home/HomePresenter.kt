package ru.hryasch.coachnotes.home

import ru.hryasch.coachnotes.repository.global.GlobalSettings

interface HomePresenter
{
    fun changeCoachInfo(newCoachFullName: String,
                        newCoachRole: String)
}