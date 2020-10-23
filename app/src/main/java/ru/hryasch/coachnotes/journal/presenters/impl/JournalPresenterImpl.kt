package ru.hryasch.coachnotes.journal.presenters.impl

import com.pawegio.kandroid.d
import com.pawegio.kandroid.i
import kotlinx.coroutines.*
import moxy.InjectViewState
import moxy.MvpPresenter
import org.koin.core.KoinComponent
import org.koin.core.get

import ru.hryasch.coachnotes.fragments.JournalView
import ru.hryasch.coachnotes.journal.table.data.TableModel
import ru.hryasch.coachnotes.journal.presenters.JournalPresenter
import ru.hryasch.coachnotes.domain.common.GroupId
import ru.hryasch.coachnotes.domain.group.data.Group
import ru.hryasch.coachnotes.domain.journal.data.*
import ru.hryasch.coachnotes.domain.journal.interactors.JournalInteractor

import java.time.LocalDate
import java.time.YearMonth
import java.util.Collections
import java.util.stream.IntStream

import kotlin.collections.ArrayList
import kotlin.collections.HashMap

@InjectViewState
class JournalPresenterImpl: MvpPresenter<JournalView>(), JournalPresenter, KoinComponent
{
    private val journalInteractor: JournalInteractor = get()

    // Current params
        // Data
        private lateinit var currentGroup: Group
        private var selectedPeriod: YearMonth = YearMonth.now()

        // Flags
        private var isJournalLocked: Boolean = true
        private var isShowAllPeople: Boolean = false
        private var isShowAllDays:   Boolean = false

    // Others
    private lateinit var findingTableJob: Job
    private val journalTableProxy: TableProxy = TableProxy()



    // Events
    override fun onCellClicked(col: Int, row: Int)
    {
        journalTableProxy.onCellClicked(col, row)
        viewState.refreshData()
    }

    override fun onCellLongPressed(col: Int, row: Int)
    {
        journalTableProxy.onCellLongPressed(col, row)
        viewState.refreshData()
    }

    override fun onColumnLongPressed(col: Int)
    {
        viewState.showDeleteColumnNotification(journalTableProxy.tableModel.columnHeaderContent[col].date, col)
    }

    override fun onExportDocButtonClicked()
    {
        i("==== EXPORT CLICKED ====")

        viewState.showSavingJournalNotification(false)

        GlobalScope.launch(Dispatchers.Default)
        {
            i("wait for saving...")
            journalTableProxy.saveAllChunksImmediatelyAndWait()
            i("all saved")
            journalInteractor.exportJournal(selectedPeriod, journalTableProxy.getGroupId())
            viewState.showSavingJournalNotification(true)
        }
    }

    override fun onLockUnlockJournal()
    {
        isJournalLocked = !isJournalLocked
        viewState.lockJournal(isJournalLocked)
    }

    override fun onShowAllPeopleClicked(isShowAll: Boolean)
    {
        isShowAllPeople = isShowAll
        if (isShowAllPeople)
        {
            updatePeopleSeqNumbers(null)
            viewState.hideRows(Collections.emptyList())
            viewState.refreshData()
        }
        else
        {
            updatePeopleSeqNumbers(journalTableProxy.tableModel.rowsToHide)
            viewState.hideRows(journalTableProxy.tableModel.rowsToHide)
            viewState.refreshData()
        }
    }

    override fun onShowAllDaysClicked(isShowAll: Boolean)
    {
        isShowAllDays = isShowAll
        if (isShowAllDays)
        {
            viewState.hideColumns(Collections.emptyList())
        }
        else
        {
            viewState.hideColumns(journalTableProxy.tableModel.columnsToHide)
        }
    }

    override fun onJournalSaveNotificationDismiss()
    {
        viewState.showSavingJournalNotification(null)
    }


    // Commands
    override fun changePeriod(newPeriod: YearMonth)
    {
        selectedPeriod = newPeriod

        // TODO: custom strategy
        with(viewState)
        {
            loadingState()
            setPeriod(selectedPeriod)
            lockJournal(null)
            hideColumns(null)
            hideRows(null)
        }

        journalTableProxy.onChangePeriod()

        if (this::findingTableJob.isInitialized && findingTableJob.isActive)
        {
            findingTableJob.cancel()
        }

        findingTableJob = GlobalScope.launch(Dispatchers.Default)
        {
            i("findingTableJob launched")
            val rawTableData = journalInteractor.getJournal(selectedPeriod, currentGroup.id)

            if (rawTableData == null)
            {
                journalTableProxy.changeDataModel(null)
                withContext(Dispatchers.Main)
                {
                    viewState.showingState(null)
                }
            }
            else
            {
                journalTableProxy.changeDataModel(rawTableData)
                withContext(Dispatchers.Main)
                {
                    viewState.showingState(journalTableProxy.tableModel)
                }
            }

            // Finally apply other data
            withContext(Dispatchers.Main)
            {
                resetFlags()

                with(viewState)
                {
                    setPeriod(selectedPeriod)
                    lockJournal(isJournalLocked)
                    if (journalTableProxy.tableModel.columnsToHide.isNotEmpty())
                    {
                        hideColumns(journalTableProxy.tableModel.columnsToHide)
                    }
                    if (journalTableProxy.tableModel.rowsToHide.isNotEmpty())
                    {
                        hideRows(journalTableProxy.tableModel.rowsToHide)
                    }
                }
            }
            i("findingTableJob ended")
        }
    }

    override fun deleteColumnData(col: Int)
    {
        journalTableProxy.clearColumnData(col)
        viewState.refreshData()
    }

    override fun applyGroupData(group: Group)
    {
        if (::currentGroup.isInitialized && (currentGroup === group || currentGroup.id == group.id))
        {
            d("Skip applying same group data from fragment re-create")
            return
        }

        currentGroup = group
        changePeriod(selectedPeriod)
    }



    private fun resetFlags()
    {
        isJournalLocked = true
        isShowAllDays = false
        isShowAllPeople = false
    }

    private fun updatePeopleSeqNumbers(rowsToHide: List<Int>?)
    {
        if (rowsToHide == null || rowsToHide.isEmpty())
        {
            var newSeq = 0
            journalTableProxy.tableModel.rowHeaderContent.forEach {
                it.index = newSeq
                newSeq++
            }
        }
        else
        {
            var newSeq = 0
            for ((i, rhc) in journalTableProxy.tableModel.rowHeaderContent.withIndex())
            {
                if (!rowsToHide.contains(i))
                {
                    rhc.index = newSeq
                    newSeq++
                }
            }
        }
    }


    // TODO: rework save chunks logic
    inner class TableProxy
    {
        lateinit var tableModel: TableModel
        private set

        private var chunksStates: MutableList<Int> = ArrayList()

        private var changingChunkSupervisor: Job = Job()
        private var changingChunkJobs: MutableList<Job> = ArrayList()
        private var chunksToSave: MutableMap<Int, JournalChunk> = HashMap()

        @Synchronized
        fun changeDataModel(newRawData: RawTableData?)
        {
            i("changeDataModel")
            tableModel = TableModel(newRawData)
            if (tableModel.cellsContent.isEmpty())
            {
                return
            }

            for (col in 0 until tableModel.cellsContent[0].size)
            {
                changingChunkJobs.add(Job())
                var chunkState = 0
                for (row in 0 until tableModel.cellsContent.size)
                {
                    val cellData = tableModel.cellsContent[row][col].data
                    if ((cellData is AbsenceData) || (cellData is PresenceData))
                    {
                        chunkState++
                    }
                }
                chunksStates.add(chunkState)
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
            changingChunkSupervisor.children.forEach { it.join() }
        }

        @Synchronized
        fun clearColumnData(col: Int)
        {
            tableModel.cellsContent.forEach {
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

            val cell = tableModel.cellsContent[row][col]
            when (cell.data)
            {
                is PresenceData,
                is AbsenceData ->
                {
                    // TODO: hotfix for manual set no exist data
                    if (cell.data!!.mark == "Б")
                    {
                        chunksStates[col]--
                        cell.data = NoExistData()
                        if (isChunkEmpty(col))
                        {
                            for (i in 0 until tableModel.cellsContent.size) // for each row
                            {
                                if (tableModel.cellsContent[i][col].data !is NoExistData)
                                {
                                    tableModel.cellsContent[i][col].data = null
                                }
                            }
                        }
                    }
                    else
                    {
                        cell.data = AbsenceData("Б")
                    }

                    saveChunkOnBackground(col)
                }

                is UnknownData ->
                {
                    chunksStates[col]++
                    cell.data = AbsenceData("Б")

                    for (i in 0 until tableModel.cellsContent.size) // for each row
                    {
                        if (i != row && tableModel.cellsContent[i][col].data == null)
                        {
                            tableModel.cellsContent[i][col].data = UnknownData()
                        }
                    }

                    saveChunkOnBackground(col)
                }

                is NoExistData ->
                {
                    /* nothing */
                    // TODO: hotfix for manual set no exist data
                    chunksStates[col]++
                    cell.data = PresenceData()

                    for (i in 0 until tableModel.cellsContent.size) // for each row
                    {
                        if (tableModel.cellsContent[i][col].data == null)
                        {
                            tableModel.cellsContent[i][col].data = UnknownData()
                        }
                    }

                    saveChunkOnBackground(col)
                }

                else ->
                {
                    chunksStates[col] = 1
                    for (i in 0 until tableModel.cellsContent.size) // for each row
                    {
                        if (i != row && tableModel.cellsContent[i][col].data !is NoExistData)
                        {
                            tableModel.cellsContent[i][col].data = UnknownData()
                        }
                    }
                    cell.data = AbsenceData("Б")
                    saveChunkOnBackground(col)
                }
            }
        }

        @Synchronized
        fun onCellClicked(col: Int, row: Int)
        {
            if (!isClickedTodayColumn(col) && isJournalLocked)
            {
                viewState.showLockedJournalNotification()
                return
            }

            val cell = tableModel.cellsContent[row][col]
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
                        for (i in 0 until tableModel.cellsContent.size) // for each row
                        {
                            if (tableModel.cellsContent[i][col].data !is NoExistData)
                            {
                                tableModel.cellsContent[i][col].data = null
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
                    for (i in 0 until tableModel.cellsContent.size) // for each row
                    {
                        if (i != row && tableModel.cellsContent[i][col].data !is NoExistData)
                        {
                            tableModel.cellsContent[i][col].data = UnknownData()
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
        fun isChunkShowingNow(chunk: JournalChunk): Boolean = (chunk.groupId == tableModel.groupId && (selectedPeriod == YearMonth.of(chunk.date.year, chunk.date.month)))



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
            val day = tableModel.columnHeaderContent[col].date.dayOfMonth
            var chunkBackup = chunksToSave[day]
            if (chunkBackup == null)
            {
                i("chunkBackup == null")
                chunkBackup = JournalChunk(tableModel.columnHeaderContent[col].date, getGroupId())
                chunksToSave[day] = chunkBackup
            }
            else
            {
                i("chunkBackup != null")
                cancelSavingJob(col)
            }

            for (row in 0 until tableModel.rowHeaderContent.size)
            {
                chunkBackup.content[ChunkPersonName(tableModel.rowHeaderContent[row].person)] = CellData.getCopy(tableModel.cellsContent[row][col].data)
            }
            i("update backuped chunk")


            changingChunkJobs[col] = GlobalScope.launch (Dispatchers.IO + changingChunkSupervisor)
            {
                i("changingChunkJob launched")
                try
                {
                    withTimeout(7.seconds())
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
                        tableModel.cellsContent[row][col].data = chunkBackup.content[ChunkPersonName(tableModel.rowHeaderContent[row].person)]
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


        private fun isClickedTodayColumn(col: Int): Boolean = tableModel.columnHeaderContent[col].date == LocalDate.now()

        private fun Int.seconds(): Long
        {
            return this.toLong() * 1000
        }
    }
}