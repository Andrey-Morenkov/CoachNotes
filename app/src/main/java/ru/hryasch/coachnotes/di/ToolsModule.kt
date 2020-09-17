package ru.hryasch.coachnotes.di

import android.content.Context
import android.os.Environment
import org.koin.core.qualifier.named
import org.koin.dsl.module
import ru.hryasch.coachnotes.R
import ru.hryasch.coachnotes.application.App
import ru.hryasch.coachnotes.domain.tools.DataExporter
import ru.hryasch.coachnotes.repository.tools.DocExporter
import java.io.File
import java.time.ZonedDateTime

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
        val currYear = ZonedDateTime.now().year
        val ages = ArrayList<String>(50)
        for (i in 1..50)
        {
            ages.add("${currYear - i}")
        }

        return@single ages as List<String>
    }

    single(named("relativeAgesList"))
    {
        val ages = ArrayList<String>(50)
        for (i in 1..50)
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

    single(named("paymentTypes"))
    {
        return@single listOf(App.getCtx().getString(R.string.group_param_payment_free),
                             App.getCtx().getString(R.string.group_param_payment_paid))
    }

    single(named("ageTypes"))
    {
        return@single listOf(App.getCtx().getString(R.string.age_type_absolute),
                             App.getCtx().getString(R.string.age_type_relative))
    }

    single(named("journalYears"))
    {
        val currYear = ZonedDateTime.now().year
        val firstYear = 2020

        val result: Array<Int> = Array((currYear - firstYear) + 1) { 0 }

        for ((i, year) in (firstYear .. currYear).withIndex())
        {
            result[i] = year
        }

        return@single result
    }
}