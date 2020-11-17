package ru.hryasch.coachnotes.repository.dao

import io.realm.RealmList
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

    // Required params
    @Required
    var name: String? = null
    @Required
    var surname: String? = null
    @Required
    var birthdayYear: Int? = null

    // Optional params
    var patronymic: String? = null
    var fullBirthday: String? = null
    var groupId: GroupId? = null
    var isPaid: Boolean = false
    var relativeInfos: RealmList<RelativeInfoDAO> = RealmList()

    // History info
    //val groupsHistory: RealmList<PersonGroupHistoryInfoElement> = RealmList()

    constructor(id: PersonId, name: String, surname: String, birthdayYear: Int): this()
    {
        this.id = id
        this.name = name
        this.surname = surname
        this.birthdayYear = birthdayYear
    }

    fun delete(): DeletedPersonDAO
    {
        return DeletedPersonDAO(this, System.currentTimeMillis())
    }
}

open class DeletedPersonDAO(): RealmObject()
{
    @Required
    @Index
    @PrimaryKey
    var id: PersonId? = null

    @Required
    var deleteTimestamp: Long? = null

    // Common params
    @Required
    var name: String? = null
    @Required
    var surname: String? = null
    @Required
    var birthdayYear: Int? = null

    // Optional params
    var patronymic: String? = null
    var fullBirthday: String? = null
    var groupId: GroupId? = null
    var isPaid: Boolean = false
    var relativeInfos: RealmList<RelativeInfoDAO> = RealmList()

    // History info
    //var groupsHistory: RealmList<PersonGroupHistoryInfoElement> = RealmList()

    constructor(personDAO: PersonDAO, timestamp: Long): this()
    {
        id = personDAO.id
        deleteTimestamp = timestamp
        name = personDAO.name
        surname = personDAO.surname
        birthdayYear = personDAO.birthdayYear
        patronymic = personDAO.patronymic
        fullBirthday = personDAO.fullBirthday
        groupId = personDAO.groupId
        isPaid = personDAO.isPaid
        relativeInfos.addAll(personDAO.relativeInfos)
        //groupsHistory.addAll(personDAO.groupsHistory)
    }

    fun revive(): PersonDAO
    {
        return PersonDAO(id!!, name!!, surname!!, birthdayYear!!)
            .apply {
                patronymic = this@DeletedPersonDAO.patronymic
                fullBirthday = this@DeletedPersonDAO.fullBirthday
                groupId = this@DeletedPersonDAO.groupId
                isPaid = this@DeletedPersonDAO.isPaid
                relativeInfos.addAll(this@DeletedPersonDAO.relativeInfos)
                //groupsHistory.addAll(this@DeletedPersonDAO.groupsHistory)
            }
    }
}