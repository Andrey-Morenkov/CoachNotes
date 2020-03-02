package ru.hryasch.coachnotes.journal

class TableModel()
{
    private lateinit var columnHeaderContent: List<ColumnHeaderModel>
    private lateinit var rowHeaderContent: List<RowHeaderModel>
    private lateinit var cellContent: List<List<CellModel>>
}