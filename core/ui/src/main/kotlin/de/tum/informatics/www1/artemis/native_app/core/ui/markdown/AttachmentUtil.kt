package de.tum.informatics.www1.artemis.native_app.core.ui.markdown

import android.content.Context
import android.webkit.MimeTypeMap
import de.tum.informatics.www1.artemis.native_app.core.common.artemis_context.ArtemisContext
import de.tum.informatics.www1.artemis.native_app.core.common.artemis_context.authTokenOrEmptyString
import de.tum.informatics.www1.artemis.native_app.core.data.service.Api
import io.ktor.http.URLBuilder
import io.ktor.http.appendPathSegments
import java.net.URLEncoder

object AttachmentUtil {

    sealed class AttachmentType(val mimeTypePattern: String) {
        data object PDF : AttachmentType("application/pdf")
        data object Image : AttachmentType("image/")
        data object Other : AttachmentType("*/*")
    }

    fun buildOpenAttachmentLink(
        serverUrl: String,
        attachmentLink: String
    ): String {
        return URLBuilder(serverUrl).apply {
            appendPathSegments(*Api.Core.Files.path)
            appendPathSegments(attachmentLink)
        }.buildString()
    }

    // Necessary to encode the file name for the attachment URL, see
    // https://github.com/ls1intum/Artemis/blob/develop/src/main/webapp/app/shared/http/file.service.ts
    fun createAttachmentFileUrl(downloadUrl: String, downloadName: String, encodeName: Boolean): String {
        val downloadUrlComponents = downloadUrl.split("/")
        val extension = downloadUrlComponents.lastOrNull()?.substringAfterLast('.', "") ?: ""
        val restOfUrl = downloadUrlComponents.dropLast(1).joinToString("/")
        val encodedDownloadName = if (encodeName) {
            URLEncoder.encode("$downloadName.$extension", "UTF-8").replace("+", "%20")
        } else {
            "$downloadName.$extension"
        }
        return "$restOfUrl/student/$encodedDownloadName"
    }

    fun detectAttachmentType(link: String?): AttachmentType {
        if (link.isNullOrEmpty()) return AttachmentType.Other

        val extension = de.tum.informatics.www1.artemis.native_app.core.ui.media.mediaHttpUri(link)?.path?.substringAfterLast('.', "")?.lowercase() ?: return AttachmentType.Other
        val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: return AttachmentType.Other

        return when {
            mimeType == AttachmentType.PDF.mimeTypePattern -> AttachmentType.PDF
            mimeType.startsWith(AttachmentType.Image.mimeTypePattern) -> AttachmentType.Image
            else -> AttachmentType.Other
        }
    }

    suspend fun downloadAttachment(
        context: Context,
        artemisContext: ArtemisContext,
        name: String?,
        link: String
    ) {
        val newName = name ?: de.tum.informatics.www1.artemis.native_app.core.ui.media.mediaHttpUri(link)?.path?.substringAfterLast('/').orEmpty()
        de.tum.informatics.www1.artemis.native_app.core.ui.media.downloadMediaAttachment(
            context, artemisContext.serverUrl, artemisContext.authTokenOrEmptyString, link, newName
        )
    }
}
