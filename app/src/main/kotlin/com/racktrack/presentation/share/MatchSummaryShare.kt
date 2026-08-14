package com.racktrack.presentation.share

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.racktrack.BuildConfig
import com.racktrack.domain.MatchSummary
import com.racktrack.domain.MatchSummaryReport
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object MatchSummaryShare {
    fun suggestedFileName(summary: MatchSummary): String =
        "${MatchSummaryReport.fileStem(summary)}.pdf"

    suspend fun sharePdf(
        context: Context,
        summary: MatchSummary,
        title: String = MatchSummaryReport.sessionSummaryTitle(summary),
        accentArgb: Int = 0xFF1B9A4A.toInt(),
    ) {
        val file = writePdfFile(context, summary, title, accentArgb)
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file,
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(
                Intent.EXTRA_SUBJECT,
                MatchSummaryReport.opponentsLabel(summary),
            )
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(
            intent,
            MatchSummaryReport.shareChooserLabel(summary),
        ).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }

    /** Writes the PDF into a user-chosen destination from [ActivityResultContracts.CreateDocument]. */
    suspend fun savePdfToUri(
        context: Context,
        summary: MatchSummary,
        destination: Uri,
        title: String = MatchSummaryReport.sessionSummaryTitle(summary),
        accentArgb: Int = 0xFF1B9A4A.toInt(),
    ) {
        val file = writePdfFile(context, summary, title, accentArgb)
        withContext(Dispatchers.IO) {
            context.contentResolver.openOutputStream(destination)?.use { out ->
                file.inputStream().use { input -> input.copyTo(out) }
            } ?: error("Could not open destination for PDF save")
        }
    }

    private suspend fun writePdfFile(
        context: Context,
        summary: MatchSummary,
        title: String,
        accentArgb: Int,
    ): File =
        withContext(Dispatchers.IO) {
            val dir = File(context.cacheDir, "shared").apply { mkdirs() }
            val output = File(dir, suggestedFileName(summary))
            MatchSummaryPdfWriter.writeToFile(
                summary = summary,
                output = output,
                title = title,
                accentArgb = accentArgb,
                versionName = BuildConfig.VERSION_NAME,
                versionCode = BuildConfig.VERSION_CODE,
            )
        }
}
