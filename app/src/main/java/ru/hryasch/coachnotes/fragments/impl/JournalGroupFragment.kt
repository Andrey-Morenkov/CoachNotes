package ru.hryasch.coachnotes.fragments.impl

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import com.evrencoskun.tableview.TableView
import com.pawegio.kandroid.i
import com.pawegio.kandroid.visible
import moxy.MvpAppCompatFragment
import moxy.presenter.InjectPresenter
import org.koin.android.ext.android.get
import org.koin.core.get
import org.koin.core.parameter.parametersOf
import ru.hryasch.coachnotes.R
import ru.hryasch.coachnotes.fragments.api.JournalView
import ru.hryasch.coachnotes.journal.table.TableAdapter
import ru.hryasch.coachnotes.journal.table.TableModel
import ru.hryasch.coachnotes.presenters.api.JournalPresenter
import ru.hryasch.coachnotes.presenters.impl.JournalPresenterImpl

class JournalGroupFragment : MvpAppCompatFragment(), JournalView
{
    @InjectPresenter
    lateinit var presenter: JournalPresenterImpl

    private lateinit var tableView: TableView
    private lateinit var tableAdapter: TableAdapter

    private lateinit var loadingSpinner: ProgressBar

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View?
    {
        val layout = inflater.inflate(R.layout.fragment_journal, container, false)

        loadingSpinner = layout.findViewById(R.id.journalProgressBar)
        tableView = layout.findViewById(R.id.journalTable)

        return layout
    }

    override fun waitingState()
    {
        i("-- Waiting State --")
        loadingSpinner.visible = true
        tableView.visible = false
    }

    override fun showingState(tableContent: TableModel)
    {
        i("-- Showing State --")
        tableAdapter = get { parametersOf(context, tableContent) }
        tableView.adapter = tableAdapter
        tableView.tableViewListener = get { parametersOf(presenter) }
        tableAdapter.renderTable()

        loadingSpinner.visible = false
        tableView.visible = true
    }

    override fun refreshData()
    {
        if (this::tableAdapter.isInitialized)
        {
            tableAdapter.notifyDataSetChanged()
        }
    }
}