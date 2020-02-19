package ru.hryasch.coachnotes.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import ru.hryasch.coachnotes.R

class MainActivity : AppCompatActivity()
{
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}
