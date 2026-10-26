package de.tum.informatics.www1.artemis.native_app.core.ui

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf

/**
 * Local provider to open links from the UI. Currently provided by
 * [de.tum.informatics.www1.artemis.native_app.android.ui.MainActivity].
 */
val LocalLinkOpener: ProvidableCompositionLocal<LinkOpener> = compositionLocalOf { EmptyLinkOpener }

/** Allows the ui to open links */
interface LinkOpener {
    fun openMediaPage(
        request: de.tum.informatics.www1.artemis.native_app.core.ui.media.MediaPageRequest
    ): Boolean = false

    fun openLink(url: String)

    /** A lecture provider page, rendered with a cookie store isolated from Artemis login. */
    fun openLectureVideoPage(url: String) = openLink(url)

    fun openLectureProviderPage(
        request: de.tum.informatics.www1.artemis.native_app.core.ui.media.MediaPageRequest
    ): Boolean {
        openLectureVideoPage(request.url)
        return true
    }
}

/** Link opener provided as default, does nothing when the ui requests to open a link. */
private object EmptyLinkOpener : LinkOpener {
    override fun openLink(url: String) = Unit
}
