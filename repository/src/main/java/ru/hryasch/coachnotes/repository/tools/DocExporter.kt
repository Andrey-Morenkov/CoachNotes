package ru.hryasch.coachnotes.repository.tools

import android.content.Context
import com.pawegio.kandroid.d
import com.pawegio.kandroid.i
import com.soywiz.klock.YearMonth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.apache.poi.xwpf.usermodel.*
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.koin.core.qualifier.named
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*
import ru.hryasch.coachnotes.domain.group.data.Group
import ru.hryasch.coachnotes.domain.journal.data.*
import ru.hryasch.coachnotes.domain.tools.DataExporter
import ru.hryasch.coachnotes.repository.R
import ru.hryasch.coachnotes.repository.tools.DocExporter.fileExtension
import ru.hryasch.coachnotes.repository.tools.DocExporter.saveDirectory
import java.io.File
import java.io.FileOutputStream
import java.math.BigInteger
import java.util.*
import kotlin.math.roundToLong


object DocExporter: DataExporter, KoinComponent
{
    internal val saveDirectory:File by inject(named("journalDirectory"))
    internal const val fileExtension = "doc"

    init
    {
        System.setProperty("org.apache.poi.javax.xml.stream.XMLInputFactory", "com.fasterxml.aalto.stax.InputFactoryImpl")
        System.setProperty("org.apache.poi.javax.xml.stream.XMLOutputFactory", "com.fasterxml.aalto.stax.OutputFactoryImpl")
        System.setProperty("org.apache.poi.javax.xml.stream.XMLEventFactory", "com.fasterxml.aalto.stax.EventFactoryImpl")
    }

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
        return XWPFDocument().also { XWPFHelper.createHeader(it, period, group.availableAge!!.toInt(), coachName) }
                             .also { XWPFHelper.createTable(it, chunks) }
                             .also { XWPFHelper.createFooter(it, coachName) }
    }

    private fun createOutputFile(): File
    {
        val monthNames: Array<String> by inject(named("months_RU"))

        val periodInfo = "${monthNames[period.month.index0].toLowerCase(Locale("ru"))} ${period.yearInt}"
        val groupInfo = "${group.availableAge} лет"

        // "Кондратьев Январь 2020 6 лет.docx"
        val outputFile = File(saveDirectory, "$coachName $periodInfo $groupInfo.$fileExtension")
        if (outputFile.exists())
        {
            outputFile.delete()
        }
        i("outputFile: ${outputFile.absolutePath}")
        outputFile.createNewFile()

        return outputFile
    }
}

private object XWPFHelper: KoinComponent
{
    private val A4Width = 21.cm()
    private val tableLeftIndentation = 1.cm()
    private val tableRightIndentation = 1.cm()
    private val context: Context by inject(named("global"))
    private val monthNames: Array<String> by inject(named("months_RU"))

    fun createHeader(document: XWPFDocument, period: YearMonth, groupAge: Int, coachName: String)
    {
        val headerParagraph = document.createParagraph().also { it.applyHeaderStyle() }

        headerParagraph
            .createRun()
            .also { it.applyDefaultStyle() }
            .apply {
                isBold = true
                setText(context.getString(R.string.journal_header_first_line).toUpperCase(Locale("ru")))
                addCarriageReturn()
            }

        headerParagraph
            .createRun()
            .also { it.applyDefaultStyle() }
            .apply {
                setText(context.getString(R.string.journal_header_second_line))
                addCarriageReturn()
            }

        headerParagraph
            .createRun()
            .also { it.applyTableStyle() }
            .apply {
                setText("$coachName   |  $groupAge лет   |   ${monthNames[period.month.index0]} ${period.yearInt}")
                addCarriageReturn()
            }
    }

    suspend fun createTable(document: XWPFDocument, chunks: List<JournalChunk>)
    {
        val table = document.createTable()
        createTableSkeleton(table, chunks.size, chunks[0].content.size) //hotfix
        tuneSkeletonTable(table, chunks.size)
        fillTunedTable(table, chunks)
    }

    fun createFooter(document: XWPFDocument, coachName: String)
    {
        document.createParagraph()
            .apply {
                alignment = ParagraphAlignment.LEFT
                indentationLeft = (-2).cm().toTwip().toInt()
            }
            .also { it.createRun().addCarriageReturn() }
            .createRun()
            .also { it.applyTableStyle() }
            .apply {
                setText("Подпись __________________ / $coachName")
            }
    }

