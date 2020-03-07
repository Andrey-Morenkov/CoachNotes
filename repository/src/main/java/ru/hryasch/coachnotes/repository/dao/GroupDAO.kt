package ru.hryasch.coachnotes.repository.dao

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import ru.hryasch.coachnotes.repository.common.GroupId
import ru.hryasch.coachnotes.repository.common.PersonId

open class GroupDAO(): RealmObject()
{
    @PrimaryKey
    var id: GroupId = 0
    var people: RealmList<PersonId> = RealmList()
}