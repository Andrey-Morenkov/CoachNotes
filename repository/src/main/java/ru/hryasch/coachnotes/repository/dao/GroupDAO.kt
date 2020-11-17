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

    // Required params
    @Required
    var name: String? = null

    // Optional params
    var availableAgeLow: AbsoluteAge? = null
    var availableAgeHigh: AbsoluteAge? = null
    var isPaid: Boolean = false
    var members: RealmList<PersonId> = RealmList()
    var scheduleDays: RealmList<ScheduleDayDAO> = RealmList()
    var scheduleDaysCode0: String = ""

    // History info
    //val historicMembers: RealmList<PersonId> = RealmList()

    constructor(id: GroupId,
                name: String,
                isPaid: Boolean = false,
                availableAgeLow: AbsoluteAge? = null,
                availableAgeHigh: AbsoluteAge? = null): this()
    {
        this.id = id
        this.name = name
        this.availableAgeLow = availableAgeLow
        this.availableAgeHigh = availableAgeHigh
        this.isPaid = isPaid
    }

    fun delete(): DeletedGroupDAO
    {
        return DeletedGroupDAO(this, System.currentTimeMillis())
    }

    override fun toString(): String
    {
        var scheduleDaysString = ""
        for (scheduleDay in scheduleDays)
        {
            scheduleDaysString += scheduleDay.toString()
        }

        return "Group[$id]:$name scheduleDays: $scheduleDaysString"
    }
}

open class DeletedGroupDAO(): RealmObject()
{
    @Required
    @Index
    @PrimaryKey
    var id: GroupId? = null

    @Required
    var name: String? = null

    @Required
    var deleteTimestamp: Long? = null

    var availableAgeLow: AbsoluteAge? = null
    var availableAgeHigh: AbsoluteAge? = null
    var isPaid: Boolean = false
    var members: RealmList<PersonId> = RealmList()
    var scheduleDays: RealmList<ScheduleDayDAO> = RealmList()
    var scheduleDaysCode0: String = ""

    // History info
    //var historicMembers: RealmList<PersonId> = RealmList()

    constructor(groupDAO: GroupDAO, timestamp: Long): this()
    {
        id = groupDAO.id
        name = groupDAO.name
        deleteTimestamp = timestamp
        availableAgeLow = groupDAO.availableAgeLow
        availableAgeHigh = groupDAO.availableAgeHigh
        isPaid = groupDAO.isPaid
        members.addAll(groupDAO.members)
        scheduleDays.addAll(groupDAO.scheduleDays)
        scheduleDaysCode0 = groupDAO.scheduleDaysCode0
        //historicMembers.addAll(groupDAO.historicMembers)
    }

    fun revive(): GroupDAO
    {
        return GroupDAO(id!!, name!!, isPaid, availableAgeLow!!, availableAgeHigh)
            .apply {
                members.addAll(this@DeletedGroupDAO.members)
                scheduleDays.addAll(this@DeletedGroupDAO.scheduleDays)
                scheduleDaysCode0 = this@DeletedGroupDAO.scheduleDaysCode0
                //historicMembers.addAll(this@DeletedGroupDAO.historicMembers)
            }
    }
}