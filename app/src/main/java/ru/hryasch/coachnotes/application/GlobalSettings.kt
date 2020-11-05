package ru.hryasch.coachnotes.application

import android.content.Context

object GlobalSettings
{
    private const val NAME = "name"
    private const val ROLE = "role"

    private val sharedPreferences = App.getCtx().getSharedPreferences("GLOBAL_SETTINGS", Context.MODE_PRIVATE)
    private val sharedPreferencesEditor = sharedPreferences.edit()

    fun getName(): String? = sharedPreferences.getString(NAME, null)
    fun getRole(): String? = sharedPreferences.getString(ROLE, null)

    fun editName(name: String?)
    {
        with(sharedPreferencesEditor)
        {
            putString(NAME, name)
            apply()
        }
    }

    fun editRole(role: String?)
    {
        with(sharedPreferencesEditor)
        {
            putString(ROLE, role)
            apply()
        }
    }
}