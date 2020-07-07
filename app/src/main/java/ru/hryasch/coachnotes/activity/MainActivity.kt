package ru.hryasch.coachnotes.activity

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Menu
import android.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.pawegio.kandroid.e
import com.pawegio.kandroid.visible
import io.realm.Realm

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import me.ibrahimsn.lib.SmoothBottomBar
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.koin.core.qualifier.named

import ru.hryasch.coachnotes.R
import ru.hryasch.coachnotes.application.App
import ru.hryasch.coachnotes.domain.repository.GroupRepository
import ru.hryasch.coachnotes.domain.repository.JournalRepository
import ru.hryasch.coachnotes.domain.repository.PersonRepository

class MainActivity : AppCompatActivity(), KoinComponent
{
    lateinit var navController: NavController
    lateinit var bottomNavBar: SmoothBottomBar

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        App.onActivityCreate()

        setContentView(R.layout.activity_main)

        System.setProperty("org.apache.poi.javax.xml.stream.XMLInputFactory",  "com.fasterxml.aalto.stax.InputFactoryImpl")
        System.setProperty("org.apache.poi.javax.xml.stream.XMLOutputFactory", "com.fasterxml.aalto.stax.OutputFactoryImpl")
        System.setProperty("org.apache.poi.javax.xml.stream.XMLEventFactory",  "com.fasterxml.aalto.stax.EventFactoryImpl")

        navController = Navigation.findNavController(this, R.id.nav_host_fragment)
        bottomNavBar = findViewById(R.id.bottomNavBar)
        supportActionBar!!.hide()
        supportActionBar!!.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(this, R.color.background)))
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.bottom_nav_menu, menu)
        bottomNavBar.setupWithNavController(menu!!, navController)
        return true
    }

    @ExperimentalCoroutinesApi
    override fun onDestroy()
    {
        App.onActivityDestroy()

        super.onDestroy()
    }

    fun hideBottomNavigation()
    {
        bottomNavBar.visible = false
    }

    fun showBottomNavigation()
    {
        bottomNavBar.visible = true
    }
}
