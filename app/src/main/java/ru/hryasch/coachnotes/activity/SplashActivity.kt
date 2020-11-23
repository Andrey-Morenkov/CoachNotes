package ru.hryasch.coachnotes.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.pawegio.kandroid.e
import com.pawegio.kandroid.fromApi
import com.pawegio.kandroid.toApi
import com.pawegio.kandroid.toast
import ru.hryasch.coachnotes.repository.global.GlobalSettings


class SplashActivity: AppCompatActivity()
{
    private var pleaseTurnOnNetwork: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        continueInitializing()
    }

    override fun onDestroy()
    {
        pleaseTurnOnNetwork?.cancel()
        pleaseTurnOnNetwork = null
        super.onDestroy()
    }

    private fun continueInitializing(force: Boolean = false)
    {
        if (!force && !isNetworkOn())
        {
            showTurnOnDialog()
            return
        }

        if (GlobalSettings.General.isFirstLaunch())
        {
            FirebaseCrashlytics.getInstance().setUserId(GlobalSettings.General.generateAnalyticUserId())
        }

        val userFIO = GlobalSettings.Coach.getFullName()
        if (userFIO == null)
        {
            startActivity(Intent(this, LoginActivity::class.java))
        }
        else
        {
            FirebaseCrashlytics.getInstance().log("${userFIO.surname} ${userFIO.name} ${userFIO.patronymic}")
            startActivity(Intent(this, MainActivity::class.java))
        }

        finish()
    }

    private fun showTurnOnDialog()
    {
        pleaseTurnOnNetwork = MaterialAlertDialogBuilder(this)
            .setTitle("Внимание!")
            .setCancelable(false)
            .setMessage("Сообщение от разработчика:\nПожалуйста, включите интернет на вашем телефоне (мобильный интернет или WiFi) перед использованием данного приложения и не выключайте его во время использования. Это нужно чтобы я смог удаленно собрать информацию об использовании приложения, если что-то пойдет не так.\nСпасибо")
            .setPositiveButton("OK") { _, _ ->
                if (!isNetworkOn())
                {
                    toast("Вы же не включили интернет :)")
                }
                continueInitializing()
            }
            .setNegativeButton("У меня нет интернета") { _, _ ->
                continueInitializing(true)
            }
            .create()

        pleaseTurnOnNetwork!!.show()
    }

    @SuppressLint("NewApi")
    private fun isNetworkOn(): Boolean
    {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        fromApi(Build.VERSION_CODES.M, true) {
            val networkCapabilities = connectivityManager.activeNetwork ?: return false
            val activeNetwork = connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false
            return@isNetworkOn when {
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)     -> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
        }

        toApi(Build.VERSION_CODES.M, false)
        {
            connectivityManager.run {
                activeNetworkInfo?.run {
                    return@isNetworkOn when (type) {
                        ConnectivityManager.TYPE_WIFI -> true
                        ConnectivityManager.TYPE_MOBILE -> true
                        ConnectivityManager.TYPE_ETHERNET -> true
                        else -> false
                    }
                }
            }
        }

        return false
    }
}