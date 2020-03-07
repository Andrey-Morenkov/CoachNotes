package ru.hryasch.coachnotes.repository.journal.impl

import com.pawegio.kandroid.i
import com.soywiz.klock.*
import io.realm.Realm
import io.realm.kotlin.where
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.koin.core.qualifier.named
import ru.hryasch.coachnotes.repository.common.GroupId
import ru.hryasch.coachnotes.repository.dao.JournalChunkDAO
import ru.hryasch.coachnotes.repository.dao.JournalChunkDataDAO
import ru.hryasch.coachnotes.repository.dao.JournalMarkAbsence
import ru.hryasch.coachnotes.repository.dao.JournalMarkPresence
import ru.hryasch.coachnotes.repository.journal.JournalRepository

val daoDateFormat = DateFormat("dd/MM/yyyy")

class JournalFakeRepository: JournalRepository, KoinComponent
{
    private val db: Realm by inject(named("journal_storage_mock"))

    init
    {
        generateJournalDb()
    }

    override suspend fun getJournalChunks(period: YearMonth,
                                          groupId: GroupId): List<JournalChunkDAO>?
    {
        val firstDate = DateTime.invoke(period.year, period.month, 1)

        val chunkList: MutableList<JournalChunkDAO> = ArrayList()

        val searchingRange = firstDate until (firstDate + 1.months)
        var currentDate = firstDate
        while (currentDate in searchingRange)
        {
            val chunk = db.where<JournalChunkDAO>()
                .equalTo("timestamp", currentDate.format(daoDateFormat))
                .equalTo("groupId", 1.toInt())
                .findFirst()

            chunk?.let { chunkList.add(it) }
            currentDate += 1.days
        }
        i("chunkListSize = ${chunkList.size}")
        return if (chunkList.size == 0)
        {
            null
        }
        else
        {
            chunkList
        }
    }

    private fun generateJournalDb()
    {
        val chunk = JournalChunkDAO()
        chunk.timestamp = DateTime.now().format(daoDateFormat)

        i("timestamp = ${chunk.timestamp}")

        chunk.groupId = 1

        val data1 = JournalChunkDataDAO()
        data1.apply {
            name = "Вася 1"
            mark = JournalMarkPresence().toString()
        }

        val data2 = JournalChunkDataDAO()
        data2.apply {
            name = "Вася 2"
            mark = JournalMarkAbsence().toString()
        }

        val data3 = JournalChunkDataDAO()
        data3.apply {
            name = "Вася 3"
            mark = JournalMarkAbsence("Б").toString()
        }

        chunk.data.apply {
            add(data1)
            add(data2)
            add(data3)
        }

        db.executeTransaction {
            it.copyToRealm(chunk)
        }
    }
}