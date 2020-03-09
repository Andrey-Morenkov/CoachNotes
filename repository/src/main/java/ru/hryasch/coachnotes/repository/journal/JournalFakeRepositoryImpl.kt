package ru.hryasch.coachnotes.repository.journal

import com.pawegio.kandroid.i
import com.soywiz.klock.*
import io.realm.Realm
import io.realm.kotlin.where
import org.koin.core.KoinComponent
import org.koin.core.get
import org.koin.core.inject
import org.koin.core.qualifier.named
import ru.hryasch.coachnotes.domain.journal.data.JournalChunk
import ru.hryasch.coachnotes.domain.repository.JournalRepository
import ru.hryasch.coachnotes.repository.common.GroupId
import ru.hryasch.coachnotes.repository.converters.daoDateFormat
import ru.hryasch.coachnotes.repository.converters.fromDAO
import ru.hryasch.coachnotes.repository.dao.JournalChunkDAO
import ru.hryasch.coachnotes.repository.dao.JournalChunkDataDAO
import ru.hryasch.coachnotes.repository.dao.JournalMarkAbsence
import ru.hryasch.coachnotes.repository.dao.JournalMarkPresence



class JournalFakeRepositoryImpl: JournalRepository, KoinComponent
{
    init
    {
        generateJournalDb()
    }

    override suspend fun getJournalChunks(period: YearMonth,
                                          groupId: GroupId): List<JournalChunk>?
    {
        val db = getDb()

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
            chunkList.fromDAO()
        }
    }

    override suspend fun updateJournalChunk(chunk: JournalChunk)
    {
        val db = getDb()

        val ch = db.where<JournalChunkDAO>()
                   .equalTo("timestamp", chunk.date.format(daoDateFormat))
                   .equalTo("groupId", chunk.groupId)
                   .findFirst()

        ch?.data = chunk.content
    }




    private fun generateJournalDb()
    {
        val db = getDb()

        val chunk = JournalChunkDAO()
        chunk.timestamp = DateTime.now().format(daoDateFormat)

        i("timestamp = ${chunk.timestamp}")

        chunk.groupId = 1

        val data1 = JournalChunkDataDAO()
        data1.apply {
            name = "Имя1"
            surname = "Фамилия1"
            mark = JournalMarkPresence().toString()
        }

        val data2 = JournalChunkDataDAO()
        data2.apply {
            name = "Имя2"
            surname = "Фамилия2"
            mark = JournalMarkAbsence().toString()
        }

        val data3 = JournalChunkDataDAO()
        data3.apply {
            name = "Имя3"
            surname = "Фамилия3"
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

    private fun getDb(): Realm = Realm.getInstance(get(named("journal_storage_mock")))
}