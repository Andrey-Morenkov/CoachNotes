package ru.hryasch.coachnotes.journal.table

import androidx.recyclerview.widget.RecyclerView
import com.evrencoskun.tableview.listener.ITableViewListener
import com.pawegio.kandroid.i
import ru.hryasch.coachnotes.domain.journal.data.PresenceData
import ru.hryasch.coachnotes.journal.presenters.JournalPresenter
import ru.hryasch.coachnotes.journal.table.viewholders.CellModel
import ru.hryasch.coachnotes.journal.table.viewholders.CellViewHolder

class TableViewClickListener(private val presenter: JournalPresenter) : ITableViewListener
{
    override fun onCellLongPressed(cellView: RecyclerView.ViewHolder, column: Int, row: Int)
    {
        i("cell ($column:$row) long pressed")
    }

    override fun onColumnHeaderLongPressed(columnHeaderView: RecyclerView.ViewHolder, column: Int)
    {
        i("column header ($column) long pressed")
    }

    override fun onRowHeaderClicked(rowHeaderView: RecyclerView.ViewHolder, row: Int)
    {
        i("row header ($row) clicked")
    }

    override fun onColumnHeaderClicked(columnHeaderView: RecyclerView.ViewHolder, column: Int)
    {
        i("column header ($column) clicked")
    }

    override fun onCellClicked(cellView: RecyclerView.ViewHolder, column: Int, row: Int)
    {
        i("cell ($column:$row) clicked")
        presenter.onCellClicked(column, row)
    }

    override fun onRowHeaderLongPressed(rowHeaderView: RecyclerView.ViewHolder, row: Int)
    {
        i("row header ($row) long pressed")
    }
}