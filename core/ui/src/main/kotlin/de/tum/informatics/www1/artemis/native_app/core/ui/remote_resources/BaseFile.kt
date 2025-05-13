package de.tum.informatics.www1.artemis.native_app.core.ui.remote_resources

import android.app.DownloadManager
import android.content.Context
import android.os.Environment
import android.widget.Toast
import androidx.core.content.ContextCompat.getString
import androidx.core.net.toUri
import de.tum.informatics.www1.artemis.native_app.core.ui.R
import io.ktor.http.HttpHeaders
import java.io.File
import java.net.UnknownHostException

abstract class BaseFile(
    open val authToken: String,
    open val filename: String,
    open val url: String
) {
    abstract fun share(context: Context, file: File)

    fun download(context: Context) {
        try {
            val downloadManager =
                context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val downloadUri = url.toUri()

            downloadManager.enqueue(
                DownloadManager.Request(downloadUri)
                    .addRequestHeader(HttpHeaders.Cookie, "jwt=$authToken")
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    .setTitle(filename)
                    .setDestinationInExternalPublicDir(
                        Environment.DIRECTORY_DOWNLOADS,
                        downloadUri.lastPathSegment
                    )
            )

            Toast.makeText(
                context,
                getString(context, R.string.pdf_view_downloading_toast),
                Toast.LENGTH_SHORT
            ).show()

        } catch (e: Exception) {
            val errorMessage = when (e) {
                is UnknownHostException -> R.string.pdf_view_error_no_internet
                else -> R.string.pdf_view_error_downloading
            }
            Toast.makeText(context, getString(context, errorMessage), Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }
}