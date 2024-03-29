package ru.hryasch.coachnotes.di

import io.realm.RealmConfiguration
import org.koin.core.qualifier.named
import org.koin.dsl.module

val realmRepositoriesModule = module {

    single(named("persons_mock"))
    {
        RealmConfiguration.Builder()
            .name("persons_fake")
            .inMemory()
            .build()
    }

    single(named("persons"))
    {
        RealmConfiguration.Builder()
            .name("persons")
            .build()
    }

    single(named("groups_mock"))
    {
       RealmConfiguration.Builder()
            .name("groups_fake")
            .inMemory()
            .build()
    }

    single(named("groups"))
    {
        RealmConfiguration.Builder()
            .name("groups")
            .build()
    }

    single(named("journal_storage_mock"))
    {
        RealmConfiguration.Builder()
            .name("journal_storage_fake")
            .inMemory()
            .build()
    }

    single(named("journal_storage"))
    {
        RealmConfiguration.Builder()
            .name("journal_storage")
            .build()
    }
}