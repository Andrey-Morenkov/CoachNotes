package ru.hryasch.coachnotes.people.presenters.impl

import com.pawegio.kandroid.i
import kotlinx.coroutines.*
import moxy.InjectViewState
import moxy.MvpPresenter
import org.koin.core.KoinComponent
import org.koin.core.inject
import ru.hryasch.coachnotes.domain.person.data.Person
import ru.hryasch.coachnotes.domain.person.data.PersonImpl
import ru.hryasch.coachnotes.domain.person.interactors.PersonInteractor
import ru.hryasch.coachnotes.fragments.PersonView
import ru.hryasch.coachnotes.people.presenters.PersonPresenter

@InjectViewState
class PersonPresenterImpl: MvpPresenter<PersonView>(), PersonPresenter, KoinComponent
{
    private val peopleInteractor: PersonInteractor by inject()

    private var currentPerson: Person? = null

    init
    {
        i("onCreate PersonPresenterImpl")
        viewState.loadingState()
    }

    override fun applyInitialArgumentPersonAsync(person: Person?)
    {
        if (currentPerson != null)
        {
            return
        }

        applyPersonDataAsync(person)
    }

    override fun applyPersonDataAsync(person: Person?)
    {
        GlobalScope.launch(Dispatchers.Default)
        {
            currentPerson = person ?: PersonImpl("", "", id = peopleInteractor.getMaxPersonId() + 1)
            val groups = peopleInteractor.getGroupNames()

            withContext(Dispatchers.Main)
            {
                viewState.setPersonData(currentPerson!!, groups)
            }
        }
    }
}