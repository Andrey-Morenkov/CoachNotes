package ru.hryasch.coachnotes.journal.table

import androidx.recyclerview.widget.RecyclerView
import com.evrencoskun.tableview.listener.ITableViewListener
import com.pawegio.kandroid.i
import ru.hryasch.coachnotes.journal.presenters.JournalPresenter
import ru.hryasch.coachnotes.journal.table.viewholders.CellViewHolder
import ru.hryasch.coachnotes.journal.table.viewholders.ColumnHeaderViewHolder
import ru.hryasch.coachnotes.journal.table.viewholders.RowHeaderViewHolder
import java.util.stream.Collectors

class TableViewClickListener(private val presenter: JournalPresenter) : ITableViewListener
{
    override fun onCellLongPressed(cellView: RecyclerView.ViewHolder, column: Int, row: Int)
    {
        val trueCoordinate: List<Int> = (cellView as CellViewHolder).getInternalId()
                                                                    .split(":")
                                                                    .stream()
                                                                    .map { it.toInt() }
                                                                    .collect(Collectors.toList())

        i("cell (${trueCoordinate[0]}:${trueCoordinate[1]}) long pressed")
        presenter.onCellLongPressed(trueCoordinate[0], trueCoordinate[1])
    }

    override fun onColumnHeaderLongPressed(columnHeaderView: RecyclerView.ViewHolder, column: Int)
    {
        val trueColumn = (columnHeaderView as ColumnHeaderViewHolder).getInternalId()

        i("column header ($trueColumn) long pressed")
        presenter.onColumnLongPressed(trueColumn)
    }

    override fun onRowHeaderClicked(rowHeaderView: RecyclerView.ViewHolder, row: Int)
    {
        val trueRow = (rowHeaderView as RowHeaderViewHolder).getInternalId()

        i("row header ($trueRow) clicked")
    }

    override fun onColumnHeaderClicked(columnHeaderView: RecyclerView.ViewHolder, column: Int)
    {
        val trueColumn = (columnHeaderView as ColumnHeaderViewHolder).getInternalId()

        i("column header ($trueColumn) clicked")
    }

    override fun onCellClicked(cellView: RecyclerView.ViewHolder, column: Int, row: Int)
    {
        val trueCoordinate: List<Int> = (cellView as CellViewHolder).getInternalId()
                                                                    .split(":")
                                                                    .stream()
                                                                    .map { it.toInt() }
                                                                    .collect(Collectors.toList())

        i("cell (${trueCoordinate[0]}:${trueCoordinate[1]}) clicked")
        presenter.onCellClicked(trueCoordinate[0], trueCoordinate[1])
    }

    override fun onRowHeaderLongPressed(rowHeaderView: RecyclerView.ViewHolder, row: Int)
    {
        val trueRow = (rowHeaderView as RowHeaderViewHolder).getInternalId()

        i("row header ($trueRow) long pressed")
    }
}