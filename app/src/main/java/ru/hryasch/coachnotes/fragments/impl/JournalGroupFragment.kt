package ru.hryasch.coachnotes.fragments.impl

import android.Manifest
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.navigation.NavController
import androidx.navigation.findNavController

import com.evrencoskun.tableview.TableView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.nabinbhandari.android.permissions.PermissionHandler
import com.nabinbhandari.android.permissions.Permissions
import com.pawegio.kandroid.*
import com.soywiz.klock.*
import com.soywiz.klock.locale.russian
import com.tingyik90.snackprogressbar.SnackProgressBar
import com.tingyik90.snackprogressbar.SnackProgressBarManager
import kotlinx.android.synthetic.main.fragment_journal.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import moxy.MvpAppCompatFragment
import moxy.presenter.InjectPresenter
import org.koin.android.ext.android.get
import org.koin.core.KoinComponent
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named

import ru.hryasch.coachnotes.R
import ru.hryasch.coachnotes.application.App
import ru.hryasch.coachnotes.domain.journal.data.NoExistData
import ru.hryasch.coachnotes.domain.journal.data.UnknownData
import ru.hryasch.coachnotes.fragments.JournalView
import ru.hryasch.coachnotes.journal.table.TableAdapter
import ru.hryasch.coachnotes.journal.table.TableModel
import ru.hryasch.coachnotes.journal.presenters.impl.JournalPresenterImpl
import java.io.File
import java.util.*

class JournalGroupFragment : MvpAppCompatFragment(), JournalView, KoinComponent
{
    @InjectPresenter
    lateinit var presenter: JournalPresenterImpl

    private lateinit var buttonExportJournal: AppCompatImageButton
    private lateinit var buttonLock: AppCompatImageButton

    private lateinit var viewJournalTable: TableView
    private lateinit var tableAdapter: TableAdapter

    private lateinit var spinnerLoadingTable: ProgressBar
    private lateinit var noDataLabel: TextView

    private lateinit var buttonNextMonth: AppCompatImageButton
    private lateinit var buttonPrevMonth: AppCompatImageButton
    private lateinit var textViewPeriod: TextView

    private lateinit var okJournalShareClickListener: JournalShareOkListener
    private lateinit var errorJournalShareClickListener: JournalShareErrorListener
    private val snackProgressBarManager by lazy { SnackProgressBarManager(activity!!.findViewById(R.id.nav_host_fragment), lifecycleOwner = this) }

    private val monthNames: Array<String> = get(named("months_RU"))
    private val dayOfWeekLongNames: Array<String> = get(named("daysOfWeekLong_RU"))

    private lateinit var navController: NavController


    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View?
    {
        val layout = inflater.inflate(R.layout.fragment_journal, container, false)

        buttonExportJournal = layout.findViewById(R.id.journalButtonShare)
        buttonLock = layout.findViewById(R.id.journalButtonLock)

        spinnerLoadingTable = layout.findViewById(R.id.journalProgressBar)
        noDataLabel = layout.findViewById(R.id.journalTextViewNoData)
        viewJournalTable = layout.findViewById(R.id.journalTable)

        buttonNextMonth = layout.findViewById(R.id.journalButtonNextPeriod)
        buttonPrevMonth = layout.findViewById(R.id.journalButtonPrevPeriod)
        textViewPeriod = layout.findViewById(R.id.journalTextViewPeriod)

        okJournalShareClickListener = JournalShareOkListener(container!!)
        errorJournalShareClickListener = JournalShareErrorListener()

        navController = container!!.findNavController()

        loadingState()

        val toolbar: Toolbar = layout.findViewById(R.id.journalToolbar)
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        (activity as AppCompatActivity).supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        (activity as AppCompatActivity).supportActionBar!!.setDisplayShowHomeEnabled(true)

        toolbar.setNavigationOnClickListener {
            navController.navigateUp()
        }

        buttonNextMonth.setOnClickListener {
            presenter.nextMonth()
        }

        buttonPrevMonth.setOnClickListener {
            presenter.prevMonth()
        }

        snackProgressBarManager
            .setMessageMaxLines(1)
            .setBackgroundColor(R.color.colorPrimaryDarkHighlight)
            .setOverlayLayoutAlpha(0f)
            .setOnDisplayListener(object: SnackProgressBarManager.OnDisplayListener
            {
                override fun onDismissed(snackProgressBar: SnackProgressBar, onDisplayId: Int)
                {
                    presenter.onJournalSaveNotificationDismiss()
                    snackProgressBarManager.dismissAll()
                    super.onDismissed(snackProgressBar, onDisplayId)
                }
            })

        buttonLock.setOnClickListener {
            presenter.onLockUnlockJournal()
        }

        val groupData = JournalGroupFragmentArgs.fromBundle(arguments!!).groupData
        (activity as AppCompatActivity).supportActionBar!!.title = "${groupData.name}"

        GlobalScope.launch(Dispatchers.Default)
        {
            presenter.applyGroupData(groupData)
        }

        return layout
    }

