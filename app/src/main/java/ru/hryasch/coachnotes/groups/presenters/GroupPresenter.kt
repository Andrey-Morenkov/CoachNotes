package ru.hryasch.coachnotes.groups.presenters

import ru.hryasch.coachnotes.domain.common.PersonId
import ru.hryasch.coachnotes.domain.group.data.Group
import ru.hryasch.coachnotes.domain.person.data.Person

interface GroupPresenter
{
    fun applyGroupDataAsync(group: Group?)

    fun onDeletePersonFromCurrentGroupClicked(person: Person)

    fun deletePersonFromCurrentGroup(personId: PersonId)

    fun onAddPeopleToGroupClicked()

    fun addPeopleToNewGroup(people: List<Person>)
}