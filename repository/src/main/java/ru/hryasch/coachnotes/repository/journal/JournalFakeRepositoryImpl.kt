package ru.hryasch.coachnotes.repository.journal

import com.pawegio.kandroid.e
import com.pawegio.kandroid.i
import com.soywiz.klock.*
import io.realm.Realm
import io.realm.Realm.Transaction.Callback
import io.realm.kotlin.where
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import org.koin.core.KoinComponent
import org.koin.core.get
import org.koin.core.qualifier.named
import ru.hryasch.coachnotes.domain.journal.data.CellData
import ru.hryasch.coachnotes.domain.journal.data.JournalChunk
import ru.hryasch.coachnotes.domain.journal.data.JournalChunkPersonName
import ru.hryasch.coachnotes.domain.person.Person
import ru.hryasch.coachnotes.domain.repository.JournalRepository
import ru.hryasch.coachnotes.repository.common.GroupId
import ru.hryasch.coachnotes.repository.converters.daoDateFormat
import ru.hryasch.coachnotes.repository.converters.fromDAO
import ru.hryasch.coachnotes.repository.converters.toDAO
import ru.hryasch.coachnotes.repository.dao.*
import java.util.concurrent.Executors


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

        val chunkList: MutableList<JournalChunkDAO> = ArrayList()

        val firstDayOfMonth = DateTime.invoke(period.year, period.month, 1)
        var currentDate = firstDayOfMonth

        while (currentDate in (firstDayOfMonth until (firstDayOfMonth + 1.months)))
        {
            db.refresh()
            val chunk = getChunk(db, currentDate.date, 1)

            if (chunk != null) e("date: ${daoDateFormat.format(currentDate)}} chunk = $chunk")

            chunk?.let { chunkList.add(it) }
            currentDate += 1.days
        }

        return if (chunkList.isEmpty())
        {
            null
        }
        else
        {
            chunkList.fromDAO()
        }
    }

    override suspend fun updateJournalChunkData(date: Date,
                                                groupId: GroupId,
                                                person: Person,
                                                mark: CellData?)
    {
        val db = getDb()
        db.refresh()

        i("updateJournalChunkData: \n" +
               "date    = ${date.format(daoDateFormat)} \n" +
               "groupId = $groupId\n" +
               "person  = ${person.surname} ${person.name} (${person.id})\n" +
               "mark    = ${mark.toString()}")

        db.executeTransaction {
            val chunk = getOrCreateChunk(db, date, groupId)
            val personMarkInfo = chunk.data.find { it.surname == person.surname &&
                    it.name == person.name }

            if (mark == null)
            {
                e("mark == null")
                // delete info about person from chunk
                personMarkInfo?.let {
                    chunk.data.remove(it)
                    e("personMarkInfo != null")
                }
            }
            else
            {
                e("mark != null")
                // create chunk info about person or update
                if (personMarkInfo == null)
                {
                    e("personMarkInfo == null")
                    chunk.data.add(JournalChunkDataDAO(person.surname, person.name, mark))
                }
                else
                {
                    e("personMarkInfo != null")
                    personMarkInfo.mark = mark.toDAO().serialize()
                }
            }

            if (chunk.data.isEmpty())
            {
                e("delete chunk")
                deleteChunk(db, chunk)
            }
            else
            {
                e("create or update chunk")
                createOrUpdateChunk(db, chunk)
            }
        }

        i("SAVED")
    }




    private fun generateJournalDb()
    {
        val db = getDb()

        val chunk = JournalChunkDAO(DateTimeTz.nowLocal().local.date, 1)
        val data1 = JournalChunkDataDAO("Фамилия1", "Имя1", JournalMarkPresenceDAO())
        val data2 = JournalChunkDataDAO("Фамилия2", "Имя2", JournalMarkAbsenceDAO())
        val data3 = JournalChunkDataDAO("Фамилия3", "Имя3", JournalMarkAbsenceDAO("Б"))

        chunk.data.apply {
            add(data1)
            add(data2)
            add(data3)
        }

        e("generated chunk: id = ${chunk.id}")

        db.executeTransaction {
            it.copyToRealm(chunk)
        }
    }

    private fun getChunk(db: Realm, date: Date, groupId: GroupId): JournalChunkDAO?
    {
        val obj = db.where<JournalChunkDAO>()
                    .equalTo("id", JournalChunkDAOId.getSerialized(date, groupId))
                    .findFirst()

        return obj?.run { db.copyFromRealm(obj) }
    }

    private fun getChunk(db: Realm, chunk: JournalChunkDAO): JournalChunkDAO?
    {
        val obj = db.where<JournalChunkDAO>()
                    .equalTo("id", chunk.id)
                    .findFirst()

        return obj?.run { db.copyFromRealm(obj) }
    }

    private fun getOrCreateChunk(db: Realm, date: Date, groupId: GroupId): JournalChunkDAO
    {
        return getChunk(db, date, groupId) ?: JournalChunkDAO(date, groupId)
    }

    private fun deleteChunk(db: Realm, chunk: JournalChunkDAO)
    {
        db.where<JournalChunkDAO>()
          .equalTo("id", chunk.id)
          .findFirst()
          ?.deleteFromRealm()
    }

    private fun deleteChunk(db: Realm, date: Date, groupId: GroupId)
    {
        db.where<JournalChunkDAO>()
          .equalTo("id", JournalChunkDAOId.getSerialized(date, groupId))
          .findFirst()
          ?.deleteFromRealm()
    }

    private fun createOrUpdateChunk(db: Realm, chunk: JournalChunkDAO)
    {
        db.copyToRealmOrUpdate(chunk)
    }

    private fun getDb(): Realm = Realm.getInstance(get(named("journal_storage_mock")))
}