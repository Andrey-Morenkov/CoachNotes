package ru.hryasch.coachnotes.activity

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.koin.core.KoinComponent
import ru.hryasch.coachnotes.R
import ru.hryasch.coachnotes.application.App
import ru.hryasch.coachnotes.domain.group.data.Group
import ru.hryasch.coachnotes.domain.person.data.Person
import ru.hryasch.coachnotes.fragments.impl.MainFragmentDirections

class MainActivity: AppCompatActivity(), KoinComponent
{
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        App.onActivityCreate()

        firebaseAnalytics = Firebase.analytics

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

    fun navigateToGroupInfoFragment(group: Group)
    {
        val action = MainFragmentDirections.actionMainFragmentToGroupInfoFragment(group)
        navController.navigate(action)
    }

    fun navigateToGroupEditFragment(group: Group?)
    {
        val action = MainFragmentDirections.actionMainFragmentToGroupEditFragment(group)
        navController.navigate(action)
    }

    fun navigateToJournalFragment(group: Group)
    {
        val action = MainFragmentDirections.actionMainFragmentToJournalGroupFragment(group)
        navController.navigate(action)
    }

    fun navigateToPersonInfoFragment(person: Person)
    {
        val action = MainFragmentDirections.actionMainFragmentToPersonInfoFragment(person)
        navController.navigate(action)
    }

    fun navigateToPersonEditFragment(person: Person?)
    {
        val action = MainFragmentDirections.actionMainFragmentToPersonEditFragment(person)
        navController.navigate(action)
    }
}
