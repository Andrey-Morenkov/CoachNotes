package ru.hryasch.coachnotes.di

import io.realm.Realm
import io.realm.RealmConfiguration
import org.koin.core.qualifier.named
import org.koin.dsl.module

val realmRepositoriesModule = module {

    single(named("persons_mock"))
    {
        val config = RealmConfiguration.Builder()
            .name("persons_fake")
            .inMemory()
            .build()
        return@single Realm.getInstance(config)
    }

    single(named("groups_mock"))
    {
        val config = RealmConfiguration.Builder()
            .name("groups_fake")
            .inMemory()
            .build()
        return@single Realm.getInstance(config)
    }

    single(named("journal_storage_mock"))
    {
        val config = RealmConfiguration.Builder()
            .name("journal_storage_fake")
            .inMemory()
            .build()
        return@single Realm.getInstance(config)
    }
}