package ru.hryasch.coachnotes.journal

import android.content.Context
import android.view.View
import com.evrencoskun.tableview.TableView
import org.koin.core.KoinComponent
import org.koin.core.get
import org.koin.core.inject
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
import ru.hryasch.coachnotes.R
import ru.hryasch.coachnotes.journal.table.TableAdapter
import ru.hryasch.coachnotes.journal.table.TableModel

class Journal(layout: View, context: Context): KoinComponent
{
    private val model:   TableModel by inject(named("mock"))
    private val view:    TableView = layout.findViewById(R.id.journalTable)
    private val adapter: TableAdapter

    init
    {
        adapter = get { parametersOf(context, model) }
        view.adapter = adapter
        view.tableViewListener = get { parametersOf(adapter) }

        adapter.renderTable()
    }
}