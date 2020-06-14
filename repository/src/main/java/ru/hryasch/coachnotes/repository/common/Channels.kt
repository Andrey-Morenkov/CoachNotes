package ru.hryasch.coachnotes.repository.common

import io.realm.Realm
import io.realm.RealmResults
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import ru.hryasch.coachnotes.domain.common.GroupId
import ru.hryasch.coachnotes.domain.common.PersonId
import ru.hryasch.coachnotes.repository.dao.GroupDAO
import ru.hryasch.coachnotes.repository.dao.PersonDAO

object GroupChannelsStorage
{
    val allGroups = StorageCellResults<GroupDAO>()
    val groupById: MutableMap<GroupId, StorageCellSingle<GroupDAO>> = HashMap()
}

object PeopleChannelsStorage
{
    val allPeople = StorageCellResults<PersonDAO>()
    val personById: MutableMap<PersonId, StorageCellSingle<PersonDAO>> = HashMap()
    val groupPeopleByGroupId: MutableMap<GroupId, StorageCellResults<PersonDAO>> = HashMap()
}

class StorageCellResults<T>
{
    var observable: RealmResults<T>? = null
    var mainDbEntity: Realm? = null
    @ExperimentalCoroutinesApi
    val channel = ConflatedBroadcastChannel<T>()
}

class StorageCellSingle<T>
{
    var observable: T? = null
    var mainDbEntity: Realm? = null
    @ExperimentalCoroutinesApi
    var channel = ConflatedBroadcastChannel<T>()
}