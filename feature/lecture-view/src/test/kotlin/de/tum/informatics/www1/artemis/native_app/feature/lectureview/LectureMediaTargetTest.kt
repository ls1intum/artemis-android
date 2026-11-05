package de.tum.informatics.www1.artemis.native_app.feature.lectureview

import de.tum.informatics.www1.artemis.native_app.core.model.lecture.Attachment
import de.tum.informatics.www1.artemis.native_app.core.model.lecture.Lecture
import de.tum.informatics.www1.artemis.native_app.core.model.lecture.lecture_units.LectureUnitAttachmentVideo
import org.junit.Assert.*
import org.junit.Test

@org.junit.experimental.categories.Category(de.tum.informatics.www1.artemis.native_app.core.common.test.UnitTest::class)
class LectureMediaTargetTest {
    private val slides = Attachment(id = 4, link = "/attachments/lecture/1/slides.pdf", attachmentType = Attachment.AttachmentType.FILE)
    private val unit = LectureUnitAttachmentVideo(id = 8, attachment = slides)
    private val lecture = Lecture(id = 1, attachments = listOf(slides), lectureUnits = listOf(unit))
    @Test fun unitReferenceOpensActualUnit() {
        assertEquals(LectureMediaDestination.Unit(unit), resolveLectureMediaTarget(lecture, listOf(unit), 8, null, null))
    }
    @Test fun slideReferenceOpensUnitAttachmentAtSpecifiedPage() {
        assertEquals(LectureMediaDestination.Slides(slides, 3), resolveLectureMediaTarget(lecture, listOf(unit), 8, null, 3))
    }
    @Test fun attachmentReferenceFindsActualAttachment() {
        assertEquals(LectureMediaDestination.Slides(slides, null), resolveLectureMediaTarget(lecture, listOf(unit), null, 4, null))
    }
    @Test fun missingOrUnreleasedUnitCannotOpenOverviewInstead() {
        assertEquals(LectureMediaDestination.Missing, resolveLectureMediaTarget(lecture, emptyList(), 8, null, 3))
        assertEquals(LectureMediaDestination.Missing, resolveLectureMediaTarget(lecture, listOf(unit), null, 99, 3))
    }
    @Test fun attachmentWithoutAFileCannotOpenABlankViewer() {
        val missing = unit.copy(attachment = slides.copy(link = null))
        assertEquals(LectureMediaDestination.Missing, resolveLectureMediaTarget(lecture, listOf(missing), 8, null, 3))
    }
}
