package de.tum.informatics.www1.artemis.native_app.android.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent
import androidx.navigation.NavHostController
import de.tum.informatics.www1.artemis.native_app.core.ui.LinkOpener
import de.tum.informatics.www1.artemis.native_app.core.ui.media.mediaHttpUri
import de.tum.informatics.www1.artemis.native_app.core.ui.R as CoreR

internal class AppLinkOpener(private val context: Context, private val navController: NavHostController) : LinkOpener {
    override fun openMediaPage(request: de.tum.informatics.www1.artemis.native_app.core.ui.media.MediaPageRequest): Boolean {
        if (mediaHttpUri(request.url) == null) return false
        return try {
            val intent = LectureMediaActivity.intent(context, request)
            if (context !is android.app.Activity) intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            true
        } catch (_: android.content.ActivityNotFoundException) { false }
          catch (_: SecurityException) { false }
    }

    override fun openLectureVideoPage(url: String) {
        if (mediaHttpUri(url) == null) return
        context.startActivity(Intent(context, LectureWebVideoActivity::class.java).putExtra(LectureWebVideoActivity.URL_EXTRA, url))
    }

    override fun openLink(url: String) {
        val uri = Uri.parse(url)
        val mediaHost = uri.encodedAuthority?.substringAfterLast('@')?.substringBefore(':') == "courses"
        if (uri.scheme.equals("artemis", ignoreCase = true) && mediaHost && "lecture-media" in uri.pathSegments) {
            if (!validLectureMediaLink(uri)) {
                invalidReference()
                return
            }
            try { navController.navigate(uri) }
            catch (_: IllegalArgumentException) { invalidReference() }
            catch (_: IllegalStateException) { invalidReference() }
            return
        }
        CustomTabsIntent.Builder().build().launchUrl(context, uri)
    }

    private fun validLectureMediaLink(uri: Uri): Boolean {
        val segments = uri.pathSegments
        if (segments.size != 2 || segments[1] != "lecture-media" || segments[0].toLongOrNull()?.let { it > 0 } != true) return false
        if (uri.authority != "courses" || uri.fragment != null) return false
        if (uri.getQueryParameters("referenceType").size != 1 || uri.getQueryParameters("reference").size != 1) return false
        val type = uri.getQueryParameter("referenceType")
        val reference = uri.getQueryParameter("reference") ?: return false
        if (type !in setOf("slide", "attachment", "lecture-unit") || reference.isBlank()) return false
        if (type == "slide" && reference.startsWith('#') && reference.drop(1).toLongOrNull()?.let { it > 0 } != true) return false
        return true
    }

    private fun invalidReference() {
        Toast.makeText(context, CoreR.string.lecture_media_invalid, Toast.LENGTH_LONG).show()
    }
}
