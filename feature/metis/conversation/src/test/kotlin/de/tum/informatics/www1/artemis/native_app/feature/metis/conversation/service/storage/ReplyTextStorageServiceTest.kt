package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.storage

import de.tum.informatics.www1.artemis.native_app.core.common.test.UnitTest
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.storage.impl.ReplyTextStorageServiceImpl
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

@Category(UnitTest::class)
@RunWith(RobolectricTestRunner::class)
class ReplyTextStorageServiceTest {
    @Test
    fun `older send cannot clear a newer identical draft written through another service instance`() = runTest {
        val older = ReplyTextStorageServiceImpl(RuntimeEnvironment.getApplication())
        val newer = ReplyTextStorageServiceImpl(RuntimeEnvironment.getApplication())
        older.updateStoredReplyText("revision.example", 9101, 2, 42, "Same body")
        val submitted = older.observeStoredReplyDraft("revision.example", 9101, 2, 42).first()
        newer.updateStoredReplyText("revision.example", 9101, 2, 42, "Interim body")
        newer.updateStoredReplyText("revision.example", 9101, 2, 42, "Same body")

        val surviving = older.clearStoredReplyDraftAfterSend("revision.example", 9101, 2, 42, submitted)

        assertEquals("Same body", surviving.text)
        assertNotEquals(submitted.revision, surviving.revision)
        assertEquals(surviving, older.observeStoredReplyDraft("revision.example", 9101, 2, 42).first())
    }

    @Test
    fun `accepted revision clears only its originating host and thread`() = runTest {
        val storage = ReplyTextStorageServiceImpl(RuntimeEnvironment.getApplication())
        storage.updateStoredReplyText("origin.example", 9102, 2, 42, "Sent body")
        storage.updateStoredReplyText("origin.example", 9102, 2, null, "Conversation draft")
        storage.updateStoredReplyText("origin.example", 9102, 2, 43, "Other thread")
        storage.updateStoredReplyText("other.example", 9102, 2, 42, "Other server")
        val submitted = storage.observeStoredReplyDraft("origin.example", 9102, 2, 42).first()

        val surviving = storage.clearStoredReplyDraftAfterSend("origin.example", 9102, 2, 42, submitted)

        assertEquals("", surviving.text)
        assertEquals("", storage.getStoredReplyText("origin.example", 9102, 2, 42))
        assertEquals("Conversation draft", storage.getStoredReplyText("origin.example", 9102, 2, null))
        assertEquals("Other thread", storage.getStoredReplyText("origin.example", 9102, 2, 43))
        assertEquals("Other server", storage.getStoredReplyText("other.example", 9102, 2, 42))
    }
}
