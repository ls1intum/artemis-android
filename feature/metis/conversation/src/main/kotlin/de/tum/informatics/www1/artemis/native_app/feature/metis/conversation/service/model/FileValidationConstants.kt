package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.model

object FileValidationConstants {
    val ALLOWED_MIME_TYPES = arrayOf(
        "image/png", "image/jpeg", "image/gif", "image/svg+xml",
        "application/pdf", "application/zip", "application/x-tar",
        "text/plain", "application/rtf", "text/markdown", "text/html",
        "application/json", "application/msword",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        "text/csv", "application/vnd.ms-excel",
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        "application/vnd.ms-powerpoint",
        "application/vnd.openxmlformats-officedocument.presentationml.presentation"
    )

    private val IMAGE_EXTENSIONS = listOf("png", "jpg", "jpeg", "gif", "svg")

    fun isImage(fileName: String?): Boolean {
        if (fileName == null) return false
        return IMAGE_EXTENSIONS.any { extension ->
            fileName.endsWith(".$extension", ignoreCase = true)
        }
    }
}
