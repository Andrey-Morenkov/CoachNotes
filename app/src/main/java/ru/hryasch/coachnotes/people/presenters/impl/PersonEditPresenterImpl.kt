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
import ru.hryasch.coachnotes.domain.common.GroupId
import ru.hryasch.coachnotes.domain.group.interactors.GroupInteractor
import ru.hryasch.coachnotes.domain.person.data.Person
import ru.hryasch.coachnotes.domain.person.data.PersonImpl
import ru.hryasch.coachnotes.domain.person.interactors.PersonInteractor
import ru.hryasch.coachnotes.domain.person.interactors.SimilarPersonFoundException
import ru.hryasch.coachnotes.fragments.PersonEditView
import ru.hryasch.coachnotes.people.presenters.PersonEditPresenter

@InjectViewState
class PersonEditPresenterImpl: MvpPresenter<PersonEditView>(), PersonEditPresenter, KoinComponent
{
    private val peopleInteractor: PersonInteractor by inject()
    private val groupInteractor: GroupInteractor by inject()

    private var originalPerson: Person? = null
    private var editingPerson: Person? = null

    init
    {
        viewState.loadingState()
    }

    override fun applyInitialArgumentPersonAsync(person: Person?, lockGroup: GroupId?)
    {
        if (editingPerson != null)
        {
            return
        }

        applyPersonDataAsync(person, lockGroup)
    }

    override fun applyPersonDataAsync(person: Person?, lockGroup: GroupId?)
    {
        GlobalScope.launch(Dispatchers.Default)
        {
            originalPerson = person
            editingPerson = originalPerson?.copy() ?: PersonImpl.generateNew()

            if (person == null && lockGroup != null)
            {
                editingPerson!!.groupId = lockGroup
            }
            val groups = groupInteractor.getGroupsList()

            withContext(Dispatchers.Main)
            {
                viewState.setPersonData(editingPerson!!, groups)
            }
        }
    }

    override fun updateOrCreatePerson()
    {
        i("updateOrCreatePerson: $editingPerson")

        if (editingPerson!!.surname == (originalPerson?.surname ?: ""))
        {
            // Surname wasn't changed, no need to check
            updateOrCreatePersonForced()
        }
        else
        {
            GlobalScope.launch(Dispatchers.Default)
            {
                try
                {
                    peopleInteractor.addOrUpdatePerson(editingPerson!!)
                    withContext(Dispatchers.Main)
                    {
                        originalPerson?.applyData(editingPerson!!)
                        viewState.updateOrCreatePersonFinished()
                    }
                }
                catch (e: SimilarPersonFoundException)
                {
                    withContext(Dispatchers.Main)
                    {
                        viewState.similarPersonFound(e.existPerson)
                    }
                }
            }
        }
    }

    override fun updateOrCreatePersonForced()
    {
        i("updateOrCreatePersonForced: $editingPerson")

        GlobalScope.launch(Dispatchers.Default)
        {
            peopleInteractor.addOrUpdatePersonForced(editingPerson!!)
            withContext(Dispatchers.Main)
            {
                originalPerson?.applyData(editingPerson!!)
                viewState.updateOrCreatePersonFinished()
            }
        }
    }

    override fun deletePerson(person: Person)
    {
        viewState.loadingState()

        GlobalScope.launch(Dispatchers.Default)
        {
            peopleInteractor.deletePerson(person)

            withContext(Dispatchers.Main)
            {
                viewState.deletePersonFinished()
            }
        }
    }
}