package ru.hryasch.coachnotes.di

import org.koin.core.qualifier.named
import org.koin.dsl.module
import ru.hryasch.coachnotes.domain.repository.JournalRepository
import ru.hryasch.coachnotes.repository.journal.JournalFakeRepository

val journalRepositoryModule = module {

    single(named("mock")) { JournalFakeRepository() as JournalRepository }
}