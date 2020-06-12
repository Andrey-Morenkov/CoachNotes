package ru.hryasch.coachnotes.fragments.impl

import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.ProgressBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.pawegio.kandroid.visible
import com.soywiz.klock.DateTime
import com.tiper.MaterialSpinner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import moxy.MvpAppCompatFragment
import moxy.presenter.InjectPresenter
import org.koin.core.KoinComponent
import ru.hryasch.coachnotes.R
import ru.hryasch.coachnotes.application.App
import ru.hryasch.coachnotes.domain.group.data.Group
import ru.hryasch.coachnotes.fragments.GroupEditView
import ru.hryasch.coachnotes.groups.presenters.impl.GroupEditPresenterImpl
import ru.hryasch.coachnotes.repository.common.toRelative

class GroupEditFragment : MvpAppCompatFragment(), GroupEditView, KoinComponent
{
    @InjectPresenter
    lateinit var presenter: GroupEditPresenterImpl

    private lateinit var navController: NavController

    private lateinit var saveOrCreateGroup: MaterialButton
    private lateinit var deleteGroup: MaterialButton

    private lateinit var name: TextInputEditText
    private lateinit var paymentType: MaterialSpinner
    private lateinit var age1: MaterialSpinner
    private lateinit var age2: MaterialSpinner
    private lateinit var ageType: MaterialSpinner

    private lateinit var contentView: NestedScrollView
    private lateinit var loadingBar: ProgressBar

    private lateinit var currentGroup: Group

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View?
    {
        val layout = inflater.inflate(R.layout.fragment_edit_group, container, false)

        saveOrCreateGroup = layout.findViewById(R.id.groupEditButtonCreateOrSave)
        deleteGroup = layout.findViewById(R.id.groupEditButtonRemoveGroup)

        name = layout.findViewById(R.id.groupEditTextInputName)
        paymentType = layout.findViewById(R.id.groupEditSpinnerPaymentType)
        age1 = layout.findViewById(R.id.groupEditSpinnerAge1)
        age2 = layout.findViewById(R.id.groupEditSpinnerAge2)
        ageType = layout.findViewById(R.id.groupEditSpinnerAgeType)

        contentView = layout.findViewById(R.id.groupEditContent)
        loadingBar = layout.findViewById(R.id.groupEditProgressBarLoading)

        loadingState()

        navController = container!!.findNavController()

        val toolbar: Toolbar = layout.findViewById(R.id.groupEditToolbar)
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        (activity as AppCompatActivity).supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        (activity as AppCompatActivity).supportActionBar!!.setDisplayShowHomeEnabled(true)

        toolbar.setNavigationOnClickListener {
            navController.navigateUp()
        }

        GlobalScope.launch(Dispatchers.Default)
        {
            presenter.applyGroupDataAsync(GroupEditFragmentArgs.fromBundle(requireArguments()).groupData)
        }

        deleteGroup.visible = false

        return layout
    }

