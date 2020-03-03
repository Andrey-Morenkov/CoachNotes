package ru.hryasch.coachnotes.journal

import android.content.Context
import android.view.View
import android.view.ViewGroup

import com.evrencoskun.tableview.adapter.AbstractTableAdapter
import com.evrencoskun.tableview.adapter.recyclerview.holder.AbstractViewHolder
import com.pawegio.kandroid.inflateLayout
import org.koin.core.KoinComponent
import org.koin.core.get
import org.koin.core.qualifier.named

import ru.hryasch.coachnotes.R
import ru.hryasch.coachnotes.journal.viewholders.CellViewHolder
import ru.hryasch.coachnotes.journal.viewholders.ColumnHeaderViewHolder
import ru.hryasch.coachnotes.journal.viewholders.RowHeaderViewHolder


class TableAdapter(private val context: Context) : AbstractTableAdapter<ColumnHeaderModel, RowHeaderModel, CellModel>(), KoinComponent
{
    private val tableContent: TableModel = get(named("mock"))

    fun renderTable()
    {
        setAllItems(tableContent.columnHeaderContent,
                    tableContent.rowHeaderContent,
                    tableContent.cellContent)
    }

    override fun onCreateColumnHeaderViewHolder(parent: ViewGroup,
                                                viewType: Int): AbstractViewHolder
    {
        val layout = context.inflateLayout(R.layout.element_journal_table_column_header, parent)
        return ColumnHeaderViewHolder(layout)
    }

    override fun onBindColumnHeaderViewHolder(holder: AbstractViewHolder,
                                              columnHeaderItemModel: ColumnHeaderModel?,
                                              columnPosition: Int)
    {
        (holder as ColumnHeaderViewHolder).setModel(columnHeaderItemModel!!)
    }

    override fun onCreateRowHeaderViewHolder(parent: ViewGroup,
                                             viewType: Int): AbstractViewHolder
    {
        val layout = context.inflateLayout(R.layout.element_journal_table_row_header, parent)
        return RowHeaderViewHolder(layout)
    }

    override fun onBindRowHeaderViewHolder(holder: AbstractViewHolder,
                                           rowHeaderItemModel: RowHeaderModel?,
                                           rowPosition: Int)
    {
        (holder as RowHeaderViewHolder).setModel(rowHeaderItemModel!!)
    }

    override fun onCreateCellViewHolder(parent: ViewGroup,
                                        viewType: Int): AbstractViewHolder
    {
        val layout = context.inflateLayout(R.layout.element_journal_table_cell, parent)
        return CellViewHolder(layout, context)
    }

    override fun onBindCellViewHolder(holder: AbstractViewHolder,
                                      cellItemModel: CellModel?,
                                      columnPosition: Int,
                                      rowPosition: Int)
    {
        (holder as CellViewHolder).setModel(cellItemModel!!)
    }

    override fun onCreateCornerView(parent: ViewGroup): View
    {
        return context.inflateLayout(R.layout.element_journal_table_corner, parent)
    }

    override fun getColumnHeaderItemViewType(position: Int): Int = 0

    override fun getRowHeaderItemViewType(position: Int): Int = 0

    override fun getCellItemViewType(position: Int): Int = 0
}