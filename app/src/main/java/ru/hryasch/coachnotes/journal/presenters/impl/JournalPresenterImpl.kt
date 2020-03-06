package ru.hryasch.coachnotes.journal.presenters.impl

import com.pawegio.kandroid.i
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
import ru.hryasch.coachnotes.domain.journal.data.PresenceData
import ru.hryasch.coachnotes.domain.journal.interactors.JournalInteractor
import ru.hryasch.coachnotes.journal.table.TableModel
import ru.hryasch.coachnotes.journal.presenters.JournalPresenter

@InjectViewState
class JournalPresenterImpl: MvpPresenter<JournalView>(), JournalPresenter, KoinComponent
{
    private val journalInteractor: JournalInteractor by inject()
    private val monthNames:Array<String> by inject(named("months_RU"))

    private lateinit var tableModel: TableModel
    private lateinit var findingTableJob: Job

    private var chosenPeriod: YearMonth = DateTime.now().yearMonth

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
                            .getJournal(chosenPeriod, 123)
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