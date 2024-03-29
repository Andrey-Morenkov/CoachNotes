package ru.hryasch.coachnotes.journal.table

import android.content.Context
import android.view.View
import android.view.ViewGroup

import com.evrencoskun.tableview.adapter.AbstractTableAdapter
import com.evrencoskun.tableview.adapter.recyclerview.holder.AbstractViewHolder
import com.pawegio.kandroid.inflateLayout
import org.koin.core.KoinComponent

import ru.hryasch.coachnotes.R
import ru.hryasch.coachnotes.journal.table.data.CellModel
import ru.hryasch.coachnotes.journal.table.data.ColumnHeaderModel
import ru.hryasch.coachnotes.journal.table.data.RowHeaderModel
import ru.hryasch.coachnotes.journal.table.data.TableModel
import ru.hryasch.coachnotes.journal.table.viewholders.*


class TableAdapter(private val context: Context,
                   val tableContent: TableModel
) : AbstractTableAdapter<ColumnHeaderModel, RowHeaderModel, CellModel>(), KoinComponent
{
    fun renderTable()
    {
        setAllItems(tableContent.columnHeaderContent,
                    tableContent.rowHeaderContent,
                    tableContent.cellsContent)
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