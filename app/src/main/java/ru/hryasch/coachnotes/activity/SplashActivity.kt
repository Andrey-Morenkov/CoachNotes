package ru.hryasch.coachnotes.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import ru.hryasch.coachnotes.repository.global.GlobalSettings


class SplashActivity: AppCompatActivity()
{
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        if (GlobalSettings.Coach.getFullName() == null)
        {
            startActivity(Intent(this, LoginActivity::class.java))
        }
        else
        {
            startActivity(Intent(this, MainActivity::class.java))
        }

        finish()
    }
}