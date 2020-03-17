package ru.hryasch.coachnotes.journal.presenters.impl

import com.pawegio.kandroid.i
import com.soywiz.klock.*
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
import ru.hryasch.coachnotes.domain.journal.data.*
import ru.hryasch.coachnotes.domain.journal.interactors.JournalInteractor
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

// TODO: add "not synced" states to cells

@InjectViewState
class JournalPresenterImpl: MvpPresenter<JournalView>(), JournalPresenter, KoinComponent
{
    private val journalInteractor: JournalInteractor by inject()
    private val monthNames:Array<String> by inject(named("months_RU"))

    private val tableHelper: TableHelper = TableHelper()
    private lateinit var findingTableJob: Job
    private var chosenPeriod: YearMonth = DateTime.now().yearMonth

    init
    {
        changePeriod()
    }

    override fun onCellClicked(col: Int, row: Int)
    {
        tableHelper.onCellCLicked(col, row)
        viewState.refreshData()
    }

    override fun onExportButtonClicked()
    {
        i("==== EXPORT CLICKED ====")
        //TODO: wait for all savings
        GlobalScope.launch(Dispatchers.Default)
        {
            journalInteractor.exportJournal(chosenPeriod, tableHelper.getGroupId())
        }
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

        tableHelper.onChangePeriod()

        if (this::findingTableJob.isInitialized && findingTableJob.isActive)
        {
            findingTableJob.cancel()
        }

        findingTableJob = GlobalScope.launch(Dispatchers.IO)
        {
            val newModel = journalInteractor.getJournal(chosenPeriod, 1).toModel()
            tableHelper.changeDataModel(newModel)

            withContext(Dispatchers.Main)
            {
                viewState.showingState(tableHelper.tableModel)
                viewState.setPeriod(month, year)
            }
        }
    }

    private fun changePeriod()
    {
        changePeriod(monthNames[chosenPeriod.month.index0], chosenPeriod.year.year)
    }


    inner class TableHelper
    {
        var tableModel: TableModel = TableModel()
        private set

        private var chunksStates: MutableList<Int> = ArrayList()

        private var changingChunkSupervisor: Job = Job()
        private var changingChunkJobs: MutableList<Job> = ArrayList()
        private var chunksToSave: MutableMap<Int, JournalChunk> = HashMap()

        @Synchronized
        fun changeDataModel(newModel: TableModel)
        {
            tableModel = newModel

            tableModel.cellContent.forEach { _ ->
                chunksStates.add(0)
                changingChunkJobs.add(Job())
            }
        }

        @Synchronized
        fun onChangePeriod()
        {
            changingChunkSupervisor.cancelChildren(CancellationException("fflush"))
            changingChunkSupervisor = Job()
            clearTableMetadata()
        }

        @Synchronized
        fun onCellCLicked(col: Int, row: Int)
        {
            val cell = tableModel.cellContent[row][col]
            when (cell.data)
            {
                is PresenceData ->
                {
                    cell.data = AbsenceData()
                    saveChunkOnBackground(col)
                }

                is AbsenceData ->
                {
                    chunksStates[col]--
                    if (isChunkEmpty(col))
                    {
                        for (i in 0 until tableModel.cellContent.size) // for each row
                        {
                            tableModel.cellContent[i][col].data = null
                        }
                    }
                    else
                    {
                        cell.data = UnknownData()
                    }
                    saveChunkOnBackground(col)
                }

                is UnknownData ->
                {
                    chunksStates[col]++
                    cell.data = PresenceData()
                    saveChunkOnBackground(col)
                }

                is NoExistData ->
                {
                    //Nothing
                }

                else -> //null
                {
                    chunksStates[col] = 1
                    for (i in 0 until tableModel.cellContent.size) // for each row
                    {
                        if (i != row)
                        {
                            tableModel.cellContent[i][col].data = UnknownData()
                        }
                    }
                    cell.data = PresenceData()
                    saveChunkOnBackground(col)
                }
            }
        }

        @Synchronized
        fun isChunkEmpty(col: Int): Boolean = (chunksStates[col] == 0)

        @Synchronized
        fun getGroupId(): GroupId = tableModel.groupId

        @Synchronized
        fun isChunkShowingNow(chunk: JournalChunk): Boolean = (chunk.groupId == tableModel.groupId && (chosenPeriod == YearMonth.Companion.invoke(chunk.date.yearYear, chunk.date.month)))

        private fun clearTableMetadata()
        {
            chunksToSave.clear()
            changingChunkJobs.clear()
            chunksStates.clear()
        }

        private fun saveChunkOnBackground(col: Int)
        {
            val day = tableModel.columnHeaderContent[col].data.timestamp.day
            var chunkBackup = chunksToSave[day]
            if (chunkBackup == null)
            {
                chunkBackup = JournalChunk(tableModel.columnHeaderContent[col].data.timestamp, getGroupId()).apply {
                    for (row in 0 .. tableModel.rowHeaderContent.size)
                    {
                        content[ChunkPersonName(tableModel.rowHeaderContent[row].data.person)] = CellData.getCopy(tableModel.cellContent[row][col].data)
                    }
                }
                chunksToSave[day] = chunkBackup
            }
            else
            {
                cancelSavingJob(col)
            }

            changingChunkJobs[col] = GlobalScope.launch (Dispatchers.IO + changingChunkSupervisor)
            {
                try
                {
                    withTimeout(7000)
                    {
                        delay(Double.POSITIVE_INFINITY.toLong())
                    }
                }
                catch (e: CancellationException)
                {
                    when (e.message)
                    {
                        "fflush" -> saveChunk(chunkBackup, col)
                    }
                }
                catch (e: TimeoutCancellationException)
                {
                    saveChunk(chunkBackup, col)
                }
            }
        }

        private fun cancelSavingJob(col: Int)
        {
            if (!changingChunkJobs[col].isCancelled)
            {
                changingChunkJobs[col].cancel()
            }
        }

        private suspend fun saveChunk(chunkBackup: JournalChunk, col: Int)
        {
            journalInteractor.saveJournalChunk(chunkBackup)

            var isNeedToRefresh = false
            synchronized(this)
            {
                if (isChunkShowingNow(chunkBackup))
                {
                    for (row in 0 .. tableModel.rowHeaderContent.size)
                    {
                        tableModel.cellContent[row][col].data = chunkBackup.content[ChunkPersonName(tableModel.rowHeaderContent[row].data.person)]
                    }
                    isNeedToRefresh = true
                }
            }

            if (isNeedToRefresh)
            {
                withContext(Dispatchers.Main)
                {
                    viewState.refreshData()
                }
            }
        }


    }
}