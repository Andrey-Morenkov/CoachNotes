package ru.hryasch.coachnotes.fragments.impl

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.evrencoskun.tableview.TableView
import com.evrencoskun.tableview.adapter.AbstractTableAdapter
import moxy.MvpAppCompatFragment
import org.koin.android.ext.android.get
import org.koin.core.parameter.parametersOf
import ru.hryasch.coachnotes.R
import ru.hryasch.coachnotes.journal.TableAdapter

class HomeFragmentImpl : MvpAppCompatFragment()
{
    private lateinit var tableView: TableView
    private lateinit var tableAdapter: TableAdapter


    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View?
    {
        val view = inflater.inflate(R.layout.fragment_journal, container, false)

        tableAdapter = get { parametersOf(container!!.context) }

        tableView = view.findViewById(R.id.journalTable)
        tableView.adapter = tableAdapter
        tableView.tableViewListener = get()

        tableAdapter.renderTable()
        return view
    }
}