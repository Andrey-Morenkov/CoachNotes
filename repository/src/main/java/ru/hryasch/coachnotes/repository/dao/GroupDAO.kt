package ru.hryasch.coachnotes.repository.dao

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.Index
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required
import ru.hryasch.coachnotes.repository.common.AbsoluteAge
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
    var availableAgeLow: AbsoluteAge? = null
    var availableAgeHigh: AbsoluteAge? = null
    var isPaid: Boolean = false
    var members: RealmList<PersonId> = RealmList()
    var scheduleDays: RealmList<ScheduleDayDAO> = RealmList()
    var scheduleDaysCode0: String = ""

    constructor(id: GroupId,
                name: String,
                isPaid: Boolean = false,
                availableAgeLow: AbsoluteAge,
                availableAgeHigh: AbsoluteAge? = null): this()
    {
        this.id = id
        this.name = name
        this.availableAgeLow = availableAgeLow
        this.availableAgeHigh = availableAgeHigh
        this.isPaid = isPaid
    }
}