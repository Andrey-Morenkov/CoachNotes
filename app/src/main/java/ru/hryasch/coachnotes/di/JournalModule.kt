package ru.hryasch.coachnotes.di

import android.content.Context
import android.view.View
import com.evrencoskun.tableview.listener.ITableViewListener
import org.koin.core.qualifier.named
import org.koin.dsl.module
import ru.hryasch.coachnotes.journal.Journal

import ru.hryasch.coachnotes.journal.table.MockTableModel
import ru.hryasch.coachnotes.journal.table.TableAdapter
import ru.hryasch.coachnotes.journal.table.TableModel
import ru.hryasch.coachnotes.journal.table.TableViewClickListener


val journalModule = module {

    factory(named("mock")) { MockTableModel() as TableModel }
    factory { TableModel() }

    factory { (context: Context, model: TableModel) -> TableAdapter(context, model) }
    factory { (adapter: TableAdapter) -> TableViewClickListener(adapter) as ITableViewListener }

    factory { (layout: View, context: Context) -> Journal(layout, context) }
}