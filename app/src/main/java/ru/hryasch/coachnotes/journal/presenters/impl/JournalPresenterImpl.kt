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
import ru.hryasch.coachnotes.fragments.JournalView
import ru.hryasch.coachnotes.journal.table.TableModel
import ru.hryasch.coachnotes.journal.presenters.JournalPresenter
import ru.hryasch.coachnotes.domain.common.GroupId
import ru.hryasch.coachnotes.domain.group.data.Group
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
    private var isJournalLocked: Boolean = true

    private lateinit var currentGroup: Group

    init
    {
        loadingState()
    }

    override fun onCellClicked(col: Int, row: Int)
    {
        tableHelper.onCellCLicked(col, row)
        viewState.refreshData()
    }

    override fun onCellLongPressed(col: Int, row: Int)
    {
        tableHelper.onCellLongPressed(col, row)
        viewState.refreshData()
    }

    override fun onColumnLongPressed(col: Int)
    {
        val date = tableHelper.tableModel.columnHeaderContent[col].data.timestamp
        viewState.showDeleteColNotification(date.format("dd/MM/yyyy"), col)
    }

    override fun onExportButtonClicked()
    {
        i("==== EXPORT CLICKED ====")

        viewState.showSavingJournalNotification(false)

        GlobalScope.launch(Dispatchers.Default)
        {
            i("wait for saving...")
            tableHelper.saveAllChunksImmediatelyAndWait()
            i("all saved")
            journalInteractor.exportJournal(chosenPeriod, tableHelper.getGroupId())
            viewState.showSavingJournalNotification(true)
        }
    }

    override fun onLockUnlockJournal()
    {
        isJournalLocked = !isJournalLocked
        viewState.lockJournal(isJournalLocked)
    }

    override fun onJournalSaveNotificationDismiss()
    {
        viewState.showSavingJournalNotification(null)
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
        viewState.loadingState()
        viewState.setPeriod(month, year)
        viewState.lockJournal(null)

        tableHelper.onChangePeriod()

        if (this::findingTableJob.isInitialized && findingTableJob.isActive)
        {
            findingTableJob.cancel()
        }

        findingTableJob = GlobalScope.launch(Dispatchers.IO)
        {
            i("findingTableJob launched")
            val newModel = journalInteractor.getJournal(chosenPeriod, currentGroup.id)?.toModel()

            if (newModel != null)
            {
                tableHelper.changeDataModel(newModel)
                withContext(Dispatchers.Main)
                {
                    viewState.showingState(tableHelper.tableModel)
                }
            }
            else
            {
                withContext(Dispatchers.Main)
                {
                    viewState.showingState(null)
                }
            }

            withContext(Dispatchers.Main)
            {
                viewState.setPeriod(month, year)

                isJournalLocked = true
                viewState.lockJournal(isJournalLocked)
            }
        }
    }

    override fun deleteColumnData(col: Int?)
    {
        if (col == null)
        {
            viewState.showDeleteColNotification(null)
        }
        else
        {
            tableHelper.onColumnLongPressed(col)
            viewState.refreshData()
        }
    }

    override fun applyGroupData(group: Group)
    {
        currentGroup = group
        changePeriod()
    }

    private fun loadingState()
    {
        viewState.loadingState()
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
            i("changeDataModel")
            tableModel = newModel

            for (col in 0 until tableModel.cellContent[0].size)
            {
                changingChunkJobs.add(Job())
                var chunkState = 0
                for (row in 0 until tableModel.cellContent.size)
                {
                    val cellData = tableModel.cellContent[row][col].data
                    if ((cellData is AbsenceData) || (cellData is PresenceData))
                    {
                        chunkState++
                    }
                }
                chunksStates.add(chunkState)
                //i("changeDataModel: chunk[$col]State = $chunkState")
            }
        }

        @Synchronized
        fun onChangePeriod()
        {
            i("onChangePeriod")
            changingChunkSupervisor.cancelChildren(CancellationException("fflush"))
            changingChunkSupervisor = Job()
            clearTableMetadata()
        }

        @Synchronized
        suspend fun saveAllChunksImmediatelyAndWait()
        {
            changingChunkSupervisor.cancelChildren(CancellationException("fflush"))
            changingChunkSupervisor.children.toList().forEach { it.join() }
        }

        @Synchronized
        fun onColumnLongPressed(col: Int)
        {
            tableModel.cellContent.forEach {
                val data = it[col].data
                if (data !is NoExistData)
                {
                    it[col].data = null
                }
            }
            chunksStates[col] = 0
            saveChunkOnBackground(col)
        }

        @Synchronized
        fun onCellLongPressed(col: Int, row: Int)
        {
            if (!isClickedTodayColumn(col) && isJournalLocked)
            {
                viewState.showLockedJournalNotification()
                return
            }

            val cell = tableModel.cellContent[row][col]
            when (cell.data)
            {
                is PresenceData,
                is AbsenceData ->
                {
                    cell.data = AbsenceData("Б")
                    saveChunkOnBackground(col)
                }

                is UnknownData ->
                {
                    chunksStates[col]++
                    cell.data = AbsenceData("Б")
                    saveChunkOnBackground(col)
                }

                is NoExistData -> { /* nothing */ }

                else ->
                {
                    chunksStates[col] = 1
                    for (i in 0 until tableModel.cellContent.size) // for each row
                    {
                        if (i != row && tableModel.cellContent[i][col].data !is NoExistData)
                        {
                            tableModel.cellContent[i][col].data = UnknownData()
                        }
                    }
                    cell.data = AbsenceData("Б")
                    saveChunkOnBackground(col)
                }
            }
        }

        @Synchronized
        fun onCellCLicked(col: Int, row: Int)
        {
            if (!isClickedTodayColumn(col) && isJournalLocked)
            {
                viewState.showLockedJournalNotification()
                return
            }

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
                    i("chunksStates[$col] = ${chunksStates[col]}")
                    if (isChunkEmpty(col))
                    {
                        for (i in 0 until tableModel.cellContent.size) // for each row
                        {
                            if (tableModel.cellContent[i][col].data !is NoExistData)
                            {
                                tableModel.cellContent[i][col].data = null
                            }
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

                is NoExistData -> { /* nothing */ }

                else -> //null
                {
                    chunksStates[col] = 1
                    for (i in 0 until tableModel.cellContent.size) // for each row
                    {
                        if (i != row && tableModel.cellContent[i][col].data !is NoExistData)
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

        @Synchronized
        private fun saveChunkOnBackground(col: Int)
        {
            i("saveChunkOnBackground")
            val day = tableModel.columnHeaderContent[col].data.timestamp.day
            var chunkBackup = chunksToSave[day]
            if (chunkBackup == null)
            {
                i("chunkBackup == null")
                chunkBackup = JournalChunk(tableModel.columnHeaderContent[col].data.timestamp, getGroupId())
                chunksToSave[day] = chunkBackup
            }
            else
            {
                i("chunkBackup != null")
                cancelSavingJob(col)
            }

            for (row in 0 until tableModel.rowHeaderContent.size)
            {
                chunkBackup.content[ChunkPersonName(tableModel.rowHeaderContent[row].data.person)] = CellData.getCopy(tableModel.cellContent[row][col].data)
            }
            i("update backuped chunk")


            changingChunkJobs[col] = GlobalScope.launch (Dispatchers.IO + changingChunkSupervisor)
            {
                i("changingChunkJob launched")
                try
                {
                    withTimeout(7000)
                    {
                        delay(Int.MAX_VALUE.toLong())
                    }
                }
                catch (e: TimeoutCancellationException)
                {
                    i("catch TimeoutCancellationException")
                    saveChunk(chunkBackup, col)
                }
                catch (e: CancellationException)
                {
                    i("catch CancellationException")
                    when (e.message)
                    {
                        "fflush" ->
                        {
                            i("fflush")
                            saveChunk(chunkBackup, col)
                        }
                    }
                }
            }
        }

        private fun cancelSavingJob(col: Int)
        {
            i("cancelSavingJob")
            if (!changingChunkJobs[col].isCancelled)
            {
                i("cancelled")
                changingChunkJobs[col].cancel()
            }
        }

        private suspend fun saveChunk(chunkBackup: JournalChunk, col: Int)
        {
            i("saveChunk")
            journalInteractor.saveJournalChunk(chunkBackup)

            var isNeedToRefresh = false
            synchronized(this)
            {
                if (isChunkShowingNow(chunkBackup))
                {
                    for (row in 0 until tableModel.rowHeaderContent.size)
                    {
                        tableModel.cellContent[row][col].data = chunkBackup.content[ChunkPersonName(tableModel.rowHeaderContent[row].data.person)]
                    }
                    isNeedToRefresh = true
                }
            }

            if (isNeedToRefresh)
            {
                i("Refresh data")
                withContext(Dispatchers.Main)
                {
                    viewState.refreshData()
                }
            }
        }

        private fun isClickedTodayColumn(col: Int): Boolean = tableModel.columnHeaderContent[col].data.timestamp == DateTime.nowLocal().local.date
    }
}