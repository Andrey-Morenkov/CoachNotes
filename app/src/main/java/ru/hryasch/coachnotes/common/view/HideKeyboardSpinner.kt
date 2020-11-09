package ru.hryasch.coachnotes.common.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat.getSystemService
import com.tiper.MaterialSpinner


class HideKeyboardSpinner @JvmOverloads constructor(context: Context,
                                                    attrs: AttributeSet? = null,
                                                    mode: Int = MODE_DROPDOWN) : MaterialSpinner(context, attrs, mode)
{
    init
    {
        val defaultFocusChangeListener = editText!!.onFocusChangeListener
        editText!!.onFocusChangeListener = OnFocusChangeListener { v, hasFocus ->
            hideKeyboard(v)
            defaultFocusChangeListener?.onFocusChange(v, hasFocus)
        }
    }

    private fun hideKeyboard(view: View)
    {
        val inputMethodManager: InputMethodManager = getSystemService(context, InputMethodManager::class.java)!!
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }
}