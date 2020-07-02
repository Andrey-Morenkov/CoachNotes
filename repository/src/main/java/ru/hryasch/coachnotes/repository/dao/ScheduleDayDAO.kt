package ru.hryasch.coachnotes.repository.dao

import io.realm.RealmObject
import io.realm.annotations.Index
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required

open class ScheduleDayDAO(): RealmObject()
{
    @Required
    @Index
    @PrimaryKey
    var name: String? = null

    @Required
    var position0: Int? = null

    @Required
    var startTime: String? = null

    @Required
    var finishTime: String? = null

    constructor(name: String, position: Int): this()
    {
        this.name = name
        this.position0 = position
    }
}