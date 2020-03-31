package ru.hryasch.coachnotes.groups.presenters

import ru.hryasch.coachnotes.domain.group.data.Group

interface GroupPresenter
{
    suspend fun applyGroupData(group: Group?)
    fun updateOrCreateGroup()
}