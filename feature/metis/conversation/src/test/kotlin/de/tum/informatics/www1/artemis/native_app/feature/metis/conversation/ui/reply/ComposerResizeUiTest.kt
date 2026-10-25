package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.focus.FocusRequester
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.emoji_picker.service.impl.EmojiServiceStub
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.IBasePost
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.common.test.UnitTest
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.BaseChatUITest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@Category(UnitTest::class)
@RunWith(RobolectricTestRunner::class)
class ComposerResizeUiTest : BaseChatUITest() {
    private fun handle() = composeTestRule.onNodeWithContentDescription("Resize message input")
    private fun field() = composeTestRule.onNodeWithTag(TEST_TAG_MARKDOWN_TEXTFIELD)
    private fun editorHeight() = field().fetchSemanticsNode().boundsInRoot.height

    @Test fun `discontinuous collapse returns a short draft to its natural height`() {
        val extra = mutableStateOf(240.dp)
        composeTestRule.setContent {
            BasicMarkdownTextField(
                modifier = Modifier.fillMaxWidth(), textFieldValue = TextFieldValue("A draft"),
                hintText = AnnotatedString("Message"), focusRequester = remember { FocusRequester() },
                extraHeight = extra.value, onTextChanged = {},
            )
        }
        assertTrue(editorHeight() > 200f)
        composeTestRule.runOnIdle { extra.value = 0.dp }
        assertEquals(48f, editorHeight(), 1f)
    }

    @Test fun `horizontal dominant diagonal drag does not expand a compact editor`() {
        setupReplyTextField(Modifier.fillMaxWidth().heightIn(max = 400.dp), initialText = "A draft")
        val collapsed = editorHeight()
        handle().performTouchInput { swipe(center, center + Offset(200f, -100f), 500) }
        assertEquals(collapsed, editorHeight(), 1f)
    }

