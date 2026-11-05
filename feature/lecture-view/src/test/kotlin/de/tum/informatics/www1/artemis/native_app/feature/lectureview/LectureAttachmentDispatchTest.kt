package de.tum.informatics.www1.artemis.native_app.feature.lectureview

import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.junit4.StateRestorationTester
import de.tum.informatics.www1.artemis.native_app.core.model.lecture.Attachment
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
@org.junit.experimental.categories.Category(de.tum.informatics.www1.artemis.native_app.core.common.test.UnitTest::class)
class LectureAttachmentDispatchTest {
    @get:Rule val compose=createComposeRule()
    private val validated=Attachment(id=3,link="attachments/old.pdf?signature=private",studentVersion="attachments/student/v1.pdf",version=1,attachmentType=Attachment.AttachmentType.FILE)
    @Test fun initialSnapshotOwnsItsIdAndPageDespiteAnotherViewModelAttachment() {
        val dispatch=LectureAttachmentDispatch()
        dispatch.openTarget(validated.copy(id=4,studentVersion="attachments/student/v2.pdf",version=2),validated,3)
        assertEquals(3,dispatch.pending?.id)
        assertEquals(3,dispatch.pending?.page)
        assertEquals(validated,dispatch.resolve(validated,3,listOf(validated.copy(id=4))))
    }
    @Test fun recreationRetainsValidatedDocumentRequirementInsteadOfFallingThroughToReloadedBytes() {
        val restore=StateRestorationTester(compose)
        lateinit var dispatch:LectureAttachmentDispatch
        restore.setContent { dispatch=rememberSaveable(saver=LectureAttachmentDispatch.Saver){LectureAttachmentDispatch()} }
        compose.runOnIdle { dispatch.openTarget(validated,validated,3) }
        restore.emulateSavedInstanceStateRestore()
        compose.runOnIdle {
            assertEquals(validated,dispatch.resolve(validated,3,listOf(validated.copy(studentVersion="attachments/student/v2.pdf",version=2))))
            assertTrue(dispatch.pending!!.requiresSnapshot)
            assertFalse(dispatch.pending!!.revision!!.contains("private"))
        }
    }
    @Test fun changedRestoredSnapshotOrPageRequiresRevalidationBeforeOpen() {
        val dispatch=LectureAttachmentDispatch()
        dispatch.openTarget(validated,validated,3)
        val replacement=validated.copy(version=2,studentVersion="attachments/student/v2.pdf")
        assertNull(dispatch.resolve(replacement,3,listOf(replacement)))
        assertNull(dispatch.resolve(validated,2,listOf(validated)))
        dispatch.clear() // Retry performs a fresh reference lookup and dispatches its consistent pair.
        dispatch.openTarget(replacement,replacement,2)
        assertEquals(replacement,dispatch.resolve(replacement,2,listOf(validated)))
    }
}
