package ru.hryasch.coachnotes.application

import android.app.Application
import android.content.Context
import com.mooveit.library.Fakeit
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
        fun getContext(): Context = App.getContext()
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

        //Fakeit.initWithLocale("ru")

        Realm.init(applicationContext)
    }
}