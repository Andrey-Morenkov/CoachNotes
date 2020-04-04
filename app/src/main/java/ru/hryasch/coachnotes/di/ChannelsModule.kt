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

@ExperimentalCoroutinesApi
val channelsModule = module {

    single(named("sendGroupsList"))  { GroupChannelsStorage.allGroupsListChannel as ConflatedBroadcastChannel<List<Group>> }
    factory(named("recvGroupsList")) { get<ConflatedBroadcastChannel<List<Group>>>(named("sendGroupsList")).openSubscription() as ReceiveChannel<List<Group>> }

    factory(named("sendSpecificGroup")) { (groupId: GroupId) ->
        var groupChannel = GroupChannelsStorage.groupChannelById[groupId]
        if (groupChannel == null)
        {
            groupChannel = ConflatedBroadcastChannel<Group>()
            GroupChannelsStorage.groupChannelById[groupId] = groupChannel
        }

        return@factory groupChannel as ConflatedBroadcastChannel<Group>
    }
    factory (named("recvSpecificGroup")) { (groupId: GroupId) ->
        get<ConflatedBroadcastChannel<Group>>(named("sendSpecificGroup")) { parametersOf(groupId) }.openSubscription() as ReceiveChannel<Group>
    }



    single(named("sendPeopleList"))  { PeopleChannelsStorage.allPeopleListChannel as ConflatedBroadcastChannel<List<Person>>}
    factory(named("recvPeopleList")) { get<ConflatedBroadcastChannel<List<Person>>>(named("sendPeopleList")).openSubscription() as ReceiveChannel<List<Person>>}

    factory(named("sendSpecificPerson")) { (personId: PersonId) ->
        var personChannel = PeopleChannelsStorage.personChannelById[personId]
        if (personChannel == null)
        {
            personChannel = ConflatedBroadcastChannel<Person>()
            PeopleChannelsStorage.personChannelById[personId] = personChannel
        }

        return@factory personChannel as ConflatedBroadcastChannel<Person>
    }
    factory(named("recvSpecificPerson")) { (personId: PersonId) ->
        get<ConflatedBroadcastChannel<Person>>(named("sendSpecificPerson")) { parametersOf(personId)}.openSubscription() as ReceiveChannel<Person>
    }

    factory(named("sendPeopleByGroup")) { (groupId: GroupId) ->
        var personsByGroupChannel = PeopleChannelsStorage.groupPeopleListChannelByGroupId[groupId]
        if (personsByGroupChannel == null)
        {
            personsByGroupChannel = ConflatedBroadcastChannel<List<Person>>()
            PeopleChannelsStorage.groupPeopleListChannelByGroupId[groupId] = personsByGroupChannel
        }

        return@factory personsByGroupChannel as ConflatedBroadcastChannel<List<Person>>
    }
    factory(named("recvPeopleByGroup")) { (groupId: GroupId) ->
        get<ConflatedBroadcastChannel<List<Person>>>(named("sendPeopleByGroup")) { parametersOf(groupId)}.openSubscription() as ReceiveChannel<List<Person>>
    }
}

object GroupChannelsStorage
{
    @ExperimentalCoroutinesApi
    val allGroupsListChannel = ConflatedBroadcastChannel<List<Group>>()

    @ExperimentalCoroutinesApi
    val groupChannelById: MutableMap<GroupId, ConflatedBroadcastChannel<Group>> = HashMap()
}

object PeopleChannelsStorage
{
    @ExperimentalCoroutinesApi
    val allPeopleListChannel = ConflatedBroadcastChannel<List<Person>>()

    @ExperimentalCoroutinesApi
    val personChannelById: MutableMap<PersonId, ConflatedBroadcastChannel<Person>> = HashMap()

    @ExperimentalCoroutinesApi
    val groupPeopleListChannelByGroupId: MutableMap<GroupId, ConflatedBroadcastChannel<List<Person>>> = HashMap()
}