package ru.hryasch.coachnotes.journal.presenters.impl

import com.pawegio.kandroid.i
import com.soywiz.klock.Date
import com.soywiz.klock.DateTime
import com.soywiz.klock.YearMonth
import com.soywiz.klock.months
import kotlinx.coroutines.*
import moxy.InjectViewState
import moxy.MvpPresenter
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.koin.core.qualifier.named

import ru.hryasch.coachnotes.converters.toModel
import ru.hryasch.coachnotes.fragments.api.JournalView
import ru.hryasch.coachnotes.journal.table.TableModel
import ru.hryasch.coachnotes.journal.presenters.JournalPresenter
import ru.hryasch.coachnotes.domain.common.GroupId
import ru.hryasch.coachnotes.domain.journal.data.AbsenceData
import ru.hryasch.coachnotes.domain.journal.data.CellData
import ru.hryasch.coachnotes.domain.journal.data.PresenceData
import ru.hryasch.coachnotes.domain.journal.interactors.JournalInteractor
import ru.hryasch.coachnotes.domain.person.PersonImpl
import java.util.*

// TODO: add "not synced" states to cells

@InjectViewState
class JournalPresenterImpl: MvpPresenter<JournalView>(), JournalPresenter, KoinComponent
{
    private val journalInteractor: JournalInteractor by inject()
    private val monthNames:Array<String> by inject(named("months_RU"))

    private val presenterScope = CoroutineScope(Dispatchers.IO)
    private lateinit var findingTableJob: Job
    private val          changingChunkJobs: MutableMap<String, Job> = TreeMap()

    private var tableModel: TableModel = TableModel()
    private var chosenPeriod: YearMonth = DateTime.now().yearMonth

    init
    {
        changePeriod()
    }

    override fun onCellClicked(col: Int, row: Int)
    {
        lateinit var backupData: ChunkEntryBackup

        synchronized(tableModel)
        {
            val cell = tableModel.cellContent[row][col]
            cell.data = when (cell.data)
            {
                is PresenceData -> AbsenceData()
                is AbsenceData -> null
                else -> PresenceData()
            }

            backupData = backupChangedCell(col, row)
        }

        val currJob = changingChunkJobs["$col|$row"]
        if (currJob != null && currJob.isActive)
        {
            i("cancelled")
            currJob.cancel()
        }

        changingChunkJobs["$col|$row"] = GlobalScope.launch(Dispatchers.Default)
        {
            i("waiting for update...")
            delay(5000)
            i("let's save")

            journalInteractor.saveChangedCell(backupData.date,
                                              backupData.personInfo,
                                              backupData.cellData,
                                              backupData.groupId)

            // hotfix
            var isNeedToRefresh = false
            synchronized(tableModel)
            {
                if (tableModel.groupId == backupData.groupId &&
                    chosenPeriod == YearMonth.Companion.invoke(backupData.date.yearYear, backupData.date.month))
                {
                    isNeedToRefresh = true
                    tableModel.cellContent[col][row].data = backupData.cellData
                }
            }

            // refresh table model
            if (isNeedToRefresh)
            {
                withContext(Dispatchers.Main)
                {
                    viewState.refreshData()
                }
            }
        }

        viewState.refreshData()
    }

    override fun onExportButtonClicked()
    {
        i("==== EXPORT CLICKED ====")
    }

    override fun nextMonth()
    {
        chosenPeriod += 1.months
        changePeriod()
    }

    override fun prevMonth()
    {
        chosenPeriod -= 1.months
        changePeriod()
    }

    override fun changePeriod(month: String, year: Int)
    {
        //TODO: custom strategy
        viewState.waitingState()
        viewState.setPeriod(month, year)

        if (this::findingTableJob.isInitialized && findingTableJob.isActive)
        {
            findingTableJob.cancel()
        }

        findingTableJob = presenterScope.launch {
            val newModel = journalInteractor
                            .getJournal(chosenPeriod, 1)
                            .toModel()

            synchronized(tableModel)
            {
                tableModel = newModel
                changingChunkJobs.clear()
            }

            withContext(Dispatchers.Main)
            {
                viewState.showingState(tableModel)
                viewState.setPeriod(month, year)
            }
        }
    }

    override fun onDestroy()
    {
        super.onDestroy()
    }

    private fun changePeriod()
    {
        changePeriod(monthNames[chosenPeriod.month.index0], chosenPeriod.year.year)
    }

    private fun backupChangedCell(col: Int, row: Int): ChunkEntryBackup
    {
        return ChunkEntryBackup(Date(tableModel.columnHeaderContent[col].data.timestamp.encoded),
                                tableModel.groupId,
                                PersonImpl(tableModel.rowHeaderContent[row].data.person.surname,
                                           tableModel.rowHeaderContent[row].data.person.name),
                                CellData.getCopy(tableModel.cellContent[row][col].data)
        )
    }

    private data class ChunkEntryBackup(val date: Date,
                                        val groupId: GroupId,
                                        val personInfo: PersonImpl,
                                        val cellData: CellData?)
}