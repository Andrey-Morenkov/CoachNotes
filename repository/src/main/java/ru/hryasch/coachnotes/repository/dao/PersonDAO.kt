package ru.hryasch.coachnotes.repository.dao

import io.realm.RealmObject
import io.realm.annotations.Index
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required
import ru.hryasch.coachnotes.repository.common.GroupId
import ru.hryasch.coachnotes.repository.common.PersonId

open class PersonDAO(): RealmObject()
{
    @Required
    @Index
    @PrimaryKey
    var id: PersonId? = null

    @Required
    var groupId: GroupId? = null
    @Required
    var name: String? = null
    @Required
    var surname: String? = null

    constructor(id: PersonId, groupId: GroupId, name: String, surname: String): this()
    {
        this.id = id
        this.groupId = groupId
        this.name = name
        this.surname = surname
    }
}