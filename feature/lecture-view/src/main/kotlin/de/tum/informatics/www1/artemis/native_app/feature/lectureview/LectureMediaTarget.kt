package de.tum.informatics.www1.artemis.native_app.feature.lectureview

import de.tum.informatics.www1.artemis.native_app.core.model.lecture.Attachment
import de.tum.informatics.www1.artemis.native_app.core.model.lecture.Lecture
import de.tum.informatics.www1.artemis.native_app.core.model.lecture.lecture_units.LectureUnitAttachmentVideo
import de.tum.informatics.www1.artemis.native_app.core.model.lecture.lecture_units.LectureUnit

internal sealed interface LectureMediaDestination {
    data class Unit(val unit: LectureUnit): LectureMediaDestination
    data class Slides(val attachment: Attachment, val page: Int?): LectureMediaDestination
    data object Missing: LectureMediaDestination
}
internal fun resolveLectureMediaTarget(
    lecture: Lecture,
    visibleUnits: List<LectureUnit>,
    unitId: Long?,
    attachmentId: Int?,
    page: Int?
): LectureMediaDestination {
    if (unitId != null) {
        val unit = visibleUnits.firstOrNull { it.id == unitId } ?: return LectureMediaDestination.Missing
        if (page == null) return LectureMediaDestination.Unit(unit)
        val attachment = (unit as? LectureUnitAttachmentVideo)?.attachment ?: return LectureMediaDestination.Missing
        if (attachment.link.isNullOrBlank() && attachment.studentVersion.isNullOrBlank()) return LectureMediaDestination.Missing
        return LectureMediaDestination.Slides(attachment, page)
    }
    val attachment = (lecture.attachments + visibleUnits.filterIsInstance<LectureUnitAttachmentVideo>().mapNotNull { it.attachment })
        .firstOrNull { it.id == attachmentId } ?: return LectureMediaDestination.Missing
    if (attachment.link.isNullOrBlank() && attachment.studentVersion.isNullOrBlank()) return LectureMediaDestination.Missing
    return LectureMediaDestination.Slides(attachment, page)
}