    private fun tuneSkeletonTable(table: XWPFTable, chunksCount: Int)
    {
        // Table width
        table.ctTbl.tblPr.addNewTblW().w = BigInteger.valueOf((A4Width.toTwip() - tableLeftIndentation.toTwip() - tableRightIndentation.toTwip()))

        // Set custom № column width
        val widNum: CTTblWidth = table.getRow(0).getCell(0).ctTc.addNewTcPr().addNewTcW().apply { w = BigInteger.valueOf(0.75.cm().toTwip()) }
        i("TableColumn(0): set width = ${widNum.w.toLong()}TWips")

        // Set custom FullName column width
        val widFN : CTTblWidth = table.getRow(0).getCell(1).ctTc.addNewTcPr().addNewTcW().apply { w = BigInteger.valueOf(5.3.cm().toTwip()) }
        i("TableColumn(1): set width = ${widFN.w.toLong()}TWips")

        val chunkColumnsWidth = (table.ctTbl.tblPr.tblW.w.toLong() - widNum.w.toLong() - widFN.w.toLong()).toDouble() / chunksCount

        i("TableWidth = ${table.ctTbl.tblPr.tblW.w.toLong()} tw: [№:${widNum.w}tw, FN:${widFN.w}tw, $chunksCount columns:${chunkColumnsWidth}tw]")

        for (chunkColumn in 0 until chunksCount)
        {
            i("TableColumn(${chunkColumn + 2}): set width = ${BigInteger.valueOf(chunkColumnsWidth.toLong())} TWips")
            table.getRow(0).getCell(chunkColumn + 2).ctTc.addNewTcPr().addNewTcW().w = BigInteger.valueOf(chunkColumnsWidth.toLong())
        }
    }

    private fun createTableSkeleton(table: XWPFTable, chunksCount: Int, peopleCount: Int)
    {
        // Hotfix for page margins
        table.applyMarginFix()

        createSimpleTable(table, chunksCount, peopleCount)
        table.rows.forEach {
            it.tableCells.forEach {
                it.apply {
                    verticalAlignment = XWPFTableCell.XWPFVertAlign.CENTER
                    paragraphs.forEach { it.applyTableStyle() }
                }
            }
        }
        mergeCells(table)
        fillHeadersNames(table)
    }

    private fun createSimpleTable(table: XWPFTable, chunksCount: Int, peopleCount: Int)
    {
        val fullWidth = chunksCount + 2 // № + FullName columns
        val fullHeight = peopleCount + 4 // DateHeader + DateContent + Execution№ + ExecutionContent rows

        // Create table cells
        val firstRow = table.getRow(0)
        repeat(fullWidth - 1) //Cell(1,1) always exists
        {
            firstRow.addNewTableCell()
        }

        repeat(fullHeight - 1) //Cell(1,1) always exists
        {
            table.createRow()
        }
    }

    private fun mergeCells(table: XWPFTable)
    {
        // №
        table.getRow(0).getCell(0).ctTc.tcPr.vMerge = CTVMerge.Factory.newInstance().apply { `val` =  STMerge.RESTART}
        for (i in 1 .. 3)
        {
            table.getRow(i).getCell(0).ctTc.tcPr.vMerge = CTVMerge.Factory.newInstance().apply { `val` =  STMerge.CONTINUE}
        }

        // FullName
        table.getRow(0).getCell(1).ctTc.tcPr.vMerge = CTVMerge.Factory.newInstance().apply { `val` =  STMerge.RESTART}
        for (i in 1 .. 3)
        {
            table.getRow(i).getCell(1).ctTc.tcPr.vMerge = CTVMerge.Factory.newInstance().apply { `val` =  STMerge.CONTINUE}
        }

        // Date
        val tableWidthCells = table.getRow(0).tableCells.size
        table.getRow(0).getCell(2).ctTc.tcPr.hMerge = CTHMerge.Factory.newInstance().apply { `val` =  STMerge.RESTART}
        for (i in 3 until tableWidthCells)
        {
            table.getRow(0).getCell(i).ctTc.tcPr.hMerge = CTHMerge.Factory.newInstance().apply { `val` =  STMerge.CONTINUE}
        }

        // № Execution
        table.getRow(2).getCell(2).ctTc.tcPr.hMerge = CTHMerge.Factory.newInstance().apply { `val` =  STMerge.RESTART}
        for (i in 3 until tableWidthCells)
        {
            table.getRow(2).getCell(i).ctTc.tcPr.hMerge = CTHMerge.Factory.newInstance().apply { `val` =  STMerge.CONTINUE}
        }
    }

    private fun fillHeadersNames(table: XWPFTable)
    {
        table.getRow(0).getCell(0).paragraphs[0].createRun().also { it.applyTableStyle() }.apply { setText("№") }
        table.getRow(0).getCell(1).paragraphs[0].createRun().also { it.applyTableStyle() }.apply { setText("Ф.И") }
        table.getRow(0).getCell(2).paragraphs[0].createRun().also { it.applyTableStyle() }.apply { setText("Дата") }
        table.getRow(2).getCell(2).paragraphs[0].createRun().also { it.applyTableStyle() }.apply { setText("Номер занятия") }
        for (i in 4 until table.rows.size)
        {
            table.getRow(i).getCell(0).paragraphs[0].createRun().also { it.applyTableStyle() }.apply { setText("${i - 3}") }
        }
    }

