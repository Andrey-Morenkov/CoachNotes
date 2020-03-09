package ru.hryasch.coachnotes.domain.person

import ru.hryasch.coachnotes.domain.common.PersonId

class PersonImpl(override val id: PersonId,
                 override var name: String,
                 override var surname: String) : Person
{

}