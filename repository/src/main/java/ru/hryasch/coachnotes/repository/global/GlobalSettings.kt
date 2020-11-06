package ru.hryasch.coachnotes.repository.global

import android.content.Context
import org.koin.core.KoinComponent
import org.koin.core.get
import org.koin.core.qualifier.named

object GlobalSettings: KoinComponent
{
    private val appContext: Context = get(named("global"))

    object Coach
    {
        private const val PREFERENCE_FILE_NAME = "COACH_DATA"
        private const val FULL_NAME = "fullName"
        private const val ROLE = "role"

        private val sharedPreferences = appContext.getSharedPreferences(PREFERENCE_FILE_NAME, Context.MODE_PRIVATE)
        private val sharedPreferencesEditor = sharedPreferences.edit()

        fun getFullName(): CoachFullName?
        {
            val fullNameString = sharedPreferences.getString(FULL_NAME, null) ?: return null

            val fullNameComponents = fullNameString.split(" ")
            return CoachFullName(fullNameComponents.elementAt(0),
                                 fullNameComponents.elementAt(1),
                                 fullNameComponents.elementAtOrNull(2))
        }

        fun getFullNameString(): String = getFullName()?.toString() ?: ""

        fun getRole(): String? = sharedPreferences.getString(ROLE, null)

        fun editName(name: String?)
        {
            with(sharedPreferencesEditor)
            {
                putString(FULL_NAME, name?.trim()?.replace(" +", " "))
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

        data class CoachFullName(val surname: String,
                                 val name: String,
                                 val patronymic: String?)
        {
            override fun toString(): String
            {
                var str = "$surname $name"
                if (!patronymic.isNullOrBlank())
                {
                    str += " $patronymic"
                }

                return str
            }
        }
    }
}