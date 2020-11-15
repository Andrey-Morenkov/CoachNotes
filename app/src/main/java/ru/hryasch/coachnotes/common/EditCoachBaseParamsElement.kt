package ru.hryasch.coachnotes.common

import android.content.Context
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import com.pawegio.kandroid.e
import com.pawegio.kandroid.textWatcher
import com.tiper.MaterialSpinner
import org.koin.core.KoinComponent
import org.koin.core.get
import org.koin.core.qualifier.named
import ru.hryasch.coachnotes.R

class EditCoachBaseParamsElement (private val context: Context,
                                  private val fullName: EditText,
                                  private val roleSpinner: MaterialSpinner,
                                  private val customCoachRole: EditText,
                                  private val listener: FieldsCorrectListener?,
                                  private val initialRole: String? = null,
                                  private val initialName: String? = null): KoinComponent
{
    private val roles: List<String> = get(named("coachRoles"))
    private val customRolePosition = roles.indexOf(context.getString(R.string.coach_role_custom))
    private val checkNameRegex = Regex("^[а-яА-Я]+\\s[а-яА-Я.]+\\s?[а-яА-Я.]*$")

    init
    {
        with(roleSpinner)
        {
            adapter = getCoachRoleAdapter()

            setInitialData()

            onItemSelectedListener = object: MaterialSpinner.OnItemSelectedListener
            {
                override fun onItemSelected(parent: MaterialSpinner,
                                            view: View?,
                                            position: Int,
                                            id: Long)
                {
                    if (position == customRolePosition)
                    {
                        customCoachRole.visibility = View.VISIBLE
                    }
                    else
                    {
                        customCoachRole.visibility = View.INVISIBLE
                        customCoachRole.text = null
                    }

                    checkFields()
                }

                override fun onNothingSelected(parent: MaterialSpinner)
                {
                }
            }
        }

        fullName.textWatcher {
            onTextChanged { _, _, _, _ -> checkFields() }
        }

        customCoachRole.textWatcher {
            onTextChanged { _, _, _, _ -> checkFields() }
        }
    }

    private fun getCoachRoleAdapter() = ArrayAdapter(context, android.R.layout.simple_list_item_1, roles)

    private fun setInitialData()
    {
        initialName?.let {
            fullName.setText(it)
        }
        initialRole?.let {
            val spinnerSelection = roles.indexOf(it)
            if (spinnerSelection >= 0)
            {
                roleSpinner.selection = spinnerSelection
                customCoachRole.visibility = View.INVISIBLE
            }
            else
            {
                roleSpinner.selection = customRolePosition
                customCoachRole.visibility = View.VISIBLE
                customCoachRole.setText(it)
            }
        }
        checkFields()
    }

    private fun checkFields()
    {
        if (fullName.text.isNullOrBlank() ||
            !fullName.text.toString().matches(checkNameRegex) ||
            roleSpinner.selection == MaterialSpinner.INVALID_POSITION ||
            (roleSpinner.selection == customRolePosition && customCoachRole.text.isNullOrBlank()))
        {
            listener?.onFieldsCorrect(false)
            return
        }

        listener?.onFieldsCorrect(true)
    }
}

interface FieldsCorrectListener
{
    fun onFieldsCorrect(isCorrect: Boolean)
}