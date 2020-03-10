package ru.hryasch.coachnotes.domain.person

import ru.hryasch.coachnotes.domain.common.GroupId
import ru.hryasch.coachnotes.domain.common.PersonId

data class PersonImpl(override var surname: String,
                      override var name: String,
                      override val id: PersonId? = null,
                      override var groupId: GroupId? = null) : Person
{

}