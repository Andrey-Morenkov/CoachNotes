package ru.hryasch.coachnotes.repository.global

import android.content.Context
import com.pawegio.kandroid.e
import org.koin.core.KoinComponent
import org.koin.core.get
import org.koin.core.qualifier.named
import java.util.UUID

object GlobalSettings: KoinComponent
{
    private val appContext: Context = get(named("global"))

    object General
    {
        private const val PREFERENCE_FILE_NAME = "GENERAL"
        private const val USER_ID = "userId"

        private val sharedPreferences = appContext.getSharedPreferences(PREFERENCE_FILE_NAME, Context.MODE_PRIVATE)
        private val sharedPreferencesEditor = sharedPreferences.edit()

        fun isFirstLaunch(): Boolean
        {
            sharedPreferences.getString(USER_ID, null) ?: return true
            return true
        }

        fun generateAnalyticUserId(): String
        {
            val userId = UUID.randomUUID().toString()

            with(sharedPreferencesEditor)
            {
                putString(USER_ID, userId)
                apply()
            }

            return userId
        }
    }

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

        fun getRole(): String? = sharedPreferences.getString(ROLE, null)

        fun editName(name: String?)
        {
            with(sharedPreferencesEditor)
            {
                if (name == null)
                {
                    remove(FULL_NAME)
                }
                else
                {
                    putString(FULL_NAME, name.trim().replace(" +", " "))
                }
                apply()
            }
        }

        fun editRole(role: String?)
        {
            with(sharedPreferencesEditor)
            {
                if (role == null)
                {
                    remove(ROLE)
                }
                else
                {
                    putString(ROLE, role)
                }
                apply()
            }
        }

        fun clearLoginData()
        {
            editName(null)
            editRole(null)
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