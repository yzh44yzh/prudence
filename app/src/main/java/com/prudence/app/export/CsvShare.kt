package com.prudence.app.export

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File
import java.time.LocalDate

/** Writes the CSV to a shareable cache file (replacing older exports) and hands it to the system share sheet (SPEC.md §7). */
fun shareCsv(context: Context, csv: String, date: LocalDate = LocalDate.now()) {
    val dir = File(context.cacheDir, "exports").apply { mkdirs() }
    dir.listFiles()?.forEach { it.delete() } // only the latest export stays cached
    val file = File(dir, "prudence-$date.csv")
    file.writeText(csv, Charsets.UTF_8)
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/csv"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, null))
}
