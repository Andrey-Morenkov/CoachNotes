package ru.hryasch.coachnotes.di

import android.content.Context
import com.evrencoskun.tableview.listener.ITableViewListener
import org.koin.core.qualifier.named
import org.koin.dsl.module

import ru.hryasch.coachnotes.domain.journal.interactors.JournalInteractor
import ru.hryasch.coachnotes.domain.journal.interactors.impl.JournalInteractorImpl
import ru.hryasch.coachnotes.journal.table.TableAdapter
import ru.hryasch.coachnotes.journal.table.TableModel
import ru.hryasch.coachnotes.journal.table.TableViewClickListener
import ru.hryasch.coachnotes.journal.presenters.JournalPresenter


val journalAppModule = module {

    single { JournalInteractorImpl() as JournalInteractor }

    factory { (context: Context, model: TableModel) -> TableAdapter(context, model) }
    factory { (presenter: JournalPresenter) -> TableViewClickListener(presenter) as ITableViewListener }

    single(named("months_RU")) { arrayOf("Январь", "Февраль", "Март", "Апрель", "Май", "Июнь", "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь") }
    single(named("daysOfWeek_RU")) { arrayOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс") }
    single(named("daysOfWeekLong_RU")) { arrayOf("Понедельник", "Вторник", "Среда", "Четверг", "Пятница", "Суббота", "Воскресенье") }
}