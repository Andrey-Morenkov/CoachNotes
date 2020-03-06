package ru.hryasch.coachnotes.domain.journal.data

class TableData
{
    var columnHeadersData: MutableList<ColumnHeaderData> = ArrayList()
    var rowHeadersData: MutableList<RowHeaderData> = ArrayList()
    var cellsData: MutableList<MutableList<CellData?>> = ArrayList()
}