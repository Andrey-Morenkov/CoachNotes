import org.apache.poi.xwpf.usermodel.ParagraphAlignment
import org.apache.poi.xwpf.usermodel.XWPFDocument
import org.apache.poi.xwpf.usermodel.XWPFTable
import org.apache.poi.xwpf.usermodel.XWPFTableCell
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*
import java.io.File
import java.io.FileOutputStream
import java.math.BigInteger


class Main
{
    companion object
    {
        @JvmStatic
        fun main(args: Array<String>)
        {
            println("generating")
            val document: XWPFDocument = XWPFDocument()

            modifyDocument(document)
            createHeader(document)
            createTable(document)

            println("saving")
            val file: File = File("test.docx")
            if (file.exists()) {
                file.delete()
            }

            val out = FileOutputStream(file)
            document.write(out)
            out.close()

            println("done")
        }

        private fun modifyDocument(document: XWPFDocument)
        {
            val sectPr = document.document.body.addNewSectPr()
            val pageMar: CTPageMar = sectPr.addNewPgMar()
            pageMar.left = BigInteger.valueOf(567L)
            pageMar.right = BigInteger.valueOf(567L)
            pageMar.top = BigInteger.valueOf(567L * 2)
            pageMar.bottom = BigInteger.valueOf(567L * 2)
        }

        private fun createHeader(document: XWPFDocument)
        {
            val title = document.createParagraph()
            title.alignment = ParagraphAlignment.CENTER
            title.spacingBetween = "1.3".toDouble()
            title.spacingAfterLines = 0
            title.spacingAfter = 0

            val tabelStr = title.createRun()
            tabelStr.fontSize = 16
            tabelStr.fontFamily = "Times New Roman"
            tabelStr.isBold = true
            tabelStr.setText("ТАБЕЛЬ")
            tabelStr.addCarriageReturn()

            val tabelSubstr = title.createRun()
            tabelSubstr.fontSize = 16
            tabelSubstr.fontFamily = "Times New Roman"
            tabelSubstr.setText("учета посещаемости занятий")
            tabelSubstr.addCarriageReturn()

            val coach = "А.А.Кондратьев"
            val groupAge = 6
            val month = "Январь"
            val year = 2020

            val tabelParam = title.createRun()
            tabelParam.fontSize = 11
            tabelParam.fontFamily = "Times New Roman"
            tabelParam.setText("$coach   |   $groupAge лет   |   $month $year")
            tabelParam.addCarriageReturn()
        }

        private fun createTable(document: XWPFDocument)
        {
            val tableHeaderHeightRows = 4
            val tableHeaderWidthColumns = 1

            val chunksExample = 5
            val period = "01.2020"

            val peopleCount = 20

            val h = peopleCount + 4
            val w = chunksExample + 2

            val table = document.createTable()
            fillTable(table, h, w)

            val wid: CTTblWidth = table.getRow(0).getCell(0).ctTc.addNewTcPr().addNewTcW()
            wid.w = BigInteger.valueOf((567 * 0.75).toLong())

            val wid1: CTTblWidth = table.getRow(0).getCell(1).ctTc.addNewTcPr().addNewTcW()
            wid1.w = BigInteger.valueOf((567 * 5.3).toLong())

            println("TableWidth = ${table.width}, NoWidth = ${wid.w.toLong()}, FioWidth = ${wid1.w.toLong()}")

            val ostWidt = (table.width - wid.w.toLong() - wid1.w.toLong()).toDouble() / chunksExample
            for (cll in 0 until chunksExample)
            {
                table.getRow(0).getCell(cll + 2).ctTc.addNewTcPr().addNewTcW().w = BigInteger.valueOf(ostWidt.toLong())
            }





            /*

            Merge is working
            val hMerge = CTHMerge.Factory.newInstance()
            hMerge.setVal(STMerge.RESTART)
            table.getRow(0).getCell(0).ctTc.tcPr = CTTcPr.Factory.newInstance().apply { setHMerge(hMerge) }
            table.getRow(1).getCell(0).ctTc.tcPr = CTTcPr.Factory.newInstance().apply { setHMerge(hMerge) }

            val hMerge1 = CTHMerge.Factory.newInstance()
            hMerge.setVal(STMerge.CONTINUE)
            table.getRow(0).getCell(1).ctTc.tcPr = CTTcPr.Factory.newInstance().apply { setHMerge(hMerge1) }
            table.getRow(1).getCell(1).ctTc.tcPr = CTTcPr.Factory.newInstance().apply { setHMerge(hMerge1) }
            */

        }

        /*

        span is working

            val cellRow1 = table.getRow(0).getCell(0)
            val cellRow2 = table.getRow(1).getCell(0)

            cellRow1.ctTc.addNewTcPr()
            cellRow1.ctTc.tcPr.addNewGridSpan()
            cellRow1.ctTc.tcPr.gridSpan.setVal(BigInteger.valueOf(2L))

            cellRow2.ctTc.addNewTcPr()
            cellRow2.ctTc.tcPr.addNewGridSpan()
            cellRow2.ctTc.tcPr.gridSpan.setVal(BigInteger.valueOf(2L))
         */

        private fun fillTable(table: XWPFTable, h: Int, w: Int)
        {
            val firstRow = table.getRow(0)
            firstRow.getCell(0)

            for (c in 1 until w)
            {
                firstRow.addNewTableCell()
            }

            for (r in 1 until h)
            {
                table.createRow()
            }

            // TEST
            table.width = (19.57 * 567).toInt()

            table.rows.forEach {
                it.tableCells.forEach {
                    it.verticalAlignment = XWPFTableCell.XWPFVertAlign.CENTER
                    it.apply {
                        verticalAlignment = XWPFTableCell.XWPFVertAlign.CENTER
                        paragraphs.forEach {
                            it.apply {
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
                        }
                    }
                }
            }

            mergeCells(table)
            fillHeaders(table)
        }

        private fun mergeCells(table: XWPFTable)
        {
            // №
            table.getRow(0).getCell(0).ctTc.tcPr.vMerge = CTVMerge.Factory.newInstance().apply { `val` =  STMerge.RESTART}
            for (i in 1 .. 3)
            {
                table.getRow(i).getCell(0).ctTc.tcPr.vMerge = CTVMerge.Factory.newInstance().apply { `val` =  STMerge.CONTINUE}
            }

            // FIO
            table.getRow(0).getCell(1).ctTc.tcPr.vMerge = CTVMerge.Factory.newInstance().apply { `val` =  STMerge.RESTART}
            for (i in 1 .. 3)
            {
                table.getRow(i).getCell(1).ctTc.tcPr.vMerge = CTVMerge.Factory.newInstance().apply { `val` =  STMerge.CONTINUE}
            }

            // DATE
            val tableWidthCells = table.getRow(0).tableCells.size
            println("TableWidthCells = $tableWidthCells")

            table.getRow(0).getCell(2).ctTc.tcPr.hMerge = CTHMerge.Factory.newInstance().apply { `val` =  STMerge.RESTART}
            for (i in 3 until tableWidthCells)
            {
                table.getRow(0).getCell(i).ctTc.tcPr.hMerge = CTHMerge.Factory.newInstance().apply { `val` =  STMerge.CONTINUE}
            }

            // № Trenirovka
            table.getRow(2).getCell(2).ctTc.tcPr.hMerge = CTHMerge.Factory.newInstance().apply { `val` =  STMerge.RESTART}
            for (i in 3 until tableWidthCells)
            {
                table.getRow(2).getCell(i).ctTc.tcPr.hMerge = CTHMerge.Factory.newInstance().apply { `val` =  STMerge.CONTINUE}
            }
        }

        private fun fillHeaders(table: XWPFTable)
        {
            table.getRow(0).getCell(0).paragraphs[0].createRun().apply {
                fontSize = 11
                fontFamily = "Times New Roman"
                setText("№")
            }

            table.getRow(0).getCell(1).paragraphs[0].createRun().apply {
                fontSize = 11
                fontFamily = "Times New Roman"
                setText("Ф.И")
            }

            table.getRow(0).getCell(2).paragraphs[0].createRun().apply {
                fontSize = 11
                fontFamily = "Times New Roman"
                setText("Дата")
            }

            table.getRow(2).getCell(2).paragraphs[0].createRun().apply {
                fontSize = 11
                fontFamily = "Times New Roman"
                setText("Номер занятия")
            }

            for (i in 4 until table.rows.size)
            {
                table.getRow(i).getCell(0).paragraphs[0].createRun().apply {
                    fontSize = 11
                    fontFamily = "Times New Roman"
                    setText("${i - 3}")
                }
            }
        }
    }
}