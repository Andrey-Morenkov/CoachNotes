package ru.hryasch.coachnotes.domain.person.data

import android.util.Log.d
import com.pawegio.kandroid.d
import org.koin.core.KoinComponent
import org.koin.core.get
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
import java.io.Serializable
import java.util.*

enum class ParentType(val type: String): KoinComponent
{
    Mother("M"),
    Father("F"),
    Aunt("A"),
    Uncle("U"),
    GrandMother("GrM"),
    GrandFather("GrF"),
    Sister("S"),
    Brother("B");

    companion object
    {
        fun getBySerializedName(name: String): ParentType
        {
            return when(name)
            {
                "M" -> Mother
                "F" -> Father
                "A" -> Aunt
                "U" -> Uncle
                "GrM" -> GrandMother
                "GrF" -> GrandFather
                "S" -> Sister
                "B" -> Brother
                else -> Mother
            }
        }
    }

    override fun toString(): String
    {
        return get(named("getRelativeName")) { parametersOf(this) }
    }
}

class RelativeInfo: Serializable
{
    private var phones: MutableList<String> = LinkedList()
    var name: String = ""
    var type: ParentType = ParentType.Mother

    fun addPhone(phone: String)
    {
        d("Relative($name): add phone $phone")
        phones.add(phone)
    }

    fun removePhone(phone: String)
    {
        phones.remove(phone)
    }

    fun removePhone(position: Int)
    {
        phones.removeAt(position)
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