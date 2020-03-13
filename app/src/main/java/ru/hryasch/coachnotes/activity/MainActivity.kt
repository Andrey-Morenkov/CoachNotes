package ru.hryasch.coachnotes.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import ru.hryasch.coachnotes.R
import ru.hryasch.coachnotes.fragments.impl.JournalGroupFragment

class MainActivity : AppCompatActivity()
{


    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        System.setProperty(
            "org.apache.poi.javax.xml.stream.XMLInputFactory",
            "com.fasterxml.aalto.stax.InputFactoryImpl"
        )

        System.setProperty(
            "org.apache.poi.javax.xml.stream.XMLOutputFactory",
            "com.fasterxml.aalto.stax.OutputFactoryImpl"
        )
        System.setProperty(
            "org.apache.poi.javax.xml.stream.XMLEventFactory",
            "com.fasterxml.aalto.stax.EventFactoryImpl"
        )

        if (supportFragmentManager.fragments.isEmpty())
        {
            supportFragmentManager
                .beginTransaction()
                .add(R.id.home_container, JournalGroupFragment(), JournalGroupFragment::class.java.simpleName)
                .commit()
        }
    }
}
