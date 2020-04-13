package ru.hryasch.coachnotes.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.Navigation
import io.realm.Realm
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.koin.core.qualifier.named
import ru.hryasch.coachnotes.R
import ru.hryasch.coachnotes.domain.repository.GroupRepository
import ru.hryasch.coachnotes.domain.repository.JournalRepository
import ru.hryasch.coachnotes.domain.repository.PersonRepository
import ru.hryasch.coachnotes.fragments.impl.JournalGroupFragment

class MainActivity : AppCompatActivity(), KoinComponent
{
    lateinit var navController: NavController

    private val groupRepository: GroupRepository by inject(named("release"))
    private val peopleRepository: PersonRepository by inject(named("release"))
    private val journalRepository: JournalRepository by inject(named("release"))

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        System.setProperty("org.apache.poi.javax.xml.stream.XMLInputFactory",  "com.fasterxml.aalto.stax.InputFactoryImpl")
        System.setProperty("org.apache.poi.javax.xml.stream.XMLOutputFactory", "com.fasterxml.aalto.stax.OutputFactoryImpl")
        System.setProperty("org.apache.poi.javax.xml.stream.XMLEventFactory",  "com.fasterxml.aalto.stax.EventFactoryImpl")

        navController = Navigation.findNavController(this, R.id.nav_host_fragment)
    }

    override fun onDestroy()
    {
        GlobalScope.launch(Dispatchers.Default)
        {
            groupRepository.closeDb()
            peopleRepository.closeDb()
            journalRepository.closeDb()
        }

        super.onDestroy()
    }
}
