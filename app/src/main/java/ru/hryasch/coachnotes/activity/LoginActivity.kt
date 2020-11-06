package ru.hryasch.coachnotes.activity

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.pawegio.kandroid.textWatcher
import com.tiper.MaterialSpinner
import kotlinx.android.synthetic.main.activity_login.*
import ru.hryasch.coachnotes.R
import ru.hryasch.coachnotes.application.App
import ru.hryasch.coachnotes.repository.global.GlobalSettings

class LoginActivity: AppCompatActivity()
{
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        supportActionBar!!.hide()
        supportActionBar!!.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(this, R.color.background)))

        initialUiState()

        with(loginSpinnerRole)
        {
            adapter = getCoachRoleAdapter()
            onItemSelectedListener = object: MaterialSpinner.OnItemSelectedListener
            {
                override fun onItemSelected(parent: MaterialSpinner,
                                            view: View?,
                                            position: Int,
                                            id: Long)
                {
                    if (position == customRolePosition)
                    {
                        loginEditTextCustomRole.visibility = View.VISIBLE

                    }
                    else
                    {
                        loginEditTextCustomRole.visibility = View.INVISIBLE
                        loginEditTextCustomRole.text = null
                    }

                    checkFields()
                }

                override fun onNothingSelected(parent: MaterialSpinner)
                {
                }
            }
        }

        loginEditTextName.textWatcher {
            onTextChanged { _, _, _, _ -> checkFields() }
        }

        loginEditTextCustomRole.textWatcher {
            onTextChanged { _, _, _, _ -> checkFields() }
        }

        loginButtonLogin.setOnClickListener {
            GlobalSettings.Coach.editName(loginEditTextName.text?.toString())
            if (loginSpinnerRole.selection == customRolePosition)
            {
                GlobalSettings.Coach.editRole(loginEditTextCustomRole.text?.toString())
            }
            else
            {
                GlobalSettings.Coach.editRole(loginSpinnerRole.selectedItem as String?)
            }

            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun initialUiState()
    {
        loginEditTextCustomRole.visibility = View.INVISIBLE
        enableLoginButton(false)
    }

    private fun checkFields()
    {
        if (loginEditTextName.text.isNullOrBlank() ||
            loginSpinnerRole.selection == MaterialSpinner.INVALID_POSITION ||
            (loginSpinnerRole.selection == customRolePosition && loginEditTextCustomRole.text.isNullOrBlank()))
        {
            enableLoginButton(false)
            return
        }

        enableLoginButton(true)
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

    private fun getCoachRoleAdapter() = ArrayAdapter(this, android.R.layout.simple_list_item_1, roles)

    companion object
    {
        val roles: List<String> = listOf(
            App.getCtx().getString(R.string.coach_role_junior_coach),
            App.getCtx().getString(R.string.coach_role_common_coach),
            App.getCtx().getString(R.string.coach_role_senior_coach),
            App.getCtx().getString(R.string.coach_role_custom))

        val customRolePosition = roles.indexOf(App.getCtx().getString(R.string.coach_role_custom))
    }
}