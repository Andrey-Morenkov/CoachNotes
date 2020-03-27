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

    // Common params
    @Required
    var name: String? = null
    @Required
    var surname: String? = null
    var patronymic: String? = null
    @Required
    var birthday: String? = null
    var groupId: GroupId? = null
    var isPaid: Boolean = false

    // Parents params
    var parentFullName: String? = null
    var parentPhone: String? = null

    constructor(id: PersonId, name: String, surname: String, birthday: String): this()
    {
        this.id = id
        this.name = name
        this.surname = surname
        this.birthday = birthday
    }
}