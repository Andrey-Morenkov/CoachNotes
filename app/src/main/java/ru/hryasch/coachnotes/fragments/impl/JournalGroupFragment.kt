package ru.hryasch.coachnotes.fragments.impl

import android.Manifest
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat

import com.evrencoskun.tableview.TableView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.nabinbhandari.android.permissions.PermissionHandler
import com.nabinbhandari.android.permissions.Permissions
import com.pawegio.kandroid.i
import com.pawegio.kandroid.visible
import kotlinx.android.synthetic.main.fragment_journal.*
import moxy.MvpAppCompatFragment
import moxy.presenter.InjectPresenter
import org.koin.android.ext.android.get
import org.koin.core.parameter.parametersOf

import ru.hryasch.coachnotes.R
import ru.hryasch.coachnotes.application.App
import ru.hryasch.coachnotes.domain.journal.data.UnknownData
import ru.hryasch.coachnotes.fragments.api.JournalView
import ru.hryasch.coachnotes.journal.table.TableAdapter
import ru.hryasch.coachnotes.journal.table.TableModel
import ru.hryasch.coachnotes.journal.presenters.impl.JournalPresenterImpl

class JournalGroupFragment : MvpAppCompatFragment(), JournalView
{
    @InjectPresenter
    lateinit var presenter: JournalPresenterImpl

    private lateinit var buttonExportJournal: AppCompatImageButton

    private lateinit var viewJournalTable: TableView
    private lateinit var tableAdapter: TableAdapter

    private lateinit var spinnerLoadingTable: ProgressBar

    private lateinit var buttonNextMonth: AppCompatImageButton
    private lateinit var buttonPrevMonth: AppCompatImageButton
    private lateinit var textViewPeriod: TextView

    private lateinit var okJournalShareClickListener: JournalShareOkListener
    private lateinit var errorJournalShareClickListener: JournalShareErrorListener

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View?
    {
        val layout = inflater.inflate(R.layout.fragment_journal, container, false)

        buttonExportJournal = layout.findViewById(R.id.journalButtonShare)

        spinnerLoadingTable = layout.findViewById(R.id.journalProgressBar)
        viewJournalTable = layout.findViewById(R.id.journalTable)

        buttonNextMonth = layout.findViewById(R.id.journalButtonNextPeriod)
        buttonPrevMonth = layout.findViewById(R.id.journalButtonPrevPeriod)
        textViewPeriod = layout.findViewById(R.id.journalTextViewPeriod)

        okJournalShareClickListener = JournalShareOkListener(container!!)
        errorJournalShareClickListener = JournalShareErrorListener(container)

        (activity as AppCompatActivity).setSupportActionBar(layout.findViewById(R.id.journalToolbar))

        buttonNextMonth.setOnClickListener {
            presenter.nextMonth()
        }

        buttonPrevMonth.setOnClickListener {
            presenter.prevMonth()
        }

        return layout
    }

    override fun waitingState()
    {
        i("-- Waiting State --")
        spinnerLoadingTable.visible = true
        viewJournalTable.visible = false
        journalButtonShare.isEnabled = false
    }

    override fun setPeriod(month: String, year: Int)
    {
        val str = "$month $year"
        textViewPeriod.text = str
    }

    override fun showingState(tableContent: TableModel)
    {
        i("-- Showing State --")
        tableAdapter = get { parametersOf(context, tableContent) }
        viewJournalTable.adapter = tableAdapter
        viewJournalTable.tableViewListener = get { parametersOf(presenter) }
        tableAdapter.renderTable()

        journalButtonShare.isEnabled = true

        checkShareButtonState()

        spinnerLoadingTable.visible = false
        viewJournalTable.visible = true
    }

    override fun refreshData()
    {
        i("-- Refresh data --")
        if (this::tableAdapter.isInitialized)
        {
            tableAdapter.notifyDataSetChanged()
            checkShareButtonState()
        }
    }

    private fun hasUnknownData(): Boolean
    {
        tableAdapter.tableContent.cellContent.forEach {
            it.forEach {
                if (it.data is UnknownData)
                {
                    return@hasUnknownData true;
                }
            }
        }
        return false
    }

    private fun checkShareButtonState()
    {
        if (hasUnknownData())
        {
            DrawableCompat.wrap(journalButtonShare.drawable).setTint(ContextCompat.getColor(App.getCtx(), R.color.colorJournalAbsenceGeneral))
            journalButtonShare.setOnClickListener(errorJournalShareClickListener)
        }
        else
        {
            DrawableCompat.wrap(journalButtonShare.drawable).setTint(Color.WHITE)
            journalButtonShare.setOnClickListener(okJournalShareClickListener)
        }
    }

    inner class JournalShareOkListener(private val container: ViewGroup): View.OnClickListener
    {
        override fun onClick(p0: View?)
        {
            i("clicked OK")
            Permissions
                .check(container!!.context,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE),
                    container.context.getString(R.string.permission_external_storage_rationale),
                    Permissions.Options()
                        .setRationaleDialogTitle(container.context.getString(R.string.permission_external_storage_rationale_dialog_title))
                        .setSettingsDialogMessage(container.context.getString(R.string.permission_external_storage_rationale_dialog_message))
                        .setSettingsDialogTitle(container.context.getString(R.string.permission_external_storage_settings_dialog_title))
                        .setSettingsText(container.context.getString(R.string.permission_external_storage_settings_dialog_message)),
                    object: PermissionHandler()
                    {
                        override fun onGranted()
                        {
                            presenter.onExportButtonClicked()
                        }
                    })
        }
    }

    inner class JournalShareErrorListener(private val container: ViewGroup): View.OnClickListener
    {
        override fun onClick(p0: View?)
        {
            i("clicked ERROR")

            val dialog = MaterialAlertDialogBuilder(this@JournalGroupFragment.context!!)
                            .setTitle("Невозможно экспортировать журнал")
                            .setMessage("Нужно задать данные для всех людей")
                            .setPositiveButton("Ок") {
                                dialog, id -> dialog.cancel()
                            }
                            .create()
            dialog.setOnShowListener {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(App.getCtx().getColor(R.color.colorAccent))
            }

            dialog.show()
        }
    }
}