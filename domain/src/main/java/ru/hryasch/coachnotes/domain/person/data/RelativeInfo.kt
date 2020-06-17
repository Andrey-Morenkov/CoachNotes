package ru.hryasch.coachnotes.domain.person.data

import java.util.*

enum class ParentType(val type: String)
{
    Mother("M"),
    Father("F"),
    Aunt("A"),
    Uncle("U"),
    GrandMother("GrM"),
    GrandFather("GrF"),
    Sister("S"),
    Brother("B")
}

class RelativeInfo
{
    private var phones: MutableList<String> = Collections.emptyList()
    var name: String = ""
    var type: ParentType = ParentType.Mother

    fun addPhone(phone: String)
    {
        phones.add(phone)
    }

    fun removePhone(phone: String)
    {
        phones.remove(phone)
    }

    fun removeAllPhones()
    {
        phones.clear()
    }

    fun getPhones(): List<String>
    {
        return phones
    }

    override fun toString(): String
    {
        return "$name ${type.type} $phones"
    }
}