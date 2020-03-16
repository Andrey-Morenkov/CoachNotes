package ru.hryasch.coachnotes.fragments.impl

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageButton

import com.evrencoskun.tableview.TableView
import com.nabinbhandari.android.permissions.PermissionHandler
import com.nabinbhandari.android.permissions.Permissions
import com.pawegio.kandroid.i
import com.pawegio.kandroid.visible
import moxy.MvpAppCompatFragment
import moxy.presenter.InjectPresenter
import org.koin.android.ext.android.get
import org.koin.core.parameter.parametersOf

import ru.hryasch.coachnotes.R
import ru.hryasch.coachnotes.fragments.api.JournalView
import ru.hryasch.coachnotes.journal.table.TableAdapter
import ru.hryasch.coachnotes.journal.table.TableModel
import ru.hryasch.coachnotes.journal.presenters.impl.JournalPresenterImpl

class JournalGroupFragment : MvpAppCompatFragment(), JournalView
{
    @InjectPresenter
    lateinit var presenter: JournalPresenterImpl

    private lateinit var buttonShareJournal: AppCompatImageButton

    private lateinit var viewJournalTable: TableView
    private lateinit var tableAdapter: TableAdapter

    private lateinit var spinnerLoadingTable: ProgressBar

    private lateinit var buttonNextMonth: AppCompatImageButton
    private lateinit var buttonPrevMonth: AppCompatImageButton
    private lateinit var textViewPeriod: TextView

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View?
    {
        val layout = inflater.inflate(R.layout.fragment_journal, container, false)

        buttonShareJournal = layout.findViewById(R.id.journalButtonShare)

        spinnerLoadingTable = layout.findViewById(R.id.journalProgressBar)
        viewJournalTable = layout.findViewById(R.id.journalTable)

        buttonNextMonth = layout.findViewById(R.id.journalButtonNextPeriod)
        buttonPrevMonth = layout.findViewById(R.id.journalButtonPrevPeriod)
        textViewPeriod = layout.findViewById(R.id.journalTextViewPeriod)

        (activity as AppCompatActivity).setSupportActionBar(layout.findViewById(R.id.journalToolbar))

        buttonShareJournal.setOnClickListener {
            i("clicked!!!!!")
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

        spinnerLoadingTable.visible = false
        viewJournalTable.visible = true
    }

    override fun refreshData()
    {
        i("-- Refresh data --")
        if (this::tableAdapter.isInitialized)
        {
            tableAdapter.notifyDataSetChanged()
        }
    }
}