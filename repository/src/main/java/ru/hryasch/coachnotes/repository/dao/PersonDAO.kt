package ru.hryasch.coachnotes.repository.dao

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import ru.hryasch.coachnotes.repository.common.GroupId
import ru.hryasch.coachnotes.repository.common.PersonId

open class PersonDAO(): RealmObject()
{
    @PrimaryKey
    var id: PersonId = -1
    var firstName: String? = null
    var secondName: String? = null
    var groupId: GroupId? = null
}