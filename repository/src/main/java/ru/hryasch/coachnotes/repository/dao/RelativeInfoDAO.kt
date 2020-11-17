package ru.hryasch.coachnotes.repository.dao

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.Index
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required

open class RelativeInfoDAO(): RealmObject()
{
    @Required
    var name: String? = null

    @Required
    var type: String? = null

    @Required
    var phones: RealmList<String> = RealmList()

    constructor(name: String, type: String): this()
    {
        this.name = name
        this.type = type
    }
}