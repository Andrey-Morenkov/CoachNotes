package ru.hryasch.coachnotes.application

import android.app.Application
import io.realm.Realm
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import ru.hryasch.coachnotes.di.journalAppModule
import ru.hryasch.coachnotes.di.realmRepositoriesModule
import ru.hryasch.coachnotes.di.journalRepositoryModule
import ru.hryasch.coachnotes.di.toolsModule

class App : Application()
{
    companion object
    {
        private lateinit var instance: App
        fun getCtx() = instance.applicationContext!!
    }

    init
    {
        instance = this
    }

    override fun onCreate()
    {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@App)
            modules(journalAppModule,
                    journalRepositoryModule,
                    realmRepositoriesModule,
                    toolsModule)
        }

        Realm.init(applicationContext)
    }
}