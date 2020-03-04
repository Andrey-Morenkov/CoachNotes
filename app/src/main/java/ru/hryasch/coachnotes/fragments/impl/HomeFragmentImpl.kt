package ru.hryasch.coachnotes.fragments.impl

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.evrencoskun.tableview.TableView
import moxy.MvpAppCompatFragment
import org.koin.android.ext.android.get
import org.koin.core.parameter.parametersOf
import ru.hryasch.coachnotes.R
import ru.hryasch.coachnotes.journal.Journal
import ru.hryasch.coachnotes.journal.table.TableAdapter

class HomeFragmentImpl : MvpAppCompatFragment()
{
    private lateinit var journal: Journal


    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View?
    {
        val view = inflater.inflate(R.layout.fragment_journal, container, false)

        journal = get { parametersOf(view, container!!.context) }

        return view
    }
}