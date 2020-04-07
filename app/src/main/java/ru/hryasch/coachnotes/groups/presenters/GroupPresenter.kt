package ru.hryasch.coachnotes.groups.presenters

import ru.hryasch.coachnotes.domain.common.PersonId
import ru.hryasch.coachnotes.domain.group.data.Group
import ru.hryasch.coachnotes.domain.person.data.Person

interface GroupPresenter
{
    suspend fun applyGroupData(group: Group?)

    fun onDeletePersonFromCurrentGroupClicked(person: Person)

    fun deletePersonFromCurrentGroup(personId: PersonId)

    fun onAddPeopleToGroupClicked()

    fun addPeopleToGroup(people: List<Person>)
}