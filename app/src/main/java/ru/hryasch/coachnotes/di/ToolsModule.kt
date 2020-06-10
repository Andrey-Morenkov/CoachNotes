package ru.hryasch.coachnotes.di

import android.content.Context
import android.os.Environment
import com.pawegio.kandroid.i
import com.soywiz.klock.DateTime
import org.koin.core.qualifier.named
import org.koin.dsl.module
import ru.hryasch.coachnotes.R
import ru.hryasch.coachnotes.application.App
import ru.hryasch.coachnotes.domain.tools.DataExporter
import ru.hryasch.coachnotes.repository.tools.DocExporter
import java.io.File

val toolsModule = module {
    single(named("global")) { App.getCtx() as Context }

    single(named("docx")) { DocExporter as DataExporter}

    single(named("journalDirectory"))
    {
        val vtx: Context by inject(named("global"))
        val journalDir: File = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).canonicalPath +
                     File.separator +
                     vtx.getString(R.string.coachNotesSubdirectoryName))
        if (!journalDir.exists())
        {
            journalDir.mkdirs()
        }
        return@single journalDir as File
    }

    single(named("absoluteAgesList"))
    {
        val currYear = DateTime.nowLocal().yearInt
        val ages = ArrayList<String>(50)
        for (i in 3..50)
        {
            ages.add("${currYear - i}")
        }

        return@single ages as List<String>
    }

    single(named("relativeAgesList"))
    {
        val ages = ArrayList<String>(50)
        for (i in 3..50)
        {
            ages.add("$i")
        }

        return@single ages as List<String>
    }

    single(named("monthDays"))
    {
        val days = ArrayList<String>(31)
        for (i in 1..31)
        {
            days.add("$i")
        }

        return@single days as List<String>
    }
}