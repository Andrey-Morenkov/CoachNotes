package ru.hryasch.coachnotes.di

import android.content.Context
import org.koin.core.qualifier.named
import org.koin.dsl.module

import ru.hryasch.coachnotes.journal.MockTableModel
import ru.hryasch.coachnotes.journal.TableAdapter
import ru.hryasch.coachnotes.journal.TableModel


val journalModule = module {

    factory(named("mock")) { MockTableModel() as TableModel }
    factory { TableModel() }

    factory { (context: Context) ->  TableAdapter(context) }

}