package ru.hryasch.coachnotes.repository.tools

import com.pawegio.kandroid.e
import com.pawegio.kandroid.i
import com.soywiz.klock.YearMonth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.math.BigInteger
import kotlin.math.roundToLong
import org.apache.poi.xwpf.usermodel.XWPFDocument
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.koin.core.qualifier.named
import java.io.File
import java.io.FileOutputStream
import java.util.*

import ru.hryasch.coachnotes.domain.journal.data.JournalChunk
import ru.hryasch.coachnotes.domain.tools.DataExporter
import ru.hryasch.coachnotes.domain.group.data.Group
import ru.hryasch.coachnotes.repository.tools.DocExporter.fileExtension
import ru.hryasch.coachnotes.repository.tools.DocExporter.saveDirectory



object DocExporter: DataExporter, KoinComponent
{
    internal val saveDirectory:File by inject(named("journalDirectory"))
    internal const val fileExtension = "docx"

    override suspend fun export(chunks: List<JournalChunk>, group: Group, period: YearMonth, coachName: String?)
    {
        JournalDocument.generate(chunks, group, period, coachName).save()
    }
}




private class JournalDocument(val period: YearMonth,
                              val group: Group,
                              val coachName: String): KoinComponent
{
    lateinit var document: XWPFDocument

    companion object
    {
        suspend fun generate(chunks: List<JournalChunk>,
                             group: Group,
                             period: YearMonth,
                             coachName: String?): JournalDocument
        {
            return JournalDocument(period, group, coachName ?: "Кондратьев А.А")
                            .apply { document = generateDocument(chunks) }
        }
    }

    suspend fun save()
    {
        i("Saving file...")
        val saveJob = GlobalScope.launch(Dispatchers.IO)
        {
            val outStream = FileOutputStream(createOutputFile())
            document.write(outStream)
            outStream.close()
        }

        saveJob.join()

        i("Saved!")
    }

    private suspend fun generateDocument(chunks: List<JournalChunk>): XWPFDocument
    {
        return XWPFDocument().also { XWPFHelper.prepareDocument(it) }
                             .also { XWPFHelper.createHeader(it, period, group.availableAge.toInt(), coachName) }
                             .also { XWPFHelper.createTable(it, chunks) }
                             .also { XWPFHelper.createFooter(it, coachName) }
    }

    private fun createOutputFile(): File
    {
        val monthNames: Array<String> by inject(named("months_RU"))

        val periodInfo = "${monthNames[period.month.index0].toLowerCase(Locale("ru"))} ${period.yearInt}"
        val groupInfo = "${group.availableAge}"

        // "Кондратьев Январь 2020 6 лет.docx"
        val outputFile = File(saveDirectory, "$coachName $periodInfo $groupInfo.$fileExtension")
        if (outputFile.exists())
        {
            outputFile.delete()
            outputFile.createNewFile()
        }

        i("outputFile: ${outputFile.absolutePath}")

        return outputFile
    }
}

private object XWPFHelper
{
    fun prepareDocument(document: XWPFDocument)
    {
        e("TRY SET CUSTOM MARGINS")
        setCustomPageMargins(document, 2.cm(), 1.cm(), 1.cm(), 1.cm())
    }

    fun createHeader(document: XWPFDocument, period: YearMonth, groupAge: Int, coachName: String)
    {

    }

    suspend fun createTable(document: XWPFDocument, chunks: List<JournalChunk>)
    {

    }

    fun createFooter(document: XWPFDocument, coachName: String)
    {

    }


    private fun setCustomPageMargins(document: XWPFDocument, top: XWPFMeasure, bottom: XWPFMeasure, left: XWPFMeasure, right: XWPFMeasure)
    {
        val sectPr = document.document.body.addNewSectPr()
        sectPr.addNewPgMar()
        ///val pageMar: CTPageMar = sectPr.addNewPgMar()
        //pageMar.top    = BigInteger.valueOf(top.toTwip())
        //pageMar.bottom = BigInteger.valueOf(bottom.toTwip())
        //pageMar.left   = BigInteger.valueOf(left.toTwip())
        //pageMar.right  = BigInteger.valueOf(right.toTwip())
    }
}

private fun Int.cm() = Cm(this.toFloat())
private fun Int.pt() = Pt(this.toFloat())

private const val PTs_IN_INCH = 72
private const val TWIPS_IN_PT = 20
private const val CMs_IN_INCH = 2.54

sealed class XWPFMeasure(val value: Float)
{
    open fun toTwip(): Long = 0
}

class Cm(value: Float): XWPFMeasure(value)
{
    override fun toTwip(): Long = ((PTs_IN_INCH * TWIPS_IN_PT) / CMs_IN_INCH).roundToLong()  // 567
}

class Pt(value: Float): XWPFMeasure(value)
{
    override fun toTwip(): Long = TWIPS_IN_PT.toLong()
}