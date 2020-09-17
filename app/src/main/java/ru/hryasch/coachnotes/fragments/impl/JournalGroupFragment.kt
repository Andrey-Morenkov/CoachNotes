package ru.hryasch.coachnotes.fragments.impl

import android.Manifest
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.NumberPicker
import android.widget.ProgressBar
import android.widget.TextView
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
import com.tingyik90.snackprogressbar.SnackProgressBar
import com.tingyik90.snackprogressbar.SnackProgressBarManager
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
import ru.hryasch.coachnotes.domain.group.data.Group
import ru.hryasch.coachnotes.domain.journal.data.NoExistData
import ru.hryasch.coachnotes.domain.journal.data.UnknownData
import ru.hryasch.coachnotes.fragments.JournalView
import ru.hryasch.coachnotes.journal.table.TableAdapter
import ru.hryasch.coachnotes.journal.table.TableModel
import ru.hryasch.coachnotes.journal.presenters.impl.JournalPresenterImpl
import java.io.File
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

class JournalGroupFragment : MvpAppCompatFragment(), JournalView, KoinComponent
{
    @InjectPresenter
    lateinit var presenter: JournalPresenterImpl
    private lateinit var navController: NavController
    private lateinit var currentGroup: Group

    // Toolbar items
    private lateinit var lockUnlockButton: MenuItem
    private lateinit var showAllDaysButton: MenuItem
    private lateinit var showAllPeopleButton: MenuItem
    private lateinit var exportDocButton: MenuItem

    // Period section
        // Views
        private lateinit var buttonNextMonth: AppCompatImageButton
        private lateinit var buttonPrevMonth: AppCompatImageButton
        private lateinit var textViewPeriod: TextView

        // Data
        private lateinit var selectedPeriod: YearMonth

    // Journal section
        // Views
        private lateinit var viewJournalTable: TableView
        private lateinit var loadingView: View
        private lateinit var emptyView: View

        // Data
        private lateinit var tableAdapter: TableAdapter

    // Others
        // Views
        private val snackProgressBarManager by lazy { SnackProgressBarManager(requireActivity().findViewById(R.id.nav_host_fragment), lifecycleOwner = this) }

        // Data
        private val initializerHelper: InflaterAndInitializer
        private lateinit var okJournalShareClickListener: JournalShareOkListener
        private lateinit var errorJournalShareClickListener: JournalShareErrorListener
        private val monthNames: Array<String> = get(named("months_RU"))
        private val dayOfWeekLongNames: Array<String> = get(named("daysOfWeekLong_RU"))


