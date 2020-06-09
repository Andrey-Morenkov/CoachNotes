package ru.hryasch.coachnotes.di

import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.koin.core.qualifier.named
import org.koin.dsl.module

import ru.hryasch.coachnotes.domain.group.interactors.GroupInteractor
import ru.hryasch.coachnotes.domain.group.interactors.impl.GroupInteractorImpl
import ru.hryasch.coachnotes.domain.home.HomeInteractor
import ru.hryasch.coachnotes.domain.home.impl.HomeInteractorImpl
import ru.hryasch.coachnotes.domain.journal.interactors.JournalInteractor
import ru.hryasch.coachnotes.domain.journal.interactors.impl.JournalInteractorImpl
import ru.hryasch.coachnotes.domain.person.interactors.PersonInteractor
import ru.hryasch.coachnotes.domain.person.interactors.impl.PersonInteractorImpl
import ru.hryasch.coachnotes.domain.repository.GroupRepository
import ru.hryasch.coachnotes.domain.repository.JournalRepository
import ru.hryasch.coachnotes.domain.repository.PersonRepository
import ru.hryasch.coachnotes.repository.group.GroupFakeRepositoryImpl
import ru.hryasch.coachnotes.repository.group.GroupRepositoryImpl
import ru.hryasch.coachnotes.repository.journal.JournalFakeRepositoryImpl
import ru.hryasch.coachnotes.repository.journal.JournalRepositoryImpl
import ru.hryasch.coachnotes.repository.person.PersonFakeRepositoryImpl
import ru.hryasch.coachnotes.repository.person.PersonRepositoryImpl


@ExperimentalCoroutinesApi
val appModule = module {

    single { JournalInteractorImpl() as JournalInteractor }
    single { HomeInteractorImpl() as HomeInteractor }
    single { GroupInteractorImpl() as GroupInteractor }
    single { PersonInteractorImpl() as PersonInteractor }

    single(named("mock")) { JournalFakeRepositoryImpl() as JournalRepository }
    single(named("mock")) { PersonFakeRepositoryImpl() as PersonRepository }
    single(named("mock")) { GroupFakeRepositoryImpl()  as GroupRepository }
    single(named("release")) { JournalRepositoryImpl() as JournalRepository }
    single(named("release")) { PersonRepositoryImpl() as PersonRepository }
    single(named("release")) { GroupRepositoryImpl()  as GroupRepository }

    single(named("months_RU")) { arrayOf("Январь", "Февраль", "Март", "Апрель", "Май", "Июнь", "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь") }
    single(named("daysOfWeek_RU")) { arrayOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс") }
    single(named("daysOfWeekLong_RU")) { arrayOf("Понедельник", "Вторник", "Среда", "Четверг", "Пятница", "Суббота", "Воскресенье") }
}