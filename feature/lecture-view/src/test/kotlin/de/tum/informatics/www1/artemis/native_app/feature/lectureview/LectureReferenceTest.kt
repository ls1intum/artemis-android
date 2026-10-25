package de.tum.informatics.www1.artemis.native_app.feature.lectureview

import de.tum.informatics.www1.artemis.native_app.core.model.lecture.*
import de.tum.informatics.www1.artemis.native_app.core.model.lecture.lecture_units.*
import org.junit.Assert.*
import org.junit.Test

@org.junit.experimental.categories.Category(de.tum.informatics.www1.artemis.native_app.core.common.test.UnitTest::class)
class LectureReferenceTest {
    private val attachment = Attachment(id = 4, link = "/attachments/attachment-unit/8/slides.pdf", attachmentType = Attachment.AttachmentType.FILE)
    private val unit = LectureUnitAttachmentVideo(id = 8, attachment = attachment, slides = listOf(LectureSlide(40, 1), LectureSlide(42, 3)))
    private val lectures = listOf(Lecture(id = 7, lectureUnits = listOf(unit)))
    @Test fun stableSlideIdResolvesActualLectureAndPdfPage() {
        assertEquals(ResolvedLectureReference(7, 8, null, 3), resolveLectureReference(lectures, "slide", "#42"))
    }
    @Test fun legacySlideReferencePreservesExplicitPage() {
        assertEquals(ResolvedLectureReference(7, 8, null, 3), resolveLectureReference(lectures, "slide", "attachment-unit/8/slide/3"))
    }
    @Test fun unitFileReferenceOpensUnitRatherThanLectureOverview() {
        assertEquals(ResolvedLectureReference(7, 8), resolveLectureReference(lectures, "lecture-unit", "attachment-unit/8/student/slides.pdf?version=1"))
    }
    @Test fun missingSlideOrUnitReportsMissing() {
        assertNull(resolveLectureReference(lectures, "slide", "#99"))
        assertNull(resolveLectureReference(lectures, "lecture-unit", "attachment-unit/99/a.pdf"))
    }
    @Test fun legacyAttachmentReferenceMatchesActualAttachment() {
        val legacy = attachment.copy(link = "/attachments/lecture/7/a.pdf")
        assertEquals(ResolvedLectureReference(7, attachmentId = 4), resolveLectureReference(listOf(Lecture(id = 7, attachments = listOf(legacy))), "attachment", "lecture/7/a.pdf"))
    }
    @Test fun studentPdfUsesVisiblePageIndexForStableAndLegacySlideReferences() {
        val studentUnit = unit.copy(attachment = attachment.copy(studentVersion = "/attachments/attachment-unit/8/student/slides.pdf"),
            slides = listOf(LectureSlide(80, 8), LectureSlide(40, 1), LectureSlide(42, 3)))
        val data = listOf(Lecture(id = 7, lectureUnits = listOf(studentUnit)))
        assertEquals(ResolvedLectureReference(7, 8, page = 3), resolveLectureReference(data, "slide", "#80"))
        assertEquals(ResolvedLectureReference(7, 8, page = 3), resolveLectureReference(data, "slide", "attachment-unit/8/slide/8"))
        assertNull(resolveLectureReference(data, "slide", "attachment-unit/8/slide/2"))
    }
    @Test fun legacyHiddenSlideCannotOpenOriginalPdfPage() {
        assertNull(resolveLectureReference(lectures, "slide", "attachment-unit/8/slide/2"))
    }
    @Test fun attachmentReferenceInsideAUnitOpensTheFileDirectly() {
        assertEquals(ResolvedLectureReference(7, attachmentId = 4), resolveLectureReference(lectures, "attachment", "attachment-unit/8/slides.pdf"))
    }
}
