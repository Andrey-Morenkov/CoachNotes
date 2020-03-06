package ru.hryasch.coachnotes.journal.table

import androidx.recyclerview.widget.RecyclerView
import com.evrencoskun.tableview.listener.ITableViewListener
import com.pawegio.kandroid.i
import ru.hryasch.coachnotes.journal.presenters.JournalPresenter

class TableViewClickListener(private val presenter: JournalPresenter) : ITableViewListener
{
    override fun onCellLongPressed(cellView: RecyclerView.ViewHolder, column: Int, row: Int)
    {
        i("cell ($column:$row) long pressed")
        //presenter.
    }

    override fun onColumnHeaderLongPressed(columnHeaderView: RecyclerView.ViewHolder, column: Int)
    {
        i("column header ($column:0) long pressed")
    }

    override fun onRowHeaderClicked(rowHeaderView: RecyclerView.ViewHolder, row: Int)
    {
        i("row header (0:$row) clicked")
    }

    override fun onColumnHeaderClicked(columnHeaderView: RecyclerView.ViewHolder, column: Int)
    {
        i("column header ($column:0) clicked")
    }

    override fun onCellClicked(cellView: RecyclerView.ViewHolder, column: Int, row: Int)
    {
        i("cell ($column:$row) clicked")
        presenter.test(column, row)
    }

    override fun onRowHeaderLongPressed(rowHeaderView: RecyclerView.ViewHolder, row: Int)
    {
        i("row header (0:$row) long pressed")
    }
}