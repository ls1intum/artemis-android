package de.tum.informatics.www1.artemis.native_app.feature.lectureview

import android.content.Context
import android.webkit.MimeTypeMap
import de.tum.informatics.www1.artemis.native_app.core.common.artemis_context.ArtemisContext
import de.tum.informatics.www1.artemis.native_app.core.common.artemis_context.authTokenOrEmptyString
import de.tum.informatics.www1.artemis.native_app.core.data.service.Api
import de.tum.informatics.www1.artemis.native_app.core.ui.remote_resources.BaseFile
import io.ktor.http.URLBuilder
import io.ktor.http.appendPathSegments
import java.io.File
import java.net.URLEncoder

object LectureUnitAttachmentUtil {

    sealed class LectureAttachmentType(val mimeTypePattern: String) {
        data object PDF : LectureAttachmentType("application/pdf")
        data object Image : LectureAttachmentType("image/")
        data object Other : LectureAttachmentType("*/*")
    }

    internal fun buildOpenAttachmentLink(
        serverUrl: String,
        attachmentLink: String
    ): String {
        return URLBuilder(serverUrl).apply {
            appendPathSegments(*Api.Core.UploadedFile.path)
            appendPathSegments(attachmentLink)
        }.buildString()
    }

    // Necessary to encode the file name for the attachment URL, see
    // https://github.com/ls1intum/Artemis/blob/develop/src/main/webapp/app/shared/http/file.service.ts
    internal fun createAttachmentFileUrl(downloadUrl: String, downloadName: String, encodeName: Boolean): String {
        val downloadUrlComponents = downloadUrl.split("/")
        val extension = downloadUrlComponents.lastOrNull()?.substringAfterLast('.', "") ?: ""
        val restOfUrl = downloadUrlComponents.dropLast(1).joinToString("/")
        val encodedDownloadName = if (encodeName) {
            URLEncoder.encode("$downloadName.$extension", "UTF-8").replace("+", "%20")
        } else {
            "$downloadName.$extension"
        }
        return "$restOfUrl/$encodedDownloadName"
    }

    internal fun detectAttachmentType(link: String?): LectureAttachmentType {
        if (link.isNullOrEmpty()) return LectureAttachmentType.Other

        val extension = MimeTypeMap.getFileExtensionFromUrl(link)?.lowercase() ?: return LectureAttachmentType.Other
        val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: return LectureAttachmentType.Other

        return when {
            mimeType == LectureAttachmentType.PDF.mimeTypePattern -> LectureAttachmentType.PDF
            mimeType.startsWith(LectureAttachmentType.Image.mimeTypePattern) -> LectureAttachmentType.Image
            else -> LectureAttachmentType.Other
        }
    }

    internal fun downloadAttachment(
        context: Context,
        artemisContext: ArtemisContext,
        name: String?,
        link: String
    ) {
        // We can use the download function of the BaseFile class to download the file
        val newName = name ?: link.substringAfterLast("/")
        val file = object : BaseFile(artemisContext.authTokenOrEmptyString, newName, link) {
            override fun share(context: Context, file: File) {}
        }
        file.download(context)
    }
}