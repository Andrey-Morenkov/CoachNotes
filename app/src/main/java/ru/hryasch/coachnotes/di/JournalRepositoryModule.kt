package ru.hryasch.coachnotes.di

import org.koin.core.qualifier.named
import org.koin.dsl.module
import ru.hryasch.coachnotes.repository.journal.JournalRepository
import ru.hryasch.coachnotes.repository.journal.impl.JournalFakeRepository

val journalRepositoryModule = module {

    single(named("mock")) { JournalFakeRepository() as JournalRepository }
}