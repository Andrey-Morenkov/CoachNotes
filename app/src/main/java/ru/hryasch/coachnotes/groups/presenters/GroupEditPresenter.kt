package ru.hryasch.coachnotes.groups.presenters

import ru.hryasch.coachnotes.domain.group.data.Group

interface GroupEditPresenter
{
    suspend fun applyGroupData(group: Group?)

    fun updateOrCreateGroup()

    fun onDeleteGroupClicked()

    fun deleteGroup(group: Group)
}