    override fun setGroupData(group: Group)
    {
        contentView.visible = true
        loadingBar.visible = false

        currentGroup = group

        paymentType.adapter = ArrayAdapter(context!!, android.R.layout.simple_list_item_1, generatePaymentTypes())
        age1.adapter = ArrayAdapter(context!!, android.R.layout.simple_list_item_1, generateAbsoluteYears())
        age2.adapter = ArrayAdapter(context!!, android.R.layout.simple_list_item_1, generateAbsoluteYears())
        ageType.adapter = ArrayAdapter(context!!, android.R.layout.simple_list_item_1, generateAgeTypes())

        ageType.selection = 0

        if (group.name.isNotBlank())
        {
            setExistGroupData()
        }

        ageType.onItemSelectedListener = object: MaterialSpinner.OnItemSelectedListener
        {
            override fun onItemSelected(parent: MaterialSpinner,
                                        view: View?,
                                        position: Int,
                                        id: Long)
            {
                when(position)
                {
                    // absolute
                    0 ->
                    {
                        val newAge1Selection = age1.selectedItem?.toString()?.toInt()
                        val newAge2Selection = age2.selectedItem?.toString()?.toInt()

                        newAge1Selection?.let {
                            if (it > 1000) return
                        }

                        newAge2Selection?.let {
                            if (it > 1000) return
                        }

                        age1.adapter = ArrayAdapter(context!!, android.R.layout.simple_list_item_1, generateAbsoluteYears())
                        age2.adapter = ArrayAdapter(context!!, android.R.layout.simple_list_item_1, generateAbsoluteYears())

                        if (newAge1Selection != null && newAge2Selection != null)
                        {
                            age1.selection = newAge2Selection.toInt()
                            age2.selection = newAge1Selection.toInt()
                        }
                        else
                        {
                            age1.selectedItem?.let {
                                age1.selection = newAge1Selection?.toInt() ?: MaterialSpinner.INVALID_POSITION
                            }

                            age2.selectedItem?.let {
                                age2.selection = newAge2Selection?.toInt() ?: MaterialSpinner.INVALID_POSITION
                            }
                        }
                    }

                    // relative
                    1 ->
                    {
                        val newAge1Selection = age1.selectedItem?.toString()?.toInt()
                        val newAge2Selection = age2.selectedItem?.toString()?.toInt()

                        newAge1Selection?.let {
                            if (it < 1000) return
                        }

                        newAge2Selection?.let {
                            if (it < 1000) return
                        }

                        age1.adapter = ArrayAdapter(context!!, android.R.layout.simple_list_item_1, generateRelativeYears())
                        age2.adapter = ArrayAdapter(context!!, android.R.layout.simple_list_item_1, generateRelativeYears())

                        if (newAge1Selection != null && newAge2Selection != null)
                        {
                            age1.selection = newAge2Selection.toInt().toRelative()
                            age2.selection = newAge1Selection.toInt().toRelative()
                        }
                        else
                        {
                            age1.selectedItem?.let {
                                age1.selection = newAge1Selection?.toRelative()?.toInt() ?: MaterialSpinner.INVALID_POSITION
                            }

                            age2.selectedItem?.let {
                                age2.selection = newAge2Selection?.toRelative()?.toInt() ?: MaterialSpinner.INVALID_POSITION
                            }
                        }
                    }
                }
            }

            override fun onNothingSelected(parent: MaterialSpinner)
            {
            }
        }

        saveOrCreateGroup.setOnClickListener {
            currentGroup.name = name.text.toString()
            currentGroup.isPaid = paymentType.selection.toBoolean()

            ageType.selection = 0
            val ageStart = age1.selectedItem?.toString()?.toInt()
            val ageFinish = age2.selectedItem?.toString()?.toInt()

            if (ageStart != null)
            {
                if (ageFinish != null)
                {
                    currentGroup.availableAbsoluteAge = ageStart .. ageFinish
                }
                else
                {
                    currentGroup.availableAbsoluteAge = ageStart .. ageStart
                }
            }
            else
            {
                currentGroup.availableAbsoluteAge = null
            }

            presenter.updateOrCreateGroup()
        }
    }

    override fun loadingState()
    {
        contentView.visible = false
        loadingBar.visible = true
    }

    override fun deleteGroupFinished()
    {
        navController.popBackStack()
        navController.navigateUp()
    }

    override fun updateOrCreateGroupFinished()
    {
        navController.navigateUp()
    }

    override fun showDeleteGroupNotification(group: Group?)
    {
        if (group == null)
        {
            return
        }

        val dialog = MaterialAlertDialogBuilder(this@GroupEditFragment.context!!)
            .setMessage("Удалить группу и все связанные с ней журналы?")
            .setPositiveButton("Удалить") { dialog, _ ->
                dialog.cancel()
                presenter.deleteGroup(currentGroup)
            }
            .setNegativeButton("Отмена") { dialog, _ ->
                dialog.cancel()
            }
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(App.getCtx(), R.color.colorAccent))
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(App.getCtx(), R.color.colorPrimaryLight))
        }

        dialog.show()
    }

    private fun setExistGroupData()
    {
        deleteGroup.visible = true
        (activity as AppCompatActivity).supportActionBar!!.setTitle(R.string.group_edit_screen_toolbar_title)
        saveOrCreateGroup.text = context!!.getString(R.string.save)

        name.text = SpannableStringBuilder(currentGroup.name)
        paymentType.selection = currentGroup.isPaid.toInt()

        currentGroup.availableAbsoluteAge?.let {
            age1.selection = it.first.toRelative()
            age2.selection = it.last.toRelative()
        }

        deleteGroup.setOnClickListener {
            presenter.onDeleteGroupClicked()
        }
    }

    private fun generateAbsoluteYears(): List<String>
    {
        val currYear = DateTime.nowLocal().yearInt
        val ages = ArrayList<String>(50)
        for (i in 0 until 50)
        {
            ages.add("${currYear - i}")
        }

        return ages
    }

    private fun generateRelativeYears(): List<String>
    {
        val ages = ArrayList<String>(50)
        for (i in 0 until 50)
        {
            ages.add("$i")
        }

        return ages
    }

    private fun generatePaymentTypes(): List<String> = listOf(context!!.getString(R.string.group_param_payment_free),
                                                              context!!.getString(R.string.group_param_payment_paid))

    private fun generateAgeTypes(): List<String> = listOf(context!!.getString(R.string.age_type_absolute),
                                                          context!!.getString(R.string.age_type_relative))
}

private fun Boolean.toInt(): Int = if (this) 1 else 0

private fun Int.toBoolean(): Boolean = this != 0