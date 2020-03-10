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

    var people: RealmList<PersonId> = RealmList()
}