package ru.hryasch.coachnotes.application

import android.app.Application
import io.realm.Realm
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import ru.hryasch.coachnotes.di.*

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
            modules(appModule,
                    journalModule,
                    journalRepositoryModule,
                    realmRepositoriesModule,
                    groupsModule,
                    toolsModule)
        }

        Realm.init(applicationContext)
    }
}