    @Test fun `constrained attachment draft keeps a usable editor and full send control`() {
        val source = (1..12).joinToString("\n") { "Line $it" } + "\n![photo](/api/files/attachments/photo.png)"
        setupReplyTextField(Modifier.fillMaxWidth().heightIn(max = 220.dp), initialText = source)
        assertTrue("Editor height ${editorHeight()}", editorHeight() >= 40f)
        composeTestRule.onNodeWithTag(TEST_TAG_REPLY_SEND_BUTTON).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TEST_TAG_REPLY_SEND_BUTTON).assertHeightIsAtLeast(48.dp)
        handle().assertDoesNotExist()
    }

    @Test fun `reply handler restoration waits for the real cold-flow editor session`() {
        val restoration = StateRestorationTester(composeTestRule)
        val provider = object : InitialReplyTextProvider {
            override val editorSessionKey = flow<Any?> { emit("resolved-conversation") }
            override val newMessageText = flowOf(TextFieldValue("A draft"))
            override fun updateInitialReplyText(text: TextFieldValue) = Unit
        }
        restoration.setContent {
            MetisReplyHandler(provider, MetisReplyHandlerInputActions.empty<IBasePost>()) { mode, _, updateFailure ->
                ReplyTextField(
                    modifier = Modifier.fillMaxWidth(), replyMode = mode, conversationName = "Conversation",
                    onFileSelected = { _, _ -> }, updateFailureState = updateFailure, emojiService = EmojiServiceStub,
                )
            }
        }
        handle().performClick()
        handle().assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "Expanded"))
        restoration.emulateSavedInstanceStateRestore()
        handle().assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "Expanded"))
    }

    @Test fun `large text leaves expansion room for a long draft`() {
        val source = TextFieldValue((1..20).joinToString("\n") { "Line $it" })
        composeTestRule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, 1.5f)) {
                val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {}
                MarkdownTextField(
                    modifier = Modifier.fillMaxWidth(), textFieldValue = source, editorSession = "large-text",
                    hintText = AnnotatedString("Message"), filePickerLauncher = picker, onTextChanged = {},
                )
            }
        }
        val collapsed = editorHeight()
        handle().performClick()
        assertTrue("Collapsed $collapsed, expanded ${editorHeight()}", editorHeight() > collapsed + 30f)
    }

    @Test fun `up and down drags resize while leaving send reachable`() {
        setupReplyTextField(initialText = "A draft")
        val collapsed = editorHeight()
        handle().performTouchInput { swipe(center, center - Offset(0f, 100f), 400) }
        assertTrue("Collapsed $collapsed, expanded ${editorHeight()}", editorHeight() > collapsed + 40f)
        composeTestRule.onNodeWithTag(TEST_TAG_REPLY_SEND_BUTTON).assertIsDisplayed()
        handle().performTouchInput { swipe(center, center + Offset(0f, 100f), 400) }
        assertEquals(collapsed, editorHeight(), 1f)
    }

    @Test fun `horizontal drag leaves composer size unchanged`() {
        setupReplyTextField(initialText = "A draft")
        val collapsed = editorHeight()
        handle().performTouchInput { swipe(center, center + Offset(120f, 8f), 400) }
        assertEquals(collapsed, editorHeight(), 1f)
    }

    @Test fun `tap preserves focused selection and exact formatted source`() {
        var source = TextFieldValue()
        setupReplyTextField(initialText = "**Hello** Artemis", onSourceChanged = { source = it })
        field().performClick().performTextInputSelection(TextRange(1, 8))
        val before = source
        handle().performClick()
        field().assertIsFocused().assertTextEquals("Hello Artemis")
        field().assert(SemanticsMatcher.expectValue(SemanticsProperties.TextSelectionRange, TextRange(1, 8)))
        assertEquals(before, source)
        handle().performClick()
        field().assertIsFocused()
        assertEquals(before, source)
    }

    @Test fun `accessible expand and collapse have the same height effect`() {
        setupReplyTextField()
        val collapsed = editorHeight()
        handle().performSemanticsAction(SemanticsActions.Expand) { it() }
        handle().assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "Expanded"))
        assertTrue("Collapsed $collapsed, expanded ${editorHeight()}", editorHeight() > collapsed + 40f)
        handle().performSemanticsAction(SemanticsActions.Collapse) { it() }
        handle().assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "Collapsed"))
        assertEquals(collapsed, editorHeight(), 1f)
    }

    @Test fun `short windows retain editor and send while hiding the grabber`() {
        val height = mutableStateOf(400.dp)
        setupReplyTextField(Modifier.composed { Modifier.fillMaxWidth().heightIn(max = height.value) }, initialText = "A draft")
        handle().performClick()
        composeTestRule.runOnIdle { height.value = 160.dp }
        handle().assertDoesNotExist()
        field().assertIsDisplayed()
        composeTestRule.onNodeWithTag(TEST_TAG_REPLY_SEND_BUTTON).assertIsDisplayed()
        composeTestRule.runOnIdle { height.value = 400.dp }
        handle().assertIsDisplayed()
    }

    @Test fun `expansion survives recreation but resets for another editor session`() {
        val restoration = StateRestorationTester(composeTestRule)
        val document = mutableStateOf(TextFieldValue("A draft", TextRange(3), TextRange(0, 1)))
        val session = mutableStateOf("first")
        val content: @Composable () -> Unit = {
            val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {}
            MarkdownTextField(
                modifier = Modifier.fillMaxWidth(), textFieldValue = document.value,
                editorSession = session.value, hintText = AnnotatedString("Message"),
                filePickerLauncher = picker, onTextChanged = { document.value = it },
            )
        }
        restoration.setContent(content)
        val collapsed = editorHeight()
        val before = document.value
        handle().performClick()
        assertTrue("Collapsed $collapsed, expanded ${editorHeight()}", editorHeight() > collapsed + 40f)
        restoration.emulateSavedInstanceStateRestore()
        assertTrue("Collapsed $collapsed, expanded ${editorHeight()}", editorHeight() > collapsed + 40f)
        assertEquals(before, document.value)
        composeTestRule.runOnIdle { session.value = "second" }
        handle().assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "Collapsed"))
        assertEquals(collapsed, editorHeight(), 1f)
        assertEquals(before, document.value)
    }
}