    private fun fillTunedTable(table: XWPFTable, chunks: List<JournalChunk>)
    {
        val chunksSorted = chunks.sortedBy { it.date }
        val fullNameStartRow = 4
        val dateStartColumn = 2
        val executionNumStart = 2
        val markStartColumn = 2
        val markStartRow = 4

        for ((j, person) in chunksSorted[0].content.keys.withIndex())
        {
            table.getRow(fullNameStartRow + j).getCell(1).paragraphs[0]
                .also { it.applyFullNameStyle() }
                .createRun()
                .also { it.applyTableStyle() }
                .apply { setText("${person.surname} ${person.name}") }
        }

        for ((i, chunk) in chunksSorted.withIndex())
        {
            table.getRow(1).getCell(dateStartColumn + i).paragraphs[0]
                .createRun()
                .also { it.applyTableStyle() }
                .apply { setText(chunk.date.format("dd.MM")) }

            table.getRow(3).getCell(executionNumStart + i).paragraphs[0]
                .createRun()
                .also { it.applyTableStyle() }
                .apply { setText("${i + 1}") }

            for ((j, entry) in chunk.content.values.withIndex())
            {
                table.getRow(markStartRow + j).getCell(markStartColumn + i).paragraphs[0]
                    .createRun()
                    .also { it.applyTableStyle() }
                    .apply { setText( entry?.toDoc() ?: "") }
            }
        }
    }

    /*
    private fun setCustomPageMargins(document: XWPFDocument, top: XWPFMeasure, bottom: XWPFMeasure, left: XWPFMeasure, right: XWPFMeasure)
    {
        val sectPr = document.document.body.addNewSectPr()
        val pageMar: CTPageMar = sectPr.addNewPgMar()
        pageMar.top    = BigInteger.valueOf(top.toTwip())
        pageMar.bottom = BigInteger.valueOf(bottom.toTwip())
        pageMar.left   = BigInteger.valueOf(left.toTwip())
        pageMar.right  = BigInteger.valueOf(right.toTwip())
    }
     */
}

private fun CellData.toDoc(): String?
{
    return when (this)
    {
        is PresenceData -> ""
        is AbsenceData -> mark ?: "Н"
        else -> ""
    }
}

private fun XWPFParagraph.applyHeaderStyle()
{
    alignment = ParagraphAlignment.CENTER
    spacingBetween = "1.3".toDouble()
    spacingAfterLines = 0
    spacingAfter = 0

    // Hotfix for page margins
    indentationLeft = (-2).cm().toTwip().toInt()
}

private fun XWPFParagraph.applyFullNameStyle()
{
    alignment = ParagraphAlignment.LEFT
    spacingBefore = 50
    spacingAfter = 50
    indentationLeft = 80
}

private fun XWPFParagraph.applyTableStyle()
{
    alignment = ParagraphAlignment.CENTER
    spacingBetween = "1.0".toDouble()
    spacingAfterLines = 0
    spacingAfter = 40
    spacingBeforeLines = 0
    spacingBefore = 40
    indentationFirstLine = 0
    indentationLeft = 0
    indentationRight = 0
}

private fun XWPFRun.applyDefaultStyle()
{
    fontSize = 16
    fontFamily = "Times New Roman"
}

private fun XWPFRun.applyTableStyle()
{
    fontSize = 11
    fontFamily = "Times New Roman"
}

private fun XWPFTable.applyMarginFix()
{
    ctTbl.tblPr.addNewTblInd().apply {
        w = BigInteger.valueOf((-2).cm().toTwip())
        type = STTblWidth.DXA
    }
}

private fun Double.cm() = Cm(this.toFloat())
private fun Int.cm() = Cm(this.toFloat())
private fun Int.pt() = Pt(this.toFloat())

private const val PTs_IN_INCH = 72
private const val TWIPS_IN_PT = 20
private const val CMs_IN_INCH = 2.54

private sealed class XWPFMeasure(val value: Float)
{
    open fun toTwip(): Long = 0
}

private class Cm(value: Float): XWPFMeasure(value)
{
    override fun toTwip(): Long = (value * ((PTs_IN_INCH * TWIPS_IN_PT) / CMs_IN_INCH)).roundToLong()  // 567
}

private class Pt(value: Float): XWPFMeasure(value)
{
    override fun toTwip(): Long = (value * TWIPS_IN_PT).toLong()
}