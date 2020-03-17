package ru.hryasch.coachnotes.repository.journal

import com.pawegio.kandroid.d
import com.pawegio.kandroid.e
import com.pawegio.kandroid.i
import com.soywiz.klock.*
import com.soywiz.klock.Date
import io.realm.Realm
import io.realm.kotlin.where
import kotlinx.coroutines.*
import org.koin.core.KoinComponent
import org.koin.core.get
import org.koin.core.inject
import org.koin.core.qualifier.named
import ru.hryasch.coachnotes.domain.journal.data.*
import ru.hryasch.coachnotes.domain.person.Person
import ru.hryasch.coachnotes.domain.repository.JournalRepository
import ru.hryasch.coachnotes.domain.repository.PersonRepository
import ru.hryasch.coachnotes.repository.common.GroupId
import ru.hryasch.coachnotes.repository.converters.daoDateFormat
import ru.hryasch.coachnotes.repository.converters.fromDAO
import ru.hryasch.coachnotes.repository.converters.toDAO
import ru.hryasch.coachnotes.repository.dao.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.random.Random
import kotlin.random.nextInt


class JournalFakeRepositoryImpl: JournalRepository, KoinComponent
{
    private val personRepo: PersonRepository by inject(named("mock"))
    private var initializingJob: Job

    init
    {
        d("JournalFakeRepositoryImpl INIT START")

        initializingJob = GlobalScope.launch(Dispatchers.Default)
        {
            generateJournalDb()
            d("JournalFakeRepositoryImpl INIT FINISH")
        }
    }

    override suspend fun getJournalChunks(period: YearMonth,
                                          groupId: GroupId): List<JournalChunk>?
    {
        if (initializingJob.isActive) { initializingJob.join() }

        val db = getDb()
        db.refresh()

        val chunkList: MutableList<JournalChunkDAO> = ArrayList()

        val firstDayOfMonth = DateTime.invoke(period.year, period.month, 1)
        var currentDate = firstDayOfMonth

        while (currentDate in (firstDayOfMonth until (firstDayOfMonth + 1.months)))
        {
            db.refresh()
            val chunk = getChunk(db, currentDate.date, 1)

            if (chunk != null) e("date: ${daoDateFormat.format(currentDate)} chunk = $chunk")

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

    override suspend fun updateJournalChunk(chunk: JournalChunk)
    {
        val db = getDb()
        db.refresh()

        i("updateJournalChunk: \n" +
               "date    = ${chunk.date.format(daoDateFormat)} \n" +
               "groupId = ${chunk.groupId}\n")

        db.executeTransaction {
            val daoChunk = getOrCreateChunk(db, chunk.date, chunk.groupId)
            daoChunk.data.clear()

            chunk.content.forEach {
                if (it.value != null && it.value !is NoExistData)
                {
                    daoChunk.data.add(JournalChunkDataDAO(it.key, it.value!!))
                }
            }

            if (daoChunk.isEmpty())
            {
                e("delete chunk")
                deleteChunk(db, daoChunk)
            }
            else
            {
                e("create or update chunk")
                createOrUpdateChunk(db, daoChunk)
            }
        }

        i("SAVED or DELETED")
    }


    private suspend fun generateJournalDb()
    {
        val executionDays = generateExecutionDays().sorted()
        val personsList = personRepo.getPersonsByGroup(1)!!

        val chunkList: MutableList<JournalChunkDAO> = LinkedList()

        for (exeDay in executionDays)
        {
            val chunk = JournalChunkDAO(exeDay, 1)
            for (person in personsList)
            {
                chunk.data.add(JournalChunkDataDAO(person.surname, person.name, getRandomCellData()))
            }
            chunkList.add(chunk)
        }

        chunkList.forEach { chunk ->
            getDb().executeTransaction {
                it.copyToRealm(chunk)
            }
        }
    }

    private fun getRandomCellData(): CellData
    {
        return when(Random.nextInt(0..10))
        {
            in 0..1 -> AbsenceData("Б")
            in 1..3 -> AbsenceData()
            else -> PresenceData()
        }
    }

    private fun generateExecutionDays(): List<Date>
    {
        val executionsDays: MutableList<Date> = LinkedList()
        val executionsCount = Random.nextInt(5..9)

        val today = DateTimeTz.nowLocal()
        for (i in 1..executionsCount)
        {
            var exeDay: Date
            do
            {
                exeDay = Date.invoke(today.year, today.month, Random.nextInt(1..today.yearMonth.days))
            }
            while (executionsDays.find { it == exeDay } != null)
            d("generated execution date: ${exeDay.format("dd/MM/yyyy")}")
            executionsDays.add(exeDay)
        }

        return executionsDays
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