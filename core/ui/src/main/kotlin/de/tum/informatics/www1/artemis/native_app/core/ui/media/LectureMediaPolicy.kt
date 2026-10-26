package de.tum.informatics.www1.artemis.native_app.core.ui.media

import java.net.URI
import java.security.MessageDigest
import java.util.Locale

enum class MediaSourceKind { MP4, HLS, WEB, INVALID }

fun mediaHttpUri(url: String): URI? = runCatching {
    URI(url).takeIf {
        it.scheme?.lowercase(Locale.ROOT) in setOf("http", "https") &&
            !it.host.isNullOrBlank() && it.rawUserInfo == null && it.port in -1..65535
    }
}.getOrNull()

fun classifyMediaSource(url: String, mimeType: String? = null): MediaSourceKind {
    val uri = mediaHttpUri(url) ?: return MediaSourceKind.INVALID
    val path = uri.path.orEmpty().lowercase(Locale.ROOT)
    return when {
        mimeType?.substringBefore(';')?.lowercase(Locale.ROOT) in setOf("application/vnd.apple.mpegurl", "application/x-mpegurl") || path.endsWith(".m3u8") -> MediaSourceKind.HLS
        mimeType?.substringBefore(';')?.lowercase(Locale.ROOT) == "video/mp4" || path.endsWith(".mp4") -> MediaSourceKind.MP4
        else -> MediaSourceKind.WEB
    }
}

fun normalizedMediaOrigin(url: String): String? = mediaHttpUri(url)?.let {
    val scheme = it.scheme.lowercase(Locale.ROOT)
    val port = if (it.port != -1) it.port else if (scheme == "https") 443 else 80
    "$scheme://${it.host.lowercase(Locale.ROOT)}:$port"
}

fun sameMediaOrigin(serverUrl: String, resourceUrl: String): Boolean {
    val origin = normalizedMediaOrigin(serverUrl) ?: return false
    return origin == normalizedMediaOrigin(resourceUrl)
}

/** No credentials, fragments or expiring access-token queries enter persistent keys. */
fun mediaProgressKey(account: String, serverUrl: String, identity: String, resourceUrl: String): String {
    val resource = mediaHttpUri(resourceUrl)?.let { "${normalizedMediaOrigin(resourceUrl)}${it.rawPath}" }.orEmpty()
    val fields = listOf(account, normalizedMediaOrigin(serverUrl).orEmpty(), identity, resource)
    val input = fields.joinToString("") { "${it.length}:$it" }
    return MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        .joinToString("") { "%02x".format(it) }
}

/** References are one-based; the PDF list/pager is zero-based. */
fun initialPdfPageIndex(explicitPage: Int?, savedPage: Int?, pageCount: Int): Int =
    ((explicitPage ?: savedPage ?: 1).toLong() - 1).coerceIn(0, (pageCount - 1).coerceAtLeast(0).toLong()).toInt()
