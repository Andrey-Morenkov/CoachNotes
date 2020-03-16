package ru.hryasch.coachnotes.repository.dao

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.Index
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required
import ru.hryasch.coachnotes.repository.common.GroupId
import ru.hryasch.coachnotes.repository.common.PersonId

open class GroupDAO(): RealmObject()
{
    @Required
    @Index
    @PrimaryKey
    var id: GroupId? = null

    @Required
    var name: String? = null
    var availableAge: Int? = null
    var members: RealmList<PersonId> = RealmList()

    constructor(id: GroupId, name: String, availableAge: Int): this()
    {
        this.id = id
        this.name = name
        this.availableAge = availableAge
    }
}