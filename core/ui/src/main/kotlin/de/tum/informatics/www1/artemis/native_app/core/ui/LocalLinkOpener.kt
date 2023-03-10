package de.tum.informatics.www1.artemis.native_app.core.ui

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf

/**
 * Local provider to open links from the UI. Currently provided by [de.tum.informatics.www1.artemis.native_app.android.ui.MainActivity].
 */
val LocalLinkOpener: ProvidableCompositionLocal<LinkOpener> = compositionLocalOf { EmptyLinkOpener }

/**
 * Allows the ui to open links
 */
interface LinkOpener {
    fun openLink(url: String)
}

/**
 * Link opener provided as default, does nothing when the ui requests to open a link.
 */
private object EmptyLinkOpener : LinkOpener {
    override fun openLink(url: String) = Unit
}