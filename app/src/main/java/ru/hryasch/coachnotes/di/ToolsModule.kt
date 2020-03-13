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

val toolsModule = module {
    single(named("global")) { App.getCtx() as Context}

    single(named("docx")) { DocExporter as DataExporter }

    single(named("journalDirectory")) {
        val vtx: Context by inject(named("global"))
        val journalDir: File = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).toString() +
                                    File.separator +
                                    vtx.getString(R.string.coachNotesSubdirectoryName))
        if (!journalDir.exists())
        {
            journalDir.mkdirs()
        }
        return@single journalDir as File
    }
}