    init
    {
        initializerHelper = InflaterAndInitializer()
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View?
    {
        val layout = inflater.inflate(R.layout.fragment_journal, container, false)
        currentGroup = JournalGroupFragmentArgs.fromBundle(requireArguments()).groupData
        navController = container!!.findNavController()

        initializerHelper.initToolbar(layout)
        initializerHelper.initPeriodSection(layout)
        initializerHelper.initJournalSection(layout)


        okJournalShareClickListener = JournalShareOkListener(container)
        errorJournalShareClickListener = JournalShareErrorListener()

        navController = container.findNavController()

        loadingState()

        snackProgressBarManager
            .setMessageMaxLines(1)
            .setBackgroundColor(R.color.colorPrimaryDarkHighlight)
            .setOverlayLayoutAlpha(0f)
            .setOnDisplayListener(object : SnackProgressBarManager.OnDisplayListener
                                  {
                                      override fun onDismissed(
                                          snackProgressBar: SnackProgressBar,
                                          onDisplayId: Int
                                      )
                                      {
                                          presenter.onJournalSaveNotificationDismiss()
                                          snackProgressBarManager.dismissAll()
                                          super.onDismissed(snackProgressBar, onDisplayId)
                                      }
                                  })




        GlobalScope.launch(Dispatchers.Default)
        {
            presenter.applyGroupData(currentGroup)
        }

        return layout
    }

    override fun onPrepareOptionsMenu(menu: Menu)
    {
        lockUnlockButton = menu.findItem(R.id.journal_lock_item)
        showAllDaysButton = menu.findItem(R.id.journal_visibility_all_days_item)
        showAllPeopleButton = menu.findItem(R.id.journal_visibility_all_people_item)
        exportDocButton = menu.findItem(R.id.journal_upload_docx_item)

        return super.onPrepareOptionsMenu(menu)
    }

    override fun lockJournal(isLocked: Boolean?)
    {
        when (isLocked)
        {
            true ->
            {
                lockUnlockButton.isVisible = true
                lockUnlockButton.setIcon(R.drawable.ic_lock)
                DrawableCompat.wrap(lockUnlockButton.icon)
                    .setTint(ContextCompat.getColor(App.getCtx(), R.color.colorText))
            }

            false ->
            {
                lockUnlockButton.isVisible = true
                lockUnlockButton.setIcon(R.drawable.ic_unlock)
                DrawableCompat.wrap(lockUnlockButton.icon)
                    .setTint(ContextCompat.getColor(App.getCtx(), R.color.colorAccent))
            }

            else  ->
            {
                lockUnlockButton.isVisible = false
            }
        }
    }

    override fun showAllPeople(isShowAll: Boolean?)
    {
        when (isShowAll)
        {
            true ->
            {
                showAllPeopleButton.isVisible = true
                showAllPeopleButton.isChecked = true
            }

            false ->
            {
                showAllPeopleButton.isVisible = true
                showAllPeopleButton.isChecked = false
            }

            else  ->
            {
                showAllPeopleButton.isVisible = false
            }
        }
    }

    override fun showAllDays(isShowAll: Boolean?)
    {
        when (isShowAll)
        {
            true ->
            {
                showAllDaysButton.isVisible = true
                showAllDaysButton.isChecked = true
            }

            false ->
            {
                showAllDaysButton.isVisible = true
                showAllDaysButton.isChecked = false
            }

            else  ->
            {
                showAllDaysButton.isVisible = false
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

    override fun setPeriod(period: YearMonth)
    {
        val str = "$month $year"
        textViewPeriod.text = str

        if (monthNames[ZonedDateTime.now().month.value - 1] == month && ZonedDateTime.now().year == year)
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
                    .setAction("Открыть папку", object : SnackProgressBar.OnActionClickListener
                    {
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
                    ?.setAction("Открыть папку", object : SnackProgressBar.OnActionClickListener
                    {
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

        val date = LocalDate.parse(dateString, DateTimeFormatter.ofPattern("dd/MM/yyyy"))

        val dialog = MaterialAlertDialogBuilder(this@JournalGroupFragment.requireContext())
            .setTitle("Удаление столбца")
            .setMessage("Вы уверены, что хотите удалить данные за ${dayOfWeekLongNames[date.dayOfWeek.value - 1].toLowerCase(Locale("ru"))}, ${date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))} ?")
            .setPositiveButton("Удалить") { dialog, _ ->
                dialog.cancel()
                presenter.deleteColumnData(col)
            }
            .setNegativeButton("Отмена") { dialog, _ ->
                dialog.cancel()
            }
            .create()

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
        val today = ZonedDateTime.now()
        Snackbar
            .make((activity as AppCompatActivity).findViewById(android.R.id.content), "Пока журнал заблокирован, можно менять только текущий день (${today.dayOfMonth} ${today.month.getDisplayName(TextStyle.FULL_STANDALONE, Locale.getDefault())} ${today.year}г. )", Snackbar.LENGTH_LONG)
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
                DrawableCompat.wrap(buttonExportJournal.drawable)
                    .setTint(ContextCompat.getColor(App.getCtx(), R.color.colorJournalAbsenceGeneral))
                buttonExportJournal.setOnClickListener(errorJournalShareClickListener.apply { setMessage("Нужно задать данные для всех людей") })
            }

            isEmpty()        ->
            {
                DrawableCompat.wrap(buttonExportJournal.drawable)
                    .setTint(ContextCompat.getColor(App.getCtx(), R.color.colorJournalAbsenceGeneral))
                buttonExportJournal.setOnClickListener(errorJournalShareClickListener.apply { setMessage("Журнал пуст") })
            }

            else             ->
            {
                DrawableCompat.wrap(buttonExportJournal.drawable).setTint(Color.WHITE)
                buttonExportJournal.setOnClickListener(okJournalShareClickListener)
            }
        }
    }

    inner class InflaterAndInitializer: KoinComponent
    {
        private lateinit var changePeriodDialog: AlertDialog

        fun initToolbar(layout: View)
        {
            val toolbar = layout.findViewById<Toolbar>(R.id.journalToolbar)
            with(toolbar)
            {
                title = currentGroup.name
                setNavigationOnClickListener {
                    navController.navigateUp()
                }
                inflateMenu(R.menu.journal_menu)
                setOnMenuItemClickListener {
                    when (it.itemId)
                    {
                        R.id.journal_lock_item -> presenter.onLockUnlockJournal()
                        R.id.journal_visibility_all_days_item -> presenter.onShowAllDaysClicked(it.isChecked)
                        R.id.journal_visibility_all_people_item -> presenter.onShowAllPeopleClicked(it.isChecked)
                        R.id.journal_upload_docx_item -> presenter.onExportDocButtonClicked()
                    }

                    return@setOnMenuItemClickListener false
                }
            }
        }

        fun initPeriodSection(layout: View)
        {
            buttonNextMonth = layout.findViewById(R.id.journalButtonNextPeriod)
            buttonPrevMonth = layout.findViewById(R.id.journalButtonPrevPeriod)
            textViewPeriod = layout.findViewById(R.id.journalTextViewPeriod)

            initChangePeriodDialog()

            textViewPeriod.setOnClickListener {
                changePeriodDialog.show()
            }

            buttonNextMonth.setOnClickListener {
                selectedPeriod = selectedPeriod.plusMonths(1)
                presenter.changePeriod(selectedPeriod)
            }

            buttonPrevMonth.setOnClickListener {
                selectedPeriod = selectedPeriod.minusMonths(1)
                presenter.changePeriod(selectedPeriod)
            }
        }

        fun initJournalSection(layout: View)
        {
            viewJournalTable = layout.findViewById(R.id.journalTable)
            loadingView = layout.findViewById(R.id.journalLoading)
            emptyView = layout.findViewById(R.id.journalNoData)
        }

        fun initStuff(layout: View)
        {

        }

        private fun initChangePeriodDialog()
        {
            val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_journal_month_year_picker, null)

            val years: Array<Int> = get(named("journalYears"))

            val yearPicker: NumberPicker  = dialogView.findViewById(R.id.journalYearPicker)
            val monthPicker: NumberPicker = dialogView.findViewById(R.id.journalMonthPicker)

            with(yearPicker)
            {
                minValue = years[0]
                maxValue = years[years.size - 1]
                wrapSelectorWheel = false
                value = selectedPeriod.year
            }

            with(monthPicker)
            {
                minValue = 1
                maxValue = monthNames.size
                wrapSelectorWheel = false
                displayedValues = monthNames
                value = selectedPeriod.monthValue
            }

            val dialogBuilder = MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView)
                .setTitle("Выбор периода")
                .setPositiveButton(android.R.string.ok) { dialog, _ ->
                    presenter.changePeriod(YearMonth.of(yearPicker.value, monthPicker.value))
                    dialog.dismiss()
                }
                .setNegativeButton("Отмена") { dialog, _ ->
                    dialog.dismiss()
                }

            val now = YearMonth.now()
            if (selectedPeriod.monthValue != now.monthValue || selectedPeriod.year != now.year)
            {
                dialogBuilder.setNeutralButton("Текущий месяц") { dialog, _ ->
                    presenter.changePeriod(now)
                    dialog.dismiss()
                }
            }

            changePeriodDialog = dialogBuilder.create()
        }
    }

    inner class JournalShareOkListener(private val container: ViewGroup) : View.OnClickListener
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
                       object : PermissionHandler()
                       {
                           override fun onGranted()
                           {
                               presenter.onExportDocButtonClicked()
                           }
                       })
        }
    }

    inner class JournalShareErrorListener(private var message: String = "Ошибка") :
        View.OnClickListener
    {
        fun setMessage(message: String)
        {
            this.message = message
        }

        override fun onClick(p0: View?)
        {
            i("clicked ERROR")

            val dialog = MaterialAlertDialogBuilder(this@JournalGroupFragment.requireContext())
                .setTitle("Невозможно экспортировать журнал")
                .setMessage(message)
                .setPositiveButton("Ок") { dialog, _ ->
                    dialog.cancel()
                }
                .create()

            dialog.show()
        }
    }
}