package ru.hryasch.coachnotes.di

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.channels.ReceiveChannel
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
import org.koin.dsl.module
import ru.hryasch.coachnotes.domain.common.GroupId
import ru.hryasch.coachnotes.domain.common.PersonId
import ru.hryasch.coachnotes.domain.group.data.Group
import ru.hryasch.coachnotes.domain.person.data.Person
import ru.hryasch.coachnotes.repository.common.GroupChannelsStorage
import ru.hryasch.coachnotes.repository.common.PeopleChannelsStorage
import ru.hryasch.coachnotes.repository.common.StorageCellResults
import ru.hryasch.coachnotes.repository.common.StorageCellSingle
import ru.hryasch.coachnotes.repository.dao.GroupDAO
import ru.hryasch.coachnotes.repository.dao.PersonDAO

@ExperimentalCoroutinesApi
val channelsModule = module {

    // AllGroups
    single(named("sendGroupsList"))  { GroupChannelsStorage.allGroups.channel }
    factory(named("recvGroupsList")) { get<ConflatedBroadcastChannel<List<Group>>>(named("sendGroupsList")).openSubscription() as ReceiveChannel<List<Group>> }

    // Specific group
    factory(named("sendSpecificGroup")) { (groupId: GroupId) ->
        var groupEntry = GroupChannelsStorage.groupById[groupId]
        if (groupEntry == null)
        {
            GroupChannelsStorage.groupById[groupId] = StorageCellSingle<GroupDAO>()
            groupEntry = GroupChannelsStorage.groupById[groupId]
        }

        return@factory groupEntry!!.channel
    }
    factory (named("recvSpecificGroup")) { (groupId: GroupId) ->
        get<ConflatedBroadcastChannel<Group>>(named("sendSpecificGroup")) { parametersOf(groupId) }.openSubscription() as ReceiveChannel<Group>
    }



    //AllPeople
    single(named("sendPeopleList"))  { PeopleChannelsStorage.allPeople.channel }
    factory(named("recvPeopleList")) { get<ConflatedBroadcastChannel<List<Person>>>(named("sendPeopleList")).openSubscription() as ReceiveChannel<List<Person>>}

    //Specific person
    factory(named("sendSpecificPerson")) { (personId: PersonId) ->
        var personEntry = PeopleChannelsStorage.personById[personId]
        if (personEntry == null)
        {
            PeopleChannelsStorage.personById[personId] = StorageCellSingle<PersonDAO>()
            personEntry = PeopleChannelsStorage.personById[personId]
        }

        return@factory personEntry!!.channel
    }
    factory(named("recvSpecificPerson")) { (personId: PersonId) ->
        get<ConflatedBroadcastChannel<Person>>(named("sendSpecificPerson")) { parametersOf(personId)}.openSubscription() as ReceiveChannel<Person>
    }

    //People by group
    factory(named("sendPeopleByGroup")) { (groupId: GroupId) ->
        var personsByGroupEntry = PeopleChannelsStorage.groupPeopleByGroupId[groupId]
        if (personsByGroupEntry == null)
        {
            PeopleChannelsStorage.groupPeopleByGroupId[groupId] = StorageCellResults<PersonDAO>()
            personsByGroupEntry = PeopleChannelsStorage.groupPeopleByGroupId[groupId]
        }

        return@factory personsByGroupEntry!!.channel
    }
    factory(named("recvPeopleByGroup")) { (groupId: GroupId) ->
        get<ConflatedBroadcastChannel<List<Person>>>(named("sendPeopleByGroup")) { parametersOf(groupId)}.openSubscription() as ReceiveChannel<List<Person>>
    }

    single { GroupChannelsStorage }
    single { PeopleChannelsStorage }
}



