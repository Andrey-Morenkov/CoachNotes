package ru.hryasch.coachnotes.groups.presenters

import ru.hryasch.coachnotes.domain.group.data.Group

interface GroupEditPresenter: GroupPresenter
{
    fun updateOrCreateGroup()

    fun deleteGroup(group: Group)
}