package ru.hryasch.coachnotes.repository.tools

import com.pawegio.kandroid.i
import com.soywiz.klock.YearMonth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.apache.poi.xwpf.usermodel.XWPFDocument
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.koin.core.qualifier.named
import java.io.File

import ru.hryasch.coachnotes.domain.journal.data.JournalChunk
import ru.hryasch.coachnotes.domain.tools.DataExporter
import java.io.FileOutputStream
import java.util.*


object DocExporter: DataExporter, KoinComponent
{
    override suspend fun export(chunks: List<JournalChunk>, groupAge: Int, period: YearMonth)
    {
        val journalDirectory: File by inject(named("journalDirectory"))
        val monthNames: Array<String> by inject(named("months_RU"))
        val coachName = "Кондратьев"
        val month = monthNames[period.month.index0]
        val year = period.yearInt

        // "Кондратьев январь 2020 6 лет.doc"
        val resultFile = File(journalDirectory, "$coachName ${month.toLowerCase(Locale("ru"))} $year ${groupAge}.doc")
        if (resultFile.exists())
        {
            resultFile.delete()
        }

        val document = generateJournalDocument(chunks, groupAge, period)

        i("=== Saving file... ===")
        val saveJob = GlobalScope.launch(Dispatchers.IO)
        {
            val outStream: FileOutputStream = FileOutputStream(resultFile)
            document.write(outStream)
            outStream.close()
        }

        saveJob.join()

        i("=== File saved ===")
    }

    private suspend fun generateJournalDocument(chunks: List<JournalChunk>, groupAge: Int, period: YearMonth): XWPFDocument
    {
        val document = XWPFDocument()

        // margins
        // header




        return document
    }
}