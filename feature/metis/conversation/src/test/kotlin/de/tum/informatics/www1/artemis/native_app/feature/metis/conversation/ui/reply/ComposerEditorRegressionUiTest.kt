package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply

import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.text.InputType
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.InterceptPlatformTextInput
import androidx.compose.ui.platform.PlatformTextInputInterceptor
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.common.test.UnitTest
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.BaseChatUITest
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.util.MarkdownEditEngine
import org.junit.Assert.*
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode

@Category(UnitTest::class)
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@OptIn(ExperimentalComposeUiApi::class)
class ComposerEditorRegressionUiTest : BaseChatUITest() {
    private fun field() = composeTestRule.onNodeWithTag(TEST_TAG_MARKDOWN_TEXTFIELD)

    private fun layout(): TextLayoutResult {
        val results = mutableListOf<TextLayoutResult>()
        field().performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(results) }
        return results.single()
    }

    private fun assertLines(vararg lines: String) {
        val result = layout()
        assertEquals("Rendered paragraphs must match the entered lines", lines.size, result.lineCount)
        lines.forEachIndexed { index, expected ->
            assertEquals(expected, result.layoutInput.text.text.substring(result.getLineStart(index), result.getLineEnd(index)).trimEnd('\n', '\u200B'))
            if (index > 0) assertEquals("No gap between text lines", result.getLineBottom(index - 1), result.getLineTop(index), 0.1f)
        }
    }

    @Test fun `quote Enter adds exactly one rendered line and retains the caret`() {
        var source = TextFieldValue()
        setupReplyTextField(initialText = "> hello", onSourceChanged = { source = it })
        field().performClick().performTextInputSelection(TextRange(5))
        field().performTextInput("\n")
        field().assertTextEquals("hello\n").assertIsFocused()
        assertEquals("> hello\n> ", source.text)
        assertEquals(TextRange(6), MarkdownEditEngine.visibleValue(source).selection)
        assertLines("hello", "")
        assertEquals(layout().getCursorRect(0).left, layout().getCursorRect(6).left, 0.1f)
        field().performTextInput("world")
        assertEquals("> hello\n> world", source.text)
        assertLines("hello", "world")
    }

    @Test fun `three quoted paragraphs occupy three lines`() {
        setupReplyTextField(initialText = "> first\n> second\n> third")
        field().assertTextEquals("first\nsecond\nthird")
        assertLines("first", "second", "third")
        val result = layout()
        assertEquals(result.getCursorRect(0).left, result.getCursorRect(result.getLineStart(1)).left, 0.1f)
        assertTrue("Quote bodies remain indented", result.getCursorRect(0).left > 0f)
    }

    @Test fun `empty quote has one editable indented line`() {
        setupReplyTextField(initialText = "> ")
        assertLines("")
        assertTrue("The empty quote caret must clear the quote bar", layout().getCursorRect(0).left > 0f)
        field().performClick()
        field().performTextInput("hello")
        field().assertTextEquals("hello").assertIsFocused()
        assertLines("hello")
    }

    @Test fun `empty quote after plain text adds only its intended line`() {
        setupReplyTextField(initialText = "before\n\n> ")
        assertLines("before", "", "")
        assertTrue(layout().getCursorRect(8).left > layout().getCursorRect(0).left)
        field().performClick().performTextInputSelection(TextRange(8))
        field().performTextInput("quoted")
        assertLines("before", "", "quoted")
    }

    @Test fun `quote between ordinary paragraphs adds no phantom line`() {
        setupReplyTextField(initialText = "before\n\n> quoted\n\nafter")
        assertLines("before", "", "quoted", "", "after")
        val result = layout()
        assertTrue(result.getCursorRect(result.getLineStart(2)).left > result.getCursorRect(0).left)
        assertEquals(result.getCursorRect(0).left, result.getCursorRect(result.getLineStart(4)).left, 0.1f)
    }

    @Test fun `Enter on empty quote continuation exits without phantom paragraphs`() {
        var source = TextFieldValue()
        setupReplyTextField(initialText = "> hello\n> ", onSourceChanged = { source = it })
        field().performClick().performTextInputSelection(TextRange(6))
        field().performTextInput("\n")
        assertEquals("> hello\n\n", source.text)
        field().performTextInput("after")
        assertEquals("> hello\n\nafter", source.text)
        assertLines("hello", "", "after")
        assertEquals(0f, layout().getLineLeft(2), 0.1f)
    }

    @Test fun `splitting a quote retains body formatting without extra rendered lines`() {
        var source = TextFieldValue()
        setupReplyTextField(initialText = "> **hello world**", onSourceChanged = { source = it })
        field().performClick().performTextInputSelection(TextRange(6))
        field().performTextInput("\n")
        field().assertTextEquals("hello \nworld")
        assertLines("hello ", "world")
        assertEquals(TextRange(7), MarkdownEditEngine.visibleValue(source).selection)
        field().performTextInput("new ")
        field().assertTextEquals("hello \nnew world").assertIsFocused()
        assertLines("hello ", "new world")
    }

    @Test fun `long press selects a word in the real composer and replacement keeps the remaining text`() {
        var source = TextFieldValue()
        setupReplyTextField(initialText = "hello world", onSourceChanged = { source = it })
        field().performClick()
        val word = layout().getBoundingBox(1).center
        field().performTouchInput { longClick(word) }
        field().assertIsFocused().assertTextEquals("hello world")
        field().assert(SemanticsMatcher.expectValue(SemanticsProperties.TextSelectionRange, TextRange(0, 5)))
        assertEquals("hello world", source.text)
        field().performTextInput("Hi")
        field().assertTextEquals("Hi world")
        assertEquals("Hi world", source.text)
        composeTestRule.onNodeWithTag(TEST_TAG_REPLY_SEND_BUTTON).assertIsDisplayed().assertIsEnabled()
    }

    @Test fun `long press in formatted text preserves exact source and selected visible word`() {
        var source = TextFieldValue()
        setupReplyTextField(initialText = "**hello** world", onSourceChanged = { source = it })
        field().performClick()
        val word = layout().getBoundingBox(1).center
        field().performTouchInput { longClick(word) }
        field().assert(SemanticsMatcher.expectValue(SemanticsProperties.TextSelectionRange, TextRange(0, 5)))
        assertEquals("**hello** world", source.text)
        assertEquals(TextRange(0, 5), MarkdownEditEngine.visibleValue(source).selection)
        field().performTextInput("Hi")
        assertEquals("**Hi** world", source.text)
        field().assertTextEquals("Hi world").assertIsFocused()
    }

    @Test fun `reversed selection across emoji and formatting replaces the intended visible range`() {
        var source = TextFieldValue()
        setupReplyTextField(initialText = "**Hi 👋** world", onSourceChanged = { source = it })
        field().performClick().performTextInputSelection(TextRange(8, 3))
        assertEquals(3, MarkdownEditEngine.visibleValue(source).selection.min)
        assertEquals(8, MarkdownEditEngine.visibleValue(source).selection.max)
        assertEquals("**Hi 👋** world", source.text)
        field().performTextInput("there ")
        field().assertTextEquals("Hi there rld")
        assertEquals(TextRange(9), MarkdownEditEngine.visibleValue(source).selection)
    }

    @Test fun `selecting part of a reference snaps atomically and keeps its payload`() {
        var source = TextFieldValue()
        setupReplyTextField(initialText = "Hi [user]Ada Lovelace(ada)[/user]!", onSourceChanged = { source = it })
        field().performClick().performTextInputSelection(TextRange(5, 7))
        field().assertTextEquals("Hi Ada Lovelace!")
        assertEquals("Hi [user]Ada Lovelace(ada)[/user]!", source.text)
        assertEquals(TextRange(3, 15), MarkdownEditEngine.visibleValue(source).selection)
        field().performTextInput("Grace")
        field().assertTextEquals("Hi Grace!")
        assertEquals("Hi Grace!", source.text)
    }

    @Test fun `selection in a multiline quote can be replaced without damaging the quote`() {
        var source = TextFieldValue()
        setupReplyTextField(initialText = "> first\n> second", onSourceChanged = { source = it })
        field().performClick().performTextInputSelection(TextRange(2, 9))
        field().performTextInput("X")
        field().assertTextEquals("fiXond").assertIsFocused()
        assertEquals("> fiXond", source.text)
        assertLines("fiXond")
    }

    @Test fun `copying across quote style boundaries keeps real newlines and never copies visual separators`() {
        val text = "before\n\nquoted\n\nafter"
        var source = TextFieldValue()
        setupReplyTextField(initialText = "before\n\n> quoted\n\nafter", onSourceChanged = { source = it })
        field().performClick().performTextInputSelection(TextRange(0, text.length))
        assertEquals(TextRange(0, text.length), MarkdownEditEngine.visibleValue(source).selection)
        field().performSemanticsAction(SemanticsActions.CopyText) { it() }
        composeTestRule.runOnIdle {
            val clipboard = androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().targetContext
                .getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
            assertEquals(text, clipboard.primaryClip!!.getItemAt(0).text.toString())
        }
        assertEquals("before\n\n> quoted\n\nafter", source.text)
        field().assertTextEquals(text)
        assertLines("before", "", "quoted", "", "after")
    }

    private fun assertCodeKeepsInputSession(label: String, initial: String = "draft", selection: TextRange = TextRange(5)) {
        val source = mutableStateOf(TextFieldValue(initial, selection))
        var sessions = 0
        val inputs = mutableListOf<Int>()
        val interceptor = PlatformTextInputInterceptor { request, next ->
            sessions++
            val info = EditorInfo()
            request.createInputConnection(info)
            inputs += info.inputType
            next.startInputMethod(request)
        }
        composeTestRule.setContent {
            val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {}
            InterceptPlatformTextInput(interceptor) {
                MarkdownTextField(
                    modifier = Modifier.fillMaxWidth(), textFieldValue = source.value,
                    hintText = AnnotatedString("Message"), filePickerLauncher = picker,
                    onTextChanged = { source.value = it },
                )
            }
        }
        field().performClick().performTextInputSelection(selection)
        composeTestRule.onNodeWithContentDescription("Show formatting").performClick()
        val before = sessions
        assertTrue("A real platform input session must be active", before > 0)
        val control = composeTestRule.onNodeWithContentDescription(label)
        control.performScrollTo().performClick()
        if (label == "Code block") {
            assertTrue("Code block must be active at the preserved caret", MarkdownStyle.CodeBlock in MarkdownEditEngine.activeMarks(source.value))
        } else control.assertIsSelected()
        field().assertIsFocused().assertTextEquals(initial)
        assertEquals("Formatting must retain the active IME session", before, sessions)
        control.performClick().assertIsNotSelected()
        assertEquals("Removing formatting must retain the active IME session", before, sessions)
        assertEquals("IME configuration must stay stable", 1, inputs.distinct().size)
        field().performTextInput("!")
        field().assertIsFocused()
        assertEquals(initial.replaceRange(selection.min, selection.max, "!"), MarkdownEditEngine.visibleValue(source.value).text)
    }

    @Test fun `toggling inline code at the caret retains the input session`() = assertCodeKeepsInputSession("Inline code")

    @Test fun `toggling inline code on a selection retains the input session`() =
        assertCodeKeepsInputSession("Inline code", selection = TextRange(0, 5))

    @Test fun `toggling code block retains the input session`() = assertCodeKeepsInputSession("Code block")

    @Test fun `code block can be toggled while the software keyboard is composing a word`() {
        val source = mutableStateOf(TextFieldValue())
        var connection: InputConnection? = null
        var sessions = 0
        val interceptor = PlatformTextInputInterceptor { request, next ->
            sessions++
            connection = request.createInputConnection(EditorInfo())
            next.startInputMethod(request)
        }
        composeTestRule.setContent {
            val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {}
            InterceptPlatformTextInput(interceptor) {
                MarkdownTextField(
                    modifier = Modifier.fillMaxWidth(), textFieldValue = source.value,
                    hintText = AnnotatedString("Message"), filePickerLauncher = picker,
                    onTextChanged = { source.value = it },
                )
            }
        }
        field().performClick()
        composeTestRule.runOnIdle { assertTrue(connection!!.setComposingText("hello", 1)) }
        composeTestRule.waitForIdle()
        assertEquals(TextRange(0, 5), MarkdownEditEngine.visibleValue(source.value).composition)
        composeTestRule.onNodeWithContentDescription("Show formatting").performClick()
        val before = sessions
        composeTestRule.onNodeWithContentDescription("Code block").performScrollTo().performClick()
        assertEquals("```\nhello\n```", source.value.text)
        assertEquals(TextRange(0, 5), MarkdownEditEngine.visibleValue(source.value).composition)
        assertEquals(before, sessions)
        composeTestRule.runOnIdle { assertTrue(connection!!.setComposingText("hello!", 1)) }
        composeTestRule.waitForIdle()
        field().assertTextEquals("hello!").assertIsFocused()
        assertEquals("```\nhello!\n```", source.value.text)
        composeTestRule.runOnIdle { assertTrue(connection!!.finishComposingText()) }
        composeTestRule.waitForIdle()
        assertNull(source.value.composition)
        assertEquals(TextRange(6), MarkdownEditEngine.visibleValue(source.value).selection)
        assertEquals(before, sessions)
    }

    @Test fun `new focus sessions adopt code and prose keyboard options without changing an active session`() {
        val source = mutableStateOf(TextFieldValue("hello", TextRange(5)))
        val inputTypes = mutableListOf<Int>()
        lateinit var focusManager: FocusManager
        val interceptor = PlatformTextInputInterceptor { request, next ->
            val info = EditorInfo()
            request.createInputConnection(info)
            inputTypes += info.inputType
            next.startInputMethod(request)
        }
        composeTestRule.setContent {
            focusManager = LocalFocusManager.current
            val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {}
            InterceptPlatformTextInput(interceptor) {
                MarkdownTextField(
                    modifier = Modifier.fillMaxWidth(), textFieldValue = source.value,
                    hintText = AnnotatedString("Message"), filePickerLauncher = picker,
                    onTextChanged = { source.value = it },
                )
            }
        }
        field().performClick()
        val prose = inputTypes.single()
        assertTrue(prose and InputType.TYPE_TEXT_FLAG_CAP_SENTENCES != 0)
        assertTrue(prose and InputType.TYPE_TEXT_FLAG_AUTO_CORRECT != 0)
        composeTestRule.onNodeWithContentDescription("Show formatting").performClick()
        val code = composeTestRule.onNodeWithContentDescription("Code block")
        code.performScrollTo().performClick()
        assertEquals(1, inputTypes.size)
        composeTestRule.runOnIdle { focusManager.clearFocus(force = true) }
        field().assertIsNotFocused().performClick()
        assertEquals(2, inputTypes.size)
        assertEquals("Refocused code: ${source.value}; IME types $inputTypes", 0, inputTypes.last() and InputType.TYPE_TEXT_FLAG_AUTO_CORRECT)
        assertEquals(0, inputTypes.last() and InputType.TYPE_TEXT_FLAG_CAP_SENTENCES)
        code.performClick()
        assertEquals(2, inputTypes.size)
        composeTestRule.runOnIdle { focusManager.clearFocus(force = true) }
        field().assertIsNotFocused().performClick()
        assertEquals(3, inputTypes.size)
        assertEquals(prose, inputTypes.last())
        field().assertIsFocused().assertTextEquals("hello")
    }

    private fun assertCaretMovementKeepsInputSession(initial: String, vararg positions: Int) {
        val source = mutableStateOf(TextFieldValue(initial))
        var sessions = 0
        val interceptor = PlatformTextInputInterceptor { request, next ->
            sessions++
            next.startInputMethod(request)
        }
        composeTestRule.setContent {
            val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {}
            InterceptPlatformTextInput(interceptor) {
                MarkdownTextField(
                    modifier = Modifier.fillMaxWidth(), textFieldValue = source.value,
                    hintText = AnnotatedString("Message"), filePickerLauncher = picker,
                    onTextChanged = { source.value = it },
                )
            }
        }
        field().performClick().performTextInputSelection(TextRange(0))
        val before = sessions
        assertTrue(before > 0)
        positions.forEach { position ->
            field().performTextInputSelection(TextRange(position))
            assertEquals(TextRange(position), MarkdownEditEngine.visibleValue(source.value).selection)
            assertEquals(initial, source.value.text)
            field().assertIsFocused()
            assertEquals("Moving across a code boundary must not restart the IME", before, sessions)
        }
    }

    @Test fun `moving the caret into and out of inline code keeps the keyboard session`() =
        assertCaretMovementKeepsInputSession("before `code` after", 8, 12, 7, 0)

    @Test fun `moving the caret into and out of fenced code keeps the keyboard session`() =
        assertCaretMovementKeepsInputSession("before\n\n```\ncode\n```\n\nafter", 9, 15, 8, 0)

    @Test fun `wrapped quote contains no empty visual paragraphs`() {
        setupReplyTextField(Modifier.width(180.dp), initialText = "> a long quoted paragraph that wraps onto several lines\n> next")
        val result = layout()
        assertTrue("The test must exercise wrapping", result.lineCount > 2)
        for (line in 0 until result.lineCount) {
            assertTrue("Unexpected empty visual paragraph at line $line", result.getLineEnd(line) > result.getLineStart(line))
            if (line > 0) assertEquals(result.getLineBottom(line - 1), result.getLineTop(line), 0.1f)
        }
        assertEquals("next", result.layoutInput.text.text.substring(result.getLineStart(result.lineCount - 1)))
        composeTestRule.onNodeWithTag(TEST_TAG_REPLY_SEND_BUTTON).assertIsDisplayed()
    }

    @Test fun `long quoted draft keeps one visual paragraph per entered line`() {
        val lines = (1..20).map { "Quote $it" }
        setupReplyTextField(initialText = lines.joinToString("\n") { "> $it" })
        assertLines(*lines.toTypedArray())
        field().performClick().performTextInputSelection(TextRange(lines.joinToString("\n").length))
        field().assertIsFocused()
        composeTestRule.onNodeWithTag(TEST_TAG_REPLY_SEND_BUTTON).assertIsDisplayed().assertIsEnabled()
    }

    @Test fun `empty composer leaves a compact gap between grabber and placeholder`() {
        setupReplyTextField()
        val handle = composeTestRule.onNodeWithContentDescription("Resize message input").fetchSemanticsNode().boundsInRoot
        val editor = field().fetchSemanticsNode().boundsInRoot
        val gapLimit = with(composeTestRule.density) { 24.dp.toPx() }
        val touchMinimum = with(composeTestRule.density) { 48.dp.toPx() }
        assertTrue("Grabber must not reserve a large blank band: handle=$handle editor=$editor", editor.top - handle.center.y <= gapLimit)
        val touch = composeTestRule.onNodeWithContentDescription("Resize message input").fetchSemanticsNode().touchBoundsInRoot
        assertTrue("The compact grabber must retain a full touch target", touch.height >= touchMinimum)
        assertTrue("Grabber must not intercept text selection", touch.bottom <= editor.top + 0.1f)
        field().assertHeightIsAtLeast(48.dp)
        composeTestRule.onNodeWithTag(TEST_TAG_REPLY_SEND_BUTTON).assertIsDisplayed().assertIsNotEnabled()
    }

    @Test fun `compact grabber still expands and collapses without changing selection`() {
        var source = TextFieldValue()
        setupReplyTextField(initialText = "**hello** world", onSourceChanged = { source = it })
        field().performClick().performTextInputSelection(TextRange(1, 8))
        val before = source
        val collapsedHeight = field().fetchSemanticsNode().boundsInRoot.height
        val handle = composeTestRule.onNodeWithContentDescription("Resize message input")
        val extension = with(composeTestRule.density) { 8.dp.toPx() }
        handle.performTouchInput { click(Offset(center.x, height + extension)) }
        assertTrue(field().fetchSemanticsNode().boundsInRoot.height > collapsedHeight + 40f)
        field().assertIsFocused()
        assertEquals(before, source)
        handle.performSemanticsAction(SemanticsActions.Collapse) { it() }
        assertEquals(collapsedHeight, field().fetchSemanticsNode().boundsInRoot.height, 1f)
        assertEquals(before, source)
    }

    @Test fun `narrow short window preserves selection gestures and reachable send`() {
        setupReplyTextField(Modifier.width(220.dp).heightIn(max = 160.dp), initialText = "hello world")
        field().performClick().performTextInputSelection(TextRange(0, 5))
        field().performTextInput("Hi")
        field().assertIsFocused().assertTextEquals("Hi world")
        composeTestRule.onNodeWithTag(TEST_TAG_REPLY_SEND_BUTTON).assertIsDisplayed().assertHeightIsAtLeast(48.dp).assertIsEnabled()
    }
}
