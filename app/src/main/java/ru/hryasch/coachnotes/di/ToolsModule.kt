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
    single(named("global")) { App.getContext() as Context}

    single(named("doc")) { DocExporter as DataExporter }

    single(named("journalDirectory")) {
        val journalDir: File = File(App.getContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)!!.toString() +
                                    File.separator +
                                    App.getContext().getString(R.string.coachNotesSubdirectoryName))
        if (!journalDir.exists())
        {
            journalDir.mkdirs()
        }
        return@single journalDir as File
    }
}