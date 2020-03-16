package ru.hryasch.coachnotes.di

import org.koin.core.qualifier.named
import org.koin.dsl.module
import ru.hryasch.coachnotes.domain.repository.GroupRepository
import ru.hryasch.coachnotes.domain.repository.JournalRepository
import ru.hryasch.coachnotes.domain.repository.PersonRepository
import ru.hryasch.coachnotes.repository.group.GroupFakeRepositoryImpl
import ru.hryasch.coachnotes.repository.journal.JournalFakeRepositoryImpl
import ru.hryasch.coachnotes.repository.person.PersonFakeRepositoryImpl

val journalRepositoryModule = module {

    single(named("mock")) { JournalFakeRepositoryImpl() as JournalRepository }
    single(named("mock")) { PersonFakeRepositoryImpl() as PersonRepository }
    single(named("mock")) { GroupFakeRepositoryImpl()  as GroupRepository }
}