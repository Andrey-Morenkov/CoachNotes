package ru.hryasch.coachnotes.activity

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.pawegio.kandroid.toast
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.koin.core.KoinComponent
import ru.hryasch.coachnotes.R
import ru.hryasch.coachnotes.application.App
import ru.hryasch.coachnotes.fragments.impl.MainFragment

class MainActivity: AppCompatActivity(), KoinComponent
{
    private var exitMillis: Long = 0

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        App.onActivityCreate()

        setContentView(R.layout.activity_main)

        supportActionBar!!.hide()
        supportActionBar!!.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(this, R.color.background)))

        if (savedInstanceState == null)
        {
            supportFragmentManager.beginTransaction()
                .add(R.id.mainFragmentSpace, MainFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    override fun onBackPressed()
    {
        if (supportFragmentManager.backStackEntryCount > 1)
        {
            supportFragmentManager.popBackStack()
        }
        else
        {
            generalOnBackPressed()
        }
    }

    @ExperimentalCoroutinesApi
    override fun onDestroy()
    {
        App.onActivityDestroy()
        super.onDestroy()
    }


    private fun generalOnBackPressed()
    {
        if ((System.currentTimeMillis() - exitMillis) > 2000)
        {
            toast("Нажмите еще раз для выхода из приложения")
            exitMillis = System.currentTimeMillis()
        }
        else
        {
            finish()
        }
    }
}
