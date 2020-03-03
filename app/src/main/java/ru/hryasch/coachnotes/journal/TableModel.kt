package ru.hryasch.coachnotes.journal

open class TableModel()
{
    var columnHeaderContent: MutableList<ColumnHeaderModel> = ArrayList()
    var rowHeaderContent: MutableList<RowHeaderModel> = ArrayList()
    var cellContent: MutableList<List<CellModel>> = ArrayList()
}

class MockTableModel(): TableModel()
{
    init
    {
        for (i in 0..10)
        {
            val chm: ColumnHeaderModel = ColumnHeaderModel(ColumnHeaderData(20, "Пн"))
            val rhm: RowHeaderModel = RowHeaderModel(RowHeaderData("Иванов", "Иван"))
            val cm:  MutableList<CellModel> = ArrayList()
            for (j in 0..10)
            {
                cm.add(CellModel("$i$j", null))
            }

            columnHeaderContent.add(chm)
            rowHeaderContent.add(rhm)
            cellContent.add(cm)
        }
    }
}