    override fun lockJournal(isLocked: Boolean?)
    {
        when (isLocked)
        {
            true ->
            {
                buttonLock.visible = true
                buttonLock.setImageResource(R.drawable.ic_lock)
                DrawableCompat.wrap(buttonLock.drawable).setTint(ContextCompat.getColor(App.getCtx(), R.color.colorText))
            }

            false ->
            {
                buttonLock.visible = true
                buttonLock.setImageResource(R.drawable.ic_unlock)
                DrawableCompat.wrap(buttonLock.drawable).setTint(ContextCompat.getColor(App.getCtx(), R.color.colorAccent))
            }

            else ->
            {
                buttonLock.visible = false
            }
        }
    }

    override fun loadingState()
    {
        i("-- Waiting State --")
        spinnerLoadingTable.visible = true
        noDataLabel.visible = false
        viewJournalTable.visible = false
        buttonExportJournal.visible = false
    }

    override fun setPeriod(month: String, year: Int)
    {
        val str = "$month $year"
        textViewPeriod.text = str

        if (monthNames[DateTime.nowLocal().month0] == month && DateTime.nowLocal().yearInt == year)
        {
            buttonNextMonth.visibility = View.INVISIBLE
        }
        else
        {
            buttonNextMonth.visibility = View.VISIBLE
        }
    }

    override fun showSavingJournalNotification(isFinished: Boolean?)
    {
        when (isFinished)
        {
            false ->
            {
                snackProgressBarManager.setActionTextColor(android.R.color.transparent)
                SnackProgressBar(SnackProgressBar.TYPE_HORIZONTAL, "Журнал сохраняется...")
                    .setIsIndeterminate(true)
                    .setAllowUserInput(true)
                    .setSwipeToDismiss(true)
                    .setAction("Открыть папку", object: SnackProgressBar.OnActionClickListener {
                        override fun onActionClick()
                        {
                        }
                    })
                    .also {
                        runOnUiThread {
                            snackProgressBarManager.show(it, SnackProgressBarManager.LENGTH_INDEFINITE)
                        }
                    }
            }

            true ->
            {
                val currentShowing = snackProgressBarManager.getLastShown()
                currentShowing
                    ?.setMessage("Журнал сохранен")
                    ?.setIsIndeterminate(false)
                    ?.setProgressMax(1)
                    ?.setAction("Открыть папку", object: SnackProgressBar.OnActionClickListener {
                        override fun onActionClick()
                        {
                            val savingDir: File = get(named("journalDirectory"))
                            val intent = Intent(Intent.ACTION_VIEW)
                                .setDataAndType(Uri.parse(savingDir.absolutePath), "resource/folder")
                            startActivity(intent)
                            snackProgressBarManager.dismissAll()
                        }
                    })
                currentShowing?.also {
                    runOnUiThread {
                        snackProgressBarManager.updateTo(it)
                        snackProgressBarManager.setProgress(1)
                        snackProgressBarManager.setActionTextColor(R.color.colorAccent)
                    }
                }
                if (currentShowing == null)
                {
                    runOnUiThread {
                        toast("Журнал сохранен")
                    }
                }
            }
        }
    }

