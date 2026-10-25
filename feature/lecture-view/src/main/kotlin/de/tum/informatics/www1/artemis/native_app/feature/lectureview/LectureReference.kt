package de.tum.informatics.www1.artemis.native_app.feature.lectureview

import de.tum.informatics.www1.artemis.native_app.core.model.lecture.Lecture
import de.tum.informatics.www1.artemis.native_app.core.model.lecture.lecture_units.LectureUnitAttachmentVideo

internal data class ResolvedLectureReference(val lectureId: Long, val unitId: Long? = null, val attachmentId: Int? = null, val page: Int? = null)

internal fun referenceLectureId(reference: String): Long? =
    Regex("(?:^|/)(?:lecture|lectures)/(\\d+)(?:/|$)").find(reference)?.groupValues?.get(1)?.toLongOrNull()
        ?: Regex("^/?(\\d+)/\\d+(?:/|$)").find(reference)?.groupValues?.get(1)?.toLongOrNull()

internal fun resolveLectureReference(lectures: List<Lecture>, type: String, reference: String): ResolvedLectureReference? {
    if (type !in setOf("attachment", "lecture-unit", "slide")) return null
    val slideId = if (type == "slide") reference.removePrefix("#").takeIf { reference.startsWith('#') }?.toLongOrNull() else null
    val unitId = Regex("(?:^|/)(?:attachment-unit|attachment-video-units)/(\\d+)(?:/|$)")
        .find(reference)?.groupValues?.get(1)?.toLongOrNull()
    val page = Regex("/slide/(-?\\d+)(?:$|[?#])").find(reference)?.groupValues?.get(1)?.toIntOrNull()
    val path = reference.substringBefore('?').substringBefore('#').trimStart('/').substringAfter("attachments/")
    lectures.forEach { lecture ->
        val lectureId = lecture.id ?: return@forEach
        lecture.lectureUnits.filterIsInstance<LectureUnitAttachmentVideo>().forEach { unit ->
            if (slideId != null) {
                unit.slides.firstOrNull { it.id == slideId }?.let { slide ->
                    val targetPage = if (unit.attachment?.studentVersion != null) unit.slides.sortedBy { it.slideNumber }.indexOf(slide) + 1 else slide.slideNumber
                    return ResolvedLectureReference(lectureId, unit.id, page = targetPage)
                }
            } else if (unit.id == unitId || unit.attachment?.link?.substringBefore('?')?.substringAfter("attachments/") == path) {
                if (type == "attachment") {
                    return ResolvedLectureReference(lectureId, attachmentId = unit.attachment?.id ?: return null)
                }
                val targetPage = if (type == "slide") {
                    val originalPage = page ?: return null
                    if (unit.slides.isNotEmpty() && unit.slides.none { it.slideNumber == originalPage }) return null
                    if (unit.attachment?.studentVersion != null) {
                        val index = unit.slides.sortedBy { it.slideNumber }.indexOfFirst { it.slideNumber == originalPage }
                        if (index == -1) return null
                        index + 1
                    } else originalPage
                } else null
                return ResolvedLectureReference(lectureId, unit.id, page = targetPage)
            }
        }
        if (type == "attachment") {
            lecture.attachments.firstOrNull { attachment ->
                attachment.link?.substringBefore('?')?.trimStart('/')?.substringAfter("attachments/") == path ||
                    path == "$lectureId/${attachment.id}"
            }?.let { return ResolvedLectureReference(lectureId, attachmentId = it.id) }
        }
    }
    return null
}
