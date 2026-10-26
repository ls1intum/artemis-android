package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply

import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.InterceptPlatformTextInput
import androidx.compose.ui.platform.PlatformTextInputInterceptor
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.common.test.UnitTest
import de.tum.informatics.www1.artemis.native_app.core.test.BaseComposeTest
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
@OptIn(ExperimentalComposeUiApi::class, ExperimentalTestApi::class)
class ComposerBlockCompositionUiTest : BaseComposeTest() {
    @Test fun `quote bar remains visible while the keyboard composes its body`() =
        assertBlockSurvivesComposition("> hello", quote = true)

    @Test fun `code background remains visible while the keyboard composes its body`() =
        assertBlockSurvivesComposition("```\nhello\n```", quote = false)

    private fun assertBlockSurvivesComposition(initial: String, quote: Boolean) {
        val source = mutableStateOf(TextFieldValue(initial, TextRange(initial.indexOf("hello") + 5)))
        var connection: InputConnection? = null
        var codeBackground = Color.Unspecified
        val interceptor = PlatformTextInputInterceptor { request, next ->
            connection = request.createInputConnection(EditorInfo())
            next.startInputMethod(request)
        }
        composeTestRule.setContent {
            codeBackground = MaterialTheme.colorScheme.surfaceContainerHighest
            val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {}
            InterceptPlatformTextInput(interceptor) {
                MarkdownTextField(
                    modifier = Modifier.fillMaxWidth(), textFieldValue = source.value,
                    hintText = AnnotatedString("Message"), filePickerLauncher = picker,
                    onTextChanged = { source.value = it },
                )
            }
        }
        val field = composeTestRule.onNodeWithTag(TEST_TAG_MARKDOWN_TEXTFIELD)
        field.performClick().performTextInputSelection(TextRange(5))
        val layouts = mutableListOf<TextLayoutResult>()
        field.performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(layouts) }
        val y = ((layouts.single().getLineTop(0) + layouts.single().getLineBottom(0)) / 2).toInt()
        val baseline = field.captureToImage().toPixelMap()
        val x = if (quote) with(composeTestRule.density) { 3.dp.toPx().toInt() } else baseline.width - 4
        val expected = baseline[x, y].toArgb()
        if (quote) {
            val besideBar = with(composeTestRule.density) { 8.dp.toPx().toInt() }
            assertNotEquals("The baseline must contain a visible quote bar", baseline[besideBar, y].toArgb(), expected)
        } else {
            assertEquals("The baseline must contain the code background", codeBackground.toArgb(), expected)
        }

        composeTestRule.runOnIdle { assertTrue(connection!!.setComposingRegion(0, 5)) }
        composeTestRule.waitForIdle()
        assertEquals(TextRange(0, 5), MarkdownEditEngine.visibleValue(source.value).composition)
        layouts.clear()
        field.performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(layouts) }
        assertTrue("Exercise the IME's rendered composition underline", layouts.single().layoutInput.text.spanStyles.any {
            it.item.textDecoration?.contains(TextDecoration.Underline) == true
        })
        val composing = field.captureToImage().toPixelMap()
        assertEquals("Block decoration must remain painted during composition", expected, composing[x, y].toArgb())
        field.assertIsFocused().assertTextEquals("hello")
        assertEquals(initial, source.value.text)

        composeTestRule.runOnIdle { assertTrue(connection!!.finishComposingText()) }
        composeTestRule.waitForIdle()
        assertNull(source.value.composition)
        assertEquals(expected, field.captureToImage().toPixelMap()[x, y].toArgb())
        assertEquals(initial, source.value.text)
    }
}