    override fun showDeleteColNotification(dateString: String?, col: Int)
    {
        if (dateString == null)
        {
            return
        }

        val date = DateFormat("dd/MM/yyyy").parse(dateString)

        val dialog = MaterialAlertDialogBuilder(this@JournalGroupFragment.context!!)
            .setTitle("Удаление столбца")
            .setMessage("Вы уверены, что хотите удалить данные за ${dayOfWeekLongNames[date.dayOfWeek.index0Monday].toLowerCase(Locale("ru"))}, ${date.format("dd.MM.yyyy")} ?")
            .setPositiveButton("Удалить") { dialog, _ ->
                dialog.cancel()
                presenter.deleteColumnData(col)
            }
            .setNegativeButton("Отмена") {
                    dialog, _ -> dialog.cancel()
            }
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(App.getCtx(), R.color.colorAccent))
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(App.getCtx(), R.color.colorPrimaryLight))
        }

        dialog.show()
    }

    override fun showingState(tableContent: TableModel?)
    {
        i("-- Showing State --")

        if (tableContent != null)
        {
            tableAdapter = get { parametersOf(context, tableContent) }
            viewJournalTable.adapter = tableAdapter
            viewJournalTable.tableViewListener = get { parametersOf(presenter) }
            tableAdapter.renderTable()

            noDataLabel.visible = false
            viewJournalTable.visible = true

            buttonExportJournal.visible = true
            checkShareButtonState()
        }
        else
        {
            noDataLabel.visible = true
            viewJournalTable.visible = false
            buttonExportJournal.visible = false
        }

        spinnerLoadingTable.visible = false
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

    override fun showLockedJournalNotification()
    {
        val today = DateTime.nowLocal()
        Snackbar
            .make((activity as AppCompatActivity).findViewById(android.R.id.content), "Пока журнал заблокирован, можно менять только текущий день (${today.dayOfMonth} ${today.month.localName(
                KlockLocale.russian)} ${today.yearInt}г. )", Snackbar.LENGTH_LONG)
            .show()
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

    private fun isEmpty(): Boolean
    {
        tableAdapter.tableContent.cellContent.forEach {
            it.forEach {
                if (it.data != null && it.data !is NoExistData)
                {
                    return@isEmpty false;
                }
            }
        }
        return true
    }

    private fun checkShareButtonState()
    {
        when
        {
            hasUnknownData() ->
            {
                DrawableCompat.wrap(buttonExportJournal.drawable).setTint(ContextCompat.getColor(App.getCtx(), R.color.colorJournalAbsenceGeneral))
                buttonExportJournal.setOnClickListener(errorJournalShareClickListener.apply { setMessage("Нужно задать данные для всех людей") })
            }

            isEmpty() ->
            {
                DrawableCompat.wrap(buttonExportJournal.drawable).setTint(ContextCompat.getColor(App.getCtx(), R.color.colorJournalAbsenceGeneral))
                buttonExportJournal.setOnClickListener(errorJournalShareClickListener.apply { setMessage("Журнал пуст") })
            }

            else ->
            {
                DrawableCompat.wrap(buttonExportJournal.drawable).setTint(Color.WHITE)
                buttonExportJournal.setOnClickListener(okJournalShareClickListener)
            }
        }
    }

    inner class JournalShareOkListener(private val container: ViewGroup): View.OnClickListener
    {
        override fun onClick(p0: View?)
        {
            i("clicked OK")
            Permissions
                .check(container.context,
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

    inner class JournalShareErrorListener(private var message: String = "Ошибка"): View.OnClickListener
    {
        fun setMessage(message: String)
        {
            this.message = message
        }

        override fun onClick(p0: View?)
        {
            i("clicked ERROR")

            val dialog = MaterialAlertDialogBuilder(this@JournalGroupFragment.context!!)
                            .setTitle("Невозможно экспортировать журнал")
                            .setMessage(message)
                            .setPositiveButton("Ок") {
                                dialog, _ -> dialog.cancel()
                            }
                            .create()
            dialog.setOnShowListener {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(App.getCtx(), R.color.colorAccent))
            }

            dialog.show()
        }
    }
}