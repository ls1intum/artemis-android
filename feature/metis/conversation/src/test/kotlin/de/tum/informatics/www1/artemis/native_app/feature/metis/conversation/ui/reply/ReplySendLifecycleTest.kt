package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply

import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.input.TextFieldValue
import de.tum.informatics.www1.artemis.native_app.core.common.test.UnitTest
import de.tum.informatics.www1.artemis.native_app.core.test.BaseComposeTest
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.shared.service.MetisModificationFailure
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.IBasePost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.StandalonePost
import kotlinx.coroutines.CompletableDeferred
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertEquals
import kotlin.test.assertIs

@Category(UnitTest::class)
@RunWith(RobolectricTestRunner::class)
class ReplySendLifecycleTest : BaseComposeTest() {
    @Test
    fun `completed send preserves text entered while sending`() {
        val text = mutableStateOf(TextFieldValue("Sent message"))
        val task = CompletableDeferred<MetisModificationFailure?>()
        val mode = ReplyMode.NewMessage(text, { text.value = it }) { task }
        lateinit var state: ReplyState
        composeTestRule.setContent { state = rememberReplyState(mode) {} }
        composeTestRule.runOnIdle { assertIs<ReplyState.CanCreate>(state).onCreateReply() }
        composeTestRule.runOnIdle { text.value = TextFieldValue("Next draft") }
        composeTestRule.runOnIdle { task.complete(null) }
        composeTestRule.runOnIdle { assertEquals("Next draft", text.value.text) }
    }

    @Test
    fun `completed send cannot clear the newly selected thread`() {
        val original = mutableStateOf(TextFieldValue("Sent message"))
        val other = mutableStateOf(TextFieldValue("Other thread draft"))
        val task = CompletableDeferred<MetisModificationFailure?>()
        // Production provider callbacks update their currently selected draft, even when an old
        // ReplyMode instance survives in the pending completion effect.
        var activeText = original
        val updateCurrentDraft: (TextFieldValue) -> Unit = { activeText.value = it }
        val mode = mutableStateOf<ReplyMode>(ReplyMode.NewMessage(original, updateCurrentDraft) { task })
        lateinit var state: ReplyState
        composeTestRule.setContent { state = rememberReplyState(mode.value) {} }
        composeTestRule.runOnIdle { assertIs<ReplyState.CanCreate>(state).onCreateReply() }
        composeTestRule.runOnIdle {
            activeText = other
            mode.value = ReplyMode.NewMessage(other, updateCurrentDraft) { CompletableDeferred() }
        }
        composeTestRule.runOnIdle { task.complete(null) }
        composeTestRule.runOnIdle { assertEquals("Other thread draft", other.value.text) }
    }

    @Test
    fun `older successful edit cannot dismiss a reopened edit of the same post`() {
        val task = CompletableDeferred<MetisModificationFailure?>()
        val actions = MetisReplyHandlerInputActions.empty<IBasePost>().copy(edit = { _, _ -> task })
        val provider = de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.TestInitialReplyTextProvider()
        val post = StandalonePost(id = 42, content = "Before")
        lateinit var mode: ReplyMode
        lateinit var output: MetisReplyHandlerOutputActions<IBasePost>
        composeTestRule.setContent {
            MetisReplyHandler(provider, actions) { value, delegate, _ ->
                SideEffect { mode = value; output = delegate }
            }
        }
        composeTestRule.runOnIdle { output.edit(post) }
        composeTestRule.runOnIdle {
            val editing = assertIs<ReplyMode.EditMessage>(mode)
            editing.onEditMessage()
            editing.onCancelEditMessage()
        }
        composeTestRule.runOnIdle { output.edit(post) }
        composeTestRule.runOnIdle { mode.onUpdate(TextFieldValue("Reopened edit")) }
        composeTestRule.runOnIdle { task.complete(null) }
        composeTestRule.runOnIdle {
            assertIs<ReplyMode.EditMessage>(mode)
            assertEquals("Reopened edit", mode.currentText.value.text)
        }
    }

    @Test
    fun `upload completing during save keeps the newer edit open`() {
        checkEditRevisionAfterSave(returnToOriginal = false)
    }

    @Test
    fun `same body with a newer edit revision is retained after save`() {
        checkEditRevisionAfterSave(returnToOriginal = true)
    }

    private fun checkEditRevisionAfterSave(returnToOriginal: Boolean) {
        val task = CompletableDeferred<MetisModificationFailure?>()
        val actions = MetisReplyHandlerInputActions.empty<IBasePost>().copy(edit = { _, _ -> task })
        val provider = de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.TestInitialReplyTextProvider()
        lateinit var mode: ReplyMode
        lateinit var output: MetisReplyHandlerOutputActions<IBasePost>
        composeTestRule.setContent {
            MetisReplyHandler(provider, actions) { value, delegate, _ ->
                SideEffect { mode = value; output = delegate }
            }
        }
        composeTestRule.runOnIdle { output.edit(StandalonePost(id = 42, content = "Original")) }
        lateinit var editing: ReplyMode.EditMessage
        composeTestRule.runOnIdle {
            editing = assertIs<ReplyMode.EditMessage>(mode)
            editing.onEditMessage()
        }
        composeTestRule.runOnIdle {
            // This is the callback captured when a file picker was launched before Save.
            val uploaded = de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.util.MarkdownEditEngine.insertMarkup(
                editing.currentText.value, " [report.pdf](/api/core/files/report.pdf)"
            )
            editing.onUpdate(uploaded)
            if (returnToOriginal) editing.onUpdate(TextFieldValue("Original"))
        }
        val expected = editing.currentText.value.text
        composeTestRule.runOnIdle { task.complete(null) }
        composeTestRule.runOnIdle {
            assertIs<ReplyMode.EditMessage>(mode)
            assertEquals(expected, mode.currentText.value.text)
        }
    }

    @Test
    fun `failed edit remains open with entered text`() {
        val task = CompletableDeferred<MetisModificationFailure?>()
        val actions = MetisReplyHandlerInputActions.empty<IBasePost>().copy(edit = { _, _ -> task })
        val provider = de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.TestInitialReplyTextProvider()
        lateinit var mode: ReplyMode
        lateinit var output: MetisReplyHandlerOutputActions<IBasePost>
        composeTestRule.setContent {
            MetisReplyHandler(provider, actions) { value, delegate, _ ->
                SideEffect { mode = value; output = delegate }
            }
        }
        composeTestRule.runOnIdle { output.edit(StandalonePost(id = 42, content = "Before")) }
        composeTestRule.runOnIdle {
            mode.onUpdate(TextFieldValue("Keep failed edit"))
            assertIs<ReplyMode.EditMessage>(mode).onEditMessage()
        }
        composeTestRule.runOnIdle { task.complete(MetisModificationFailure.UPDATE_POST) }
        composeTestRule.runOnIdle {
            assertIs<ReplyMode.EditMessage>(mode)
            assertEquals("Keep failed edit", mode.currentText.value.text)
        }
    }
}
