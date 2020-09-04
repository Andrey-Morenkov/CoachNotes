package ru.hryasch.coachnotes.repository.dao

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.Required
import ru.hryasch.coachnotes.repository.common.GroupId

open class PersonGroupHistoryInfoElement(): RealmObject()
{
    @Required
    var groupId: GroupId? = null
    var dates: RealmList<PersonGroupHistoryInfoDatesElement> = RealmList()

    constructor(groupId: GroupId): this()
    {
        this.groupId = groupId
    }
}

open class PersonGroupHistoryInfoDatesElement(): RealmObject()
{
    var startDate: String? = null
    var endDate: String? = null

    constructor(startDate: String): this()
    {
        this.startDate = startDate
    }
}