package ru.hryasch.coachnotes.di

import android.content.Context
import com.evrencoskun.tableview.listener.ITableViewListener
import org.koin.dsl.module
import ru.hryasch.coachnotes.journal.presenters.JournalPresenter
import ru.hryasch.coachnotes.journal.table.TableAdapter
import ru.hryasch.coachnotes.journal.table.data.TableModel
import ru.hryasch.coachnotes.journal.table.TableViewClickListener

val journalModule = module {

    factory { (context: Context, model: TableModel) -> TableAdapter(context, model) }
    factory { (presenter: JournalPresenter) -> TableViewClickListener(presenter) as ITableViewListener }
}