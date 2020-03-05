package ru.hryasch.coachnotes.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import ru.hryasch.coachnotes.R
import ru.hryasch.coachnotes.fragments.impl.HomeFragmentImpl
import ru.hryasch.coachnotes.fragments.impl.JournalGroupFragment

class MainActivity : FragmentActivity()
{
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (supportFragmentManager.fragments.isEmpty())
        {
            supportFragmentManager
                .beginTransaction()
                .add(R.id.home_container, JournalGroupFragment(), JournalGroupFragment::class.java.simpleName)
                .commit()
        }
    }
}
