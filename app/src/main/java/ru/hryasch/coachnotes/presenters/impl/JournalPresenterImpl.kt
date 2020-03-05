package ru.hryasch.coachnotes.presenters.impl

import com.pawegio.kandroid.i
import com.soywiz.klock.DateTime
import com.soywiz.klock.months
import kotlinx.coroutines.*
import moxy.InjectViewState
import moxy.MvpPresenter
import org.koin.core.KoinComponent
import org.koin.core.get
import org.koin.core.qualifier.named

import ru.hryasch.coachnotes.fragments.api.JournalView
import ru.hryasch.coachnotes.journal.table.AbsenceData
import ru.hryasch.coachnotes.journal.table.PresenceData
import ru.hryasch.coachnotes.journal.table.TableModel
import ru.hryasch.coachnotes.presenters.api.JournalPresenter

@InjectViewState
class JournalPresenterImpl: MvpPresenter<JournalView>(), JournalPresenter, KoinComponent
{
    private lateinit var tableModel: TableModel

    private val monthNames:Array<String> = get(named("months"))
    private var chosenPeriod: DateTime = DateTime.now()

    private lateinit var findingTableJob: Job

    init
    {
        changePeriod()
    }

    override fun test(col: Int, row: Int)
    {
        i("-- TEST IN PRESENTER --")

        with (tableModel.cellContent[row][col])
        {
            data =
                when (data)
                {
                    is PresenceData -> AbsenceData()
                    is AbsenceData -> null
                    else -> PresenceData()
                }
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

    private fun changePeriod()
    {
        changePeriod(monthNames[chosenPeriod.month0], chosenPeriod.year.year)
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

        findingTableJob = GlobalScope.launch(Dispatchers.IO) {

            tableModel = get(named("mock"))

            withContext(Dispatchers.Main)
            {
                viewState.showingState(tableModel)
                viewState.setPeriod(month, year)
            }
        }
    }
}