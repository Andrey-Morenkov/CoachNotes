package ru.hryasch.coachnotes.domain.entity

import ru.hryasch.coachnotes.domain.entity.common.Label

class Group(val id: String,
            val name: String,
            val availableAge: Byte = 0)
{
    private val labels: MutableList<Label>  = mutableListOf()
    private val people: MutableList<Person> = mutableListOf()

    fun addLabel(newLabel: Label)
    {
        labels.add(newLabel.copy())
    }

    fun getLabels(): List<Label>
    {
        return labels.toList()
    }

    fun removeLabel(existedLabel: Label)
    {
        labels.remove(existedLabel)
    }

    fun removeAllLabels()
    {
        labels.clear()
    }

    fun addPerson(newPerson: Person)
    {
        people.add(newPerson)
    }

    fun getPersons(): List<Person>
    {
        return people.toList()
    }

    fun removePerson(existedPerson: Person)
    {
        people.remove(existedPerson)
    }

    fun removeAllPersons()
    {
        people.clear()
    }
}