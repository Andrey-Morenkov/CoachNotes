package ru.hryasch.coachnotes.common

import android.content.Context
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import com.google.android.material.textfield.TextInputEditText
import com.pawegio.kandroid.textWatcher
import com.tiper.MaterialSpinner
import org.koin.core.KoinComponent
import org.koin.core.get
import org.koin.core.qualifier.named
import ru.hryasch.coachnotes.R

class EditCoachBaseParamsElement (private val context: Context,
                                  private val surname: TextInputEditText,
                                  private val name: TextInputEditText,
                                  private val patronymic: TextInputEditText,
                                  private val roleSpinner: MaterialSpinner,
                                  private val customCoachRole: EditText,
                                  private val listener: FieldsCorrectListener?,
                                  private val initialRole: String? = null,
                                  private val initialSurname: String? = null,
                                  private val initialName: String? = null,
                                  private val initialPatronymic: String? = null): KoinComponent
{
    private val roles: List<String> = get(named("coachRoles"))
    private val customRolePosition = roles.indexOf(context.getString(R.string.coach_role_custom))

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

        surname.textWatcher {
            onTextChanged { _, _, _, _ -> checkFields() }
        }

        name.textWatcher {
            onTextChanged { _, _, _, _ -> checkFields() }
        }

        patronymic.textWatcher {
            onTextChanged { _, _, _, _ -> checkFields() }
        }

        customCoachRole.textWatcher {
            onTextChanged { _, _, _, _ -> checkFields() }
        }
    }

    private fun getCoachRoleAdapter() = ArrayAdapter(context, android.R.layout.simple_list_item_1, roles)

    private fun setInitialData()
    {
        initialSurname?.let {
            surname.setText(it)
        }
        initialName?.let {
            name.setText(it)
        }
        initialPatronymic?.let {
            patronymic.setText(it)
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
        if (surname.text.isNullOrBlank() ||
            name.text.isNullOrBlank() ||
            patronymic.text.isNullOrBlank() ||
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