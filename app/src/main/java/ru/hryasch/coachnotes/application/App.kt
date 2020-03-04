package ru.hryasch.coachnotes.application

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import ru.hryasch.coachnotes.di.journalModule

class App : Application()
{
    override fun onCreate()
    {
        super.onCreate()

        startKoin{
            androidLogger()
            androidContext(this@App)
            modules(journalModule)
        }
    }
}