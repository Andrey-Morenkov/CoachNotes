package ru.hryasch.coachnotes.repository.journal

import com.pawegio.kandroid.e
import com.pawegio.kandroid.i
import com.soywiz.klock.*
import io.realm.Realm
import io.realm.kotlin.where
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.koin.core.KoinComponent
import org.koin.core.get
import org.koin.core.qualifier.named
import ru.hryasch.coachnotes.domain.common.GroupId
import ru.hryasch.coachnotes.domain.journal.data.JournalChunk
import ru.hryasch.coachnotes.domain.journal.data.NoExistData
import ru.hryasch.coachnotes.domain.repository.JournalRepository
import ru.hryasch.coachnotes.repository.converters.daoDateFormat
import ru.hryasch.coachnotes.repository.converters.fromDAO
import ru.hryasch.coachnotes.repository.dao.JournalChunkDAO
import ru.hryasch.coachnotes.repository.dao.JournalChunkDAOId
import ru.hryasch.coachnotes.repository.dao.JournalChunkDataDAO
import java.util.concurrent.Executors

class JournalRepositoryImpl: JournalRepository, KoinComponent
{
    private lateinit var db: Realm
    private val dbContext = Executors.newSingleThreadExecutor().asCoroutineDispatcher()

    init
    {
        runBlocking(dbContext)
        {
            db = Realm.getInstance(get(named("journal_storage")))
        }
    }

    override suspend fun getJournalChunks(period: YearMonth,
                                          groupId: GroupId): List<JournalChunk>?
    {
        val chunkList: MutableList<JournalChunkDAO> = ArrayList()
        val firstDayOfMonth = DateTime.invoke(period.year, period.month, 1)
        var currentDate = firstDayOfMonth

        while (currentDate in (firstDayOfMonth until (firstDayOfMonth + 1.months)))
        {
            withContext(dbContext)
            {
                val chunk = getChunk(currentDate.date, groupId)
                if (chunk != null)
                {
                    e("getJournalChunks: date: ${daoDateFormat.format(currentDate)} chunk = $chunk")
                    chunkList.add(chunk)
                }
            }

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

    override suspend fun updateJournalChunk(chunk: JournalChunk)
    {
        i("updateJournalChunk: date = ${chunk.date.format(daoDateFormat)}, groupId = ${chunk.groupId}\n")

        withContext(dbContext)
        {
            db.executeTransaction {
                val daoChunk = getOrCreateChunk(chunk.date, chunk.groupId)
                daoChunk.data.clear()

                chunk.content.forEach {
                    if (it.value != null && it.value !is NoExistData)
                    {
                        daoChunk.data.add(JournalChunkDataDAO(it.key, it.value!!))
                    }
                }

                if (daoChunk.isEmpty())
                {
                    i("delete chunk")
                    deleteChunk(daoChunk)
                }
                else
                {
                    i("create or update chunk")
                    it.copyToRealmOrUpdate(daoChunk)
                }
            }
        }
    }

    override suspend fun deleteAllJournalsByGroup(groupId: GroupId)
    {
        withContext(dbContext)
        {
            db.executeTransaction {
                it.where<JournalChunkDAO>().findAll().forEach { chunk ->
                    val chunkGroupId = JournalChunkDAOId.deserialize(chunk.id!!).groupId
                    if (chunkGroupId == groupId)
                    {
                        chunk.deleteFromRealm()
                    }
                }
            }
        }
    }

    override suspend fun closeDb()
    {
        db.close()
    }


    private fun getChunk(date: Date, groupId: GroupId): JournalChunkDAO?
    {
        val obj = db.where<JournalChunkDAO>()
                    .equalTo("id", JournalChunkDAOId.getSerialized(date, groupId))
                    .findFirst()

        return obj?.run { db.copyFromRealm(obj) }
    }

    private fun getOrCreateChunk(date: Date, groupId: GroupId): JournalChunkDAO
    {
        return getChunk(date, groupId) ?: JournalChunkDAO(date, groupId)
    }

    private fun deleteChunk(chunk: JournalChunkDAO)
    {
        db.where<JournalChunkDAO>()
          .equalTo("id", chunk.id)
          .findFirst()
          ?.deleteFromRealm()
    }
}