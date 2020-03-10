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
import ru.hryasch.coachnotes.domain.journal.data.AbsenceData
import ru.hryasch.coachnotes.domain.journal.data.JournalChunkPersonName
import ru.hryasch.coachnotes.domain.journal.data.PresenceData
import ru.hryasch.coachnotes.domain.journal.interactors.JournalInteractor
import ru.hryasch.coachnotes.domain.person.PersonImpl
import ru.hryasch.coachnotes.journal.table.TableModel
import ru.hryasch.coachnotes.journal.presenters.JournalPresenter
import java.util.*

@InjectViewState
class JournalPresenterImpl: MvpPresenter<JournalView>(), JournalPresenter, KoinComponent
{
    private val journalInteractor: JournalInteractor by inject()
    private val monthNames:Array<String> by inject(named("months_RU"))

    private lateinit var tableModel: TableModel

    private lateinit var findingTableJob: Job
    private val changingJobs: MutableMap<String, Job> = TreeMap()

    private var chosenPeriod: YearMonth = DateTime.now().yearMonth

    init
    {
        changePeriod()
    }

    override fun onCellClicked(col: Int, row: Int)
    {
        i("onCell($col:$row) clicked")

        val cell = tableModel.cellContent[row][col]

        cell.data =
                when (cell.data)
                {
                    is PresenceData -> AbsenceData()
                    is AbsenceData -> null
                    else -> PresenceData()
                }

        val currJob = changingJobs["$col|$row"]
        if (currJob != null && currJob.isActive)
        {
            i("cancelled")
            currJob.cancel()
        }

        changingJobs["$col|$row"] = GlobalScope.launch(Dispatchers.IO)
        {
            i("waiting for update...")
            delay(5000)
            i("let's save")
            val person = PersonImpl(tableModel.rowHeaderContent[row].data.person.surname,
                tableModel.rowHeaderContent[row].data.person.name)

            journalInteractor.saveChangedCell(tableModel.columnHeaderContent[col].data.timestamp,
                person,
                tableModel.cellContent[row][col].data,
                tableModel.groupId)
        }

        viewState.refreshData()
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

        findingTableJob = GlobalScope.launch(Dispatchers.IO)
        {
            tableModel = journalInteractor
                            .getJournal(chosenPeriod, 1)
                            .toModel()

            withContext(Dispatchers.Main)
            {
                viewState.showingState(tableModel)
                viewState.setPeriod(month, year)
            }
        }
    }

    private fun changePeriod()
    {
        changePeriod(monthNames[chosenPeriod.month.index0], chosenPeriod.year.year)
    }

}