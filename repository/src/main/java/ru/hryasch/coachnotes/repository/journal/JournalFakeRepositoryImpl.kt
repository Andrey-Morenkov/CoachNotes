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
import ru.hryasch.coachnotes.domain.person.data.Person
import ru.hryasch.coachnotes.domain.repository.JournalRepository
import ru.hryasch.coachnotes.domain.repository.PersonRepository
import ru.hryasch.coachnotes.repository.common.GroupId
import ru.hryasch.coachnotes.repository.converters.daoDateFormat
import ru.hryasch.coachnotes.repository.converters.fromDAO
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
        initializingJob = GlobalScope.launch(Dispatchers.Default)
        {
            generateJournalDb()
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
            val chunk = getChunk(db, currentDate.date, groupId)

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

        i("updateJournalChunk: date = ${chunk.date.format(daoDateFormat)}, groupId = ${chunk.groupId}\n")

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

    override suspend fun deleteAllJournalsByGroup(groupId: GroupId)
    {
        val db = getDb()
        db.refresh()

        db.executeTransaction {
            db.where<JournalChunkDAO>().findAll().forEach { chunk ->
                val chunkGroupId = JournalChunkDAOId.deserialize(chunk.id!!).groupId
                if (chunkGroupId == groupId)
                {
                    chunk.deleteFromRealm()
                }
            }
        }
    }

    override suspend fun closeDb()
    {
    }


    private suspend fun generateJournalDb()
    {
        val db = getDb()
        db.refresh()

        db.executeTransaction {
            it.deleteAll()
        }

        val executionDays = generateExecutionDays().sorted()
        val personsList = personRepo.getPersonsByGroup(1)!!

        val chunkList: MutableList<JournalChunkDAO> = LinkedList()
        for (exeDay in executionDays)
        {
            chunkList.add(JournalChunkDAO(exeDay, 1))
        }

        val leftPerson = Random.nextInt(personsList.indices)
        var newPerson: Int

        do
        {
            newPerson = Random.nextInt(personsList.indices)
        }
        while (newPerson == leftPerson)

        for ((id, person) in personsList.withIndex())
        {
            when (id)
            {
                leftPerson -> generateRandomLeftPeopleData(person, chunkList, executionDays)
                newPerson -> generateRandomNewPeopleData(person, chunkList, executionDays)
                else -> generateCommonPeopleData(person, chunkList, executionDays)
            }
        }

        chunkList.forEach { chunk ->
            db.executeTransaction {
                it.copyToRealm(chunk)
            }
        }
    }

    private fun generateRandomLeftPeopleData(person: Person, chunkList: MutableList<JournalChunkDAO>, executionDays: List<Date>)
    {
        for (i in executionDays.indices)
        {
            if (i !in ((executionDays.indices.last - 2) .. executionDays.indices.last))
            {
                chunkList[i].data.add(JournalChunkDataDAO(person, getRandomCellData()))
            }
        }
    }

    private fun generateRandomNewPeopleData(person: Person, chunkList: MutableList<JournalChunkDAO>, executionDays: List<Date>)
    {
        for (i in executionDays.indices)
        {
            if (i !in 0..2)
            {
                chunkList[i].data.add(JournalChunkDataDAO(person, getRandomCellData()))
            }
        }
    }

    private fun generateCommonPeopleData(person: Person, chunkList: MutableList<JournalChunkDAO>, executionDays: List<Date>)
    {
        for (i in executionDays.indices)
        {
            chunkList[i].data.add(JournalChunkDataDAO(person, getRandomCellData()))
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
        val executionsCount = Random.nextInt(5..10)

        val generationPeriod = DateTimeTz.nowLocal() - 1.months
        for (i in 1..executionsCount)
        {
            var exeDay: Date
            do
            {
                exeDay = Date.invoke(generationPeriod.year, generationPeriod.month, Random.nextInt(1..generationPeriod.yearMonth.days))
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