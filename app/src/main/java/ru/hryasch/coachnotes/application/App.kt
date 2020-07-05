package ru.hryasch.coachnotes.application

import android.app.Application
import com.pawegio.kandroid.e
import io.realm.Realm
import kotlinx.coroutines.*
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import ru.hryasch.coachnotes.di.*
import ru.hryasch.coachnotes.domain.repository.GroupRepository
import ru.hryasch.coachnotes.domain.repository.JournalRepository
import ru.hryasch.coachnotes.domain.repository.PersonRepository
import ru.hryasch.coachnotes.repository.group.GroupRepositoryImpl
import ru.hryasch.coachnotes.repository.journal.JournalRepositoryImpl
import ru.hryasch.coachnotes.repository.person.PersonRepositoryImpl

class App : Application()
{
    companion object
    {
        private lateinit var instance: App
        fun getCtx() = instance.applicationContext!!


        @ExperimentalCoroutinesApi
        private lateinit var groupRepository: GroupRepository
        @ExperimentalCoroutinesApi
        private lateinit var peopleRepository: PersonRepository
        private lateinit var journalRepository: JournalRepository
        private lateinit var destroyRealmJob: Job
        @ExperimentalCoroutinesApi
        fun onActivityDestroy()
        {
            destroyRealmJob = GlobalScope.launch(Dispatchers.IO)
            {
                delay(2000L)
                GlobalScope.launch(Dispatchers.IO)
                {
                    e("destroy realm")
                    groupRepository.closeDb()
                    peopleRepository.closeDb()
                    journalRepository.closeDb()
                }
            }
        }
        fun onActivityCreate()
        {
            if (::destroyRealmJob.isInitialized && destroyRealmJob.isActive)
            {
                destroyRealmJob.cancel()
                e("destroy realm prevented")
            }
        }
    }

    init
    {
        instance = this
    }

    @ExperimentalCoroutinesApi
    override fun onCreate()
    {
        super.onCreate()

        Realm.init(applicationContext)

        startKoin {
            androidLogger()
            androidContext(this@App)
            modules(appModule,
                    journalModule,
                    realmRepositoriesModule,
                    groupsModule,
                    peopleModule,
                    toolsModule,
                    channelsModule)
        }

        groupRepository = GroupRepositoryImpl()
        peopleRepository = PersonRepositoryImpl()
        journalRepository = JournalRepositoryImpl()
    }


}