package ru.hryasch.coachnotes.application

import android.app.Application
import com.mooveit.library.Fakeit
import io.realm.Realm
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import ru.hryasch.coachnotes.di.journalAppModule
import ru.hryasch.coachnotes.di.realmRepositoriesModule
import ru.hryasch.coachnotes.di.journalRepositoryModule

class App : Application()
{
    override fun onCreate()
    {
        super.onCreate()

        startKoin{
            androidLogger()
            androidContext(this@App)
            modules(journalAppModule,
                journalRepositoryModule, realmRepositoriesModule)
        }

        Fakeit.initWithLocale("ru")

        Realm.init(applicationContext)
    }
}