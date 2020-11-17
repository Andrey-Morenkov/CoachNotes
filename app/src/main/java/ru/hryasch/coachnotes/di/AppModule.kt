package ru.hryasch.coachnotes.di

import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.koin.core.qualifier.named
import org.koin.dsl.module
import ru.hryasch.coachnotes.R
import ru.hryasch.coachnotes.application.App
import ru.hryasch.coachnotes.repository.global.GlobalSettings

import ru.hryasch.coachnotes.domain.group.interactors.GroupInteractor
import ru.hryasch.coachnotes.domain.group.interactors.impl.GroupInteractorImpl
import ru.hryasch.coachnotes.domain.home.HomeInteractor
import ru.hryasch.coachnotes.domain.home.impl.HomeInteractorImpl
import ru.hryasch.coachnotes.domain.journal.interactors.JournalInteractor
import ru.hryasch.coachnotes.domain.journal.interactors.impl.JournalInteractorImpl
import ru.hryasch.coachnotes.domain.person.data.ParentType
import ru.hryasch.coachnotes.domain.person.interactors.PersonInteractor
import ru.hryasch.coachnotes.domain.person.interactors.impl.PersonInteractorImpl
import ru.hryasch.coachnotes.domain.repository.GroupRepository
import ru.hryasch.coachnotes.domain.repository.JournalRepository
import ru.hryasch.coachnotes.domain.repository.PersonRepository
import ru.hryasch.coachnotes.repository.group.GroupRepositoryImpl
import ru.hryasch.coachnotes.repository.journal.JournalRepositoryImpl
import ru.hryasch.coachnotes.repository.person.PersonRepositoryImpl


@ExperimentalCoroutinesApi
val appModule = module {
    single { JournalInteractorImpl() as JournalInteractor }
    single { HomeInteractorImpl() as HomeInteractor }
    single { GroupInteractorImpl() as GroupInteractor }
    single { PersonInteractorImpl() as PersonInteractor }

    single(named("release")) { JournalRepositoryImpl() as JournalRepository }
    single(named("release")) { PersonRepositoryImpl() as PersonRepository }
    single(named("release")) { GroupRepositoryImpl()  as GroupRepository }

    single(named("months_RU")) { arrayOf("Январь", "Февраль", "Март", "Апрель", "Май", "Июнь", "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь") }
    single(named("daysOfWeek_RU")) { arrayOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс") }
    single(named("daysOfWeekLong_RU")) { arrayOf("Понедельник", "Вторник", "Среда", "Четверг", "Пятница", "Суббота", "Воскресенье") }
    single(named("relatives_RU")) { arrayOf(App.getCtx().getString(R.string.mother), App.getCtx().getString(R.string.father), App.getCtx().getString(R.string.grandMa), App.getCtx().getString(R.string.grandFa), App.getCtx().getString(R.string.aunt), App.getCtx().getString(R.string.uncle), App.getCtx().getString(R.string.sister), App.getCtx().getString(R.string.brother))}
    single(named("coachRoles")) { listOf(App.getCtx().getString(R.string.coach_role_common_coach), App.getCtx().getString(R.string.coach_role_choreographer_coach), App.getCtx().getString(R.string.coach_role_sport_master_coach_), App.getCtx().getString(R.string.coach_role_custom)) }

    single { GlobalSettings }

    factory(named("getRelativeName")) { (parentType: ParentType) ->
        when(parentType)
        {
            ParentType.Mother -> App.getCtx().getString(R.string.mother)
            ParentType.Father -> App.getCtx().getString(R.string.father)
            ParentType.GrandMother -> App.getCtx().getString(R.string.grandMa)
            ParentType.GrandFather -> App.getCtx().getString(R.string.grandFa)
            ParentType.Aunt -> App.getCtx().getString(R.string.aunt)
            ParentType.Uncle -> App.getCtx().getString(R.string.uncle)
            ParentType.Brother -> App.getCtx().getString(R.string.brother)
            ParentType.Sister -> App.getCtx().getString(R.string.sister)
            else -> App.getCtx().getString(R.string.mother)
        }
    }
}