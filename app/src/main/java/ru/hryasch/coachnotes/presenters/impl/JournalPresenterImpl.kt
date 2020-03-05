package ru.hryasch.coachnotes.presenters.impl

import com.pawegio.kandroid.e
import com.pawegio.kandroid.i
import kotlinx.coroutines.*
import moxy.InjectViewState
import moxy.MvpPresenter
import org.koin.core.KoinComponent
import org.koin.core.get
import org.koin.core.qualifier.named
import ru.hryasch.coachnotes.fragments.api.JournalView
import ru.hryasch.coachnotes.journal.table.AbsenceData
import ru.hryasch.coachnotes.journal.table.MockTableModel
import ru.hryasch.coachnotes.journal.table.PresenceData
import ru.hryasch.coachnotes.journal.table.TableModel
import ru.hryasch.coachnotes.presenters.api.JournalPresenter

@InjectViewState
class JournalPresenterImpl: MvpPresenter<JournalView>(), JournalPresenter, KoinComponent
{
    private lateinit var tableModel: TableModel

    init
    {
        viewState.waitingState()

        GlobalScope.launch(Dispatchers.IO) {

            tableModel = get(named("mock"))

            withContext(Dispatchers.Main)
            {
                viewState.showingState(tableModel)
            }
        }
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
}