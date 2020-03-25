package ru.hryasch.coachnotes.di

import org.koin.core.qualifier.named
import org.koin.dsl.module
import ru.hryasch.coachnotes.domain.group.interactors.GroupInteractor
import ru.hryasch.coachnotes.domain.group.interactors.impl.GroupInteractorImpl
import ru.hryasch.coachnotes.domain.home.HomeInteractor
import ru.hryasch.coachnotes.domain.home.impl.HomeInteractorImpl

import ru.hryasch.coachnotes.domain.journal.interactors.JournalInteractor
import ru.hryasch.coachnotes.domain.journal.interactors.impl.JournalInteractorImpl


val appModule = module {

    single { JournalInteractorImpl() as JournalInteractor }
    single { HomeInteractorImpl() as HomeInteractor }
    single { GroupInteractorImpl() as GroupInteractor }

    single(named("months_RU")) { arrayOf("Январь", "Февраль", "Март", "Апрель", "Май", "Июнь", "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь") }
    single(named("daysOfWeek_RU")) { arrayOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс") }
    single(named("daysOfWeekLong_RU")) { arrayOf("Понедельник", "Вторник", "Среда", "Четверг", "Пятница", "Суббота", "Воскресенье") }
}