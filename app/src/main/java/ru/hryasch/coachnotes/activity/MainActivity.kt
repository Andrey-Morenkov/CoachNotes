package ru.hryasch.coachnotes.activity

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.Navigation
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.koin.core.KoinComponent
import ru.hryasch.coachnotes.R
import ru.hryasch.coachnotes.application.App

class MainActivity: AppCompatActivity(), KoinComponent
{
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        App.onActivityCreate()

        setContentView(R.layout.activity_main)

        navController = Navigation.findNavController(this, R.id.nav_host_fragment)

        supportActionBar!!.hide()
        supportActionBar!!.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(this, R.color.background)))
    }

    @ExperimentalCoroutinesApi
    override fun onDestroy()
    {
        App.onActivityDestroy()
        super.onDestroy()
    }
}
