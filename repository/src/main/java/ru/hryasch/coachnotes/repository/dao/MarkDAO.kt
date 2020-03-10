package ru.hryasch.coachnotes.repository.dao

sealed class JournalMarkDAO()
{
    companion object
    {
        fun deserialize(str: String): JournalMarkDAO?
        {
            val options = str.split(" ")
            return when(options[0])
            {
                "p" -> JournalMarkPresenceDAO()
                "a" ->
                {
                    if (options.size == 1)
                    {
                        JournalMarkAbsenceDAO()
                    }
                    else
                    {
                        JournalMarkAbsenceDAO(options[1])
                    }
                }
                else -> null
            }
        }
    }

    open fun serialize() = ""
}

class JournalMarkPresenceDAO(): JournalMarkDAO()
{
    override fun toString(): String = "p"
    override fun serialize() = toString()
}
class JournalMarkAbsenceDAO(val mark: String? = null): JournalMarkDAO()
{
    override fun toString(): String
    {
        var str = "a"
        mark?.let { str += " $mark" }
        return str
    }

    override fun serialize() = toString()
}