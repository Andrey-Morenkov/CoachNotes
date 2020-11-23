package ru.hryasch.coachnotes.activity

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.element_edit_coach_base_params.*
import org.koin.core.KoinComponent
import org.koin.core.get
import org.koin.core.qualifier.named
import ru.hryasch.coachnotes.R
import ru.hryasch.coachnotes.common.EditCoachBaseParamsElement
import ru.hryasch.coachnotes.common.FieldsCorrectListener
import ru.hryasch.coachnotes.repository.global.GlobalSettings

class LoginActivity: AppCompatActivity(), KoinComponent
{
    private val coachRoles: List<String> = get(named("coachRoles"))
    private lateinit var editParamsElement: EditCoachBaseParamsElement

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        supportActionBar!!.hide()
        supportActionBar!!.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(this, R.color.background)))

        initialUiState()

        editParamsElement = EditCoachBaseParamsElement(this,
                                                       coachBaseParamEditTextSurname,
                                                       coachBaseParamEditTextName,
                                                       coachBaseParamEditTextPatronymic,
                                                       coachBaseParamSpinnerRole,
                                                       coachBaseParamEditTextCustomRole,
                                                       object: FieldsCorrectListener {
                                                            override fun onFieldsCorrect(isCorrect: Boolean)
                                                            {
                                                                enableLoginButton(isCorrect)
                                                            }
                                                        })

        loginButtonLogin.setOnClickListener {
            val fullName = "${coachBaseParamEditTextSurname.text.toString().trim()} ${coachBaseParamEditTextName.text.toString().trim()} ${coachBaseParamEditTextPatronymic.text.toString().trim()}"
            GlobalSettings.Coach.editName(fullName)
            if (coachBaseParamSpinnerRole.selection == coachRoles.indexOf(getString(R.string.coach_role_custom)))
            {
                GlobalSettings.Coach.editRole(coachBaseParamEditTextCustomRole.text?.toString()?.trim())
            }
            else
            {
                GlobalSettings.Coach.editRole(coachBaseParamSpinnerRole.selectedItem as String?)
            }

            FirebaseCrashlytics.getInstance().log(fullName)
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun initialUiState()
    {
        coachBaseParamEditTextCustomRole.visibility = View.INVISIBLE
        enableLoginButton(false)
    }

    private fun enableLoginButton(enable: Boolean)
    {
        if (enable)
        {
            with(loginButtonLogin)
            {
                isEnabled = true
                setTextColor(ContextCompat.getColor(this@LoginActivity, R.color.colorText))
                setBackgroundColor(ContextCompat.getColor(this@LoginActivity, R.color.colorAccent))
            }
        }
        else
        {
            with(loginButtonLogin)
            {
                isEnabled = false
                setTextColor(ContextCompat.getColor(this@LoginActivity, R.color.colorDisabledText))
                setBackgroundColor(ContextCompat.getColor(this@LoginActivity, R.color.colorAccentDisabled))
            }
        }
    }
}