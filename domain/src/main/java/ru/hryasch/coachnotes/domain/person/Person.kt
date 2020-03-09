package ru.hryasch.coachnotes.domain.person

import ru.hryasch.coachnotes.domain.common.PersonId

interface Person
{
    val id: PersonId
    var name: String
    var surname: String
}