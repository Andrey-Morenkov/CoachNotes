package ru.hryasch.coachnotes.people.presenters.impl

import com.pawegio.kandroid.i
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import moxy.InjectViewState
import moxy.MvpPresenter
import org.koin.core.KoinComponent
import org.koin.core.inject
import ru.hryasch.coachnotes.domain.group.interactors.GroupInteractor
import ru.hryasch.coachnotes.domain.person.data.Person
import ru.hryasch.coachnotes.domain.person.data.PersonImpl
import ru.hryasch.coachnotes.domain.person.interactors.PersonInteractor
import ru.hryasch.coachnotes.fragments.PersonEditView
import ru.hryasch.coachnotes.people.presenters.PersonEditPresenter

@InjectViewState
class PersonEditPresenterImpl: MvpPresenter<PersonEditView>(), PersonEditPresenter, KoinComponent
{
    private val peopleInteractor: PersonInteractor by inject()
    private val groupInteractor: GroupInteractor by inject()

    private lateinit var currentPerson: Person

    init
    {
        viewState.loadingState()
    }

    override fun applyPersonDataAsync(person: Person?)
    {
        GlobalScope.launch(Dispatchers.Default)
        {
            currentPerson = person ?: PersonImpl("", "", id = peopleInteractor.getMaxPersonId() + 1)
            val groups = groupInteractor.getGroupsList()

            withContext(Dispatchers.Main)
            {
                viewState.setPersonData(currentPerson, groups)
            }
        }
    }

    override fun updateOrCreatePerson()
    {
        i("updateOrCreatePerson: $currentPerson")

        GlobalScope.launch(Dispatchers.Main)
        {
            peopleInteractor.addOrUpdatePerson(currentPerson)

            withContext(Dispatchers.Main)
            {
                viewState.updateOrCreatePersonFinished()
            }
        }
    }

    override fun onDeletePersonClicked()
    {
        viewState.showDeletePersonNotification(currentPerson)
    }

    override fun deletePerson(person: Person)
    {
        viewState.loadingState()

        GlobalScope.launch(Dispatchers.Main)
        {
            peopleInteractor.deletePerson(person)

            withContext(Dispatchers.Main)
            {
                viewState.deletePersonFinished()
            }
        }
    }
}