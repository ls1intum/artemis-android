package de.tum.informatics.www1.artemis.native_app.core.common.markdown

/**
 * A custom utility class for encoding and decoding URLs for attachments and file-uploads.
 * This can be expanded as needed.
 */
object MarkdownUrlUtil {

    private const val SPACE = "%20"

    fun encodeUrl(url: String): String {
        return url.replace(" ", SPACE)
    }

    fun decodeUrl(url: String): String {
        return url.replace(SPACE, " ")
    }
}