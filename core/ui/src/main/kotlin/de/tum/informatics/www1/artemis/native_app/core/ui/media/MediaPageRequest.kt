package de.tum.informatics.www1.artemis.native_app.core.ui.media

import de.tum.informatics.www1.artemis.native_app.core.common.artemis_context.ArtemisContext
import java.security.MessageDigest

enum class MediaPageKind {
    PDF,
    VIDEO,
}

/**
 * Transient private activity handoff, matching the existing provider flow. Never contains an auth
 * token.
 */
data class MediaPageRequest(
    val url: String,
    val title: String,
    val identity: String,
    val scope: String,
    val kind: MediaPageKind,
    val initialPage: Int? = null,
    val description: String? = null,
    val relatedPdf: MediaPageRequest? = null,
)

fun mediaPageScope(session: ArtemisContext): String =
    MessageDigest.getInstance("SHA-256")
        .digest(
            "${(session as? ArtemisContext.LoggedIn)?.loginName.orEmpty()}|${normalizedMediaOrigin(session.serverUrl)}"
                .toByteArray()
        )
        .joinToString("") { "%02x".format(it) }
