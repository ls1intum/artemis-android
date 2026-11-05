package de.tum.informatics.www1.artemis.native_app.feature.lectureview

import de.tum.informatics.www1.artemis.native_app.core.common.artemis_context.ArtemisContext
import de.tum.informatics.www1.artemis.native_app.core.model.lecture.lecture_units.LectureUnitAttachmentVideo
import de.tum.informatics.www1.artemis.native_app.core.ui.LinkOpener
import de.tum.informatics.www1.artemis.native_app.core.ui.markdown.AttachmentUtil
import de.tum.informatics.www1.artemis.native_app.core.ui.media.*

internal val LectureUnitAttachmentVideo.isPageMedia: Boolean
    get() =
        hasVideo ||
            attachment?.let {
                (it.studentVersion ?: it.link)
                    ?.substringBefore('?')
                    ?.substringBefore('#')
                    ?.endsWith(".pdf", ignoreCase = true) == true
            } == true

internal fun openLectureUnitMedia(
    unit: LectureUnitAttachmentVideo,
    lectureId: Long?,
    session: ArtemisContext,
    opener: LinkOpener,
): Boolean {
    val scope = mediaPageScope(session)
    val pdf =
        unit.attachment?.let { attachment ->
            val link = attachment.studentVersion ?: attachment.link.orEmpty()
            val url =
                if (mediaHttpUri(link) != null) link
                else AttachmentUtil.buildOpenAttachmentLink(session.serverUrl, link)
            val source =
                if (attachment.studentVersion != null || !sameMediaOrigin(session.serverUrl, url))
                    url
                else AttachmentUtil.createAttachmentFileUrl(url, attachment.name.orEmpty(), true)
            if (AttachmentUtil.detectAttachmentType(source) == AttachmentUtil.AttachmentType.PDF)
                MediaPageRequest(
                    source,
                    attachment.name.orEmpty(),
                    "lecture/$lectureId/attachment/${attachment.id}",
                    scope,
                    MediaPageKind.PDF,
                    description = unit.description,
                )
            else null
        }
    if (unit.hasVideo) {
        val url = unit.videoSource.orEmpty()
        val request =
            MediaPageRequest(
                url,
                unit.name.orEmpty(),
                "unit/${unit.id}",
                scope,
                MediaPageKind.VIDEO,
                description = unit.description,
                relatedPdf = pdf,
            )
        return when (classifyMediaSource(url)) {
            MediaSourceKind.INVALID -> false
            MediaSourceKind.WEB -> opener.openLectureProviderPage(request)
            else -> opener.openMediaPage(request)
        }
    }
    return pdf?.let(opener::openMediaPage) ?: false
}
