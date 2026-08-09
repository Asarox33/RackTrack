package com.racktrack.presentation.share

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.racktrack.domain.MatchSummary
import com.racktrack.domain.MatchSummaryReport
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object MatchSummaryShare {
    suspend fun sharePdf(
        context: Context,
        summary: MatchSummary,
        title: String = "MATCH SUMMARY",
        accentArgb: Int = 0xFF1B9A4A.toInt(),
    ) {
        val file = withContext(Dispatchers.IO) {
            val dir = File(context.cacheDir, "shared").apply { mkdirs() }
            val output = File(dir, "${MatchSummaryReport.fileStem(summary)}.pdf")
            MatchSummaryPdfWriter.writeToFile(
                summary = summary,
                output = output,
                title = title,
                accentArgb = accentArgb,
            )
        }
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
                "${summary.player1Name} vs ${summary.player2Name}",
            )
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(intent, "Share match").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }
}
