package ru.hryasch.coachnotes.groups.presenters

import ru.hryasch.coachnotes.domain.group.data.Group

interface GroupEditPresenter
{
    fun applyInitialArgumentGroupAsync(group: Group?)

    fun applyGroupDataAsync(group: Group?)

    fun updateOrCreateGroup()

    fun onDeleteGroupClicked()

    fun deleteGroup(group: Group)
}