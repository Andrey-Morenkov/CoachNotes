package ru.hryasch.coachnotes.di

import android.content.Context
import com.evrencoskun.tableview.listener.ITableViewListener
import org.koin.core.qualifier.named
import org.koin.dsl.module

import ru.hryasch.coachnotes.journal.MockTableModel
import ru.hryasch.coachnotes.journal.TableAdapter
import ru.hryasch.coachnotes.journal.TableModel
import ru.hryasch.coachnotes.journal.TableViewClickListener


val journalModule = module {

    factory(named("mock")) { MockTableModel() as TableModel }
    factory { TableModel() }

    factory { (context: Context) ->  TableAdapter(context) }
    factory { TableViewClickListener() as ITableViewListener }
}