package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply

import android.content.ContentResolver
import android.content.ContextWrapper
import android.net.Uri
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.core.app.ActivityOptionsCompat
import de.tum.informatics.www1.artemis.native_app.core.common.test.UnitTest
import de.tum.informatics.www1.artemis.native_app.core.test.BaseComposeTest
import de.tum.informatics.www1.artemis.native_app.core.ui.test.BottomSheetClickWorkaroundTheme
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.emoji_picker.service.impl.EmojiServiceStub
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.StandalonePost
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@Category(UnitTest::class)
@RunWith(RobolectricTestRunner::class)
class ComposerAttachmentTargetUiTest : BaseComposeTest() {

    @Test
    fun `attachment chosen while editing keeps original target after changing composers`() {
        val firstEdit = ReplyMode.EditMessage(
            StandalonePost(id = 1, content = "First edit"),
            onEditMessage = { CompletableDeferred(null) },
            onCancelEditMessage = {}
        )
        val secondEdit = ReplyMode.EditMessage(
            StandalonePost(id = 2, content = "Second edit"),
            onEditMessage = { CompletableDeferred(null) },
            onCancelEditMessage = {}
        )
        val normalText = mutableStateOf(TextFieldValue("Normal draft"))
        val normalMode = ReplyMode.NewMessage(normalText, { normalText.value = it }, { CompletableDeferred(null) })
        val activeMode = mutableStateOf<ReplyMode>(firstEdit)
        val registry = FilePickerRegistry()
        val owner = object : ActivityResultRegistryOwner {
            override val activityResultRegistry = registry
        }
        val resolver = mockk<ContentResolver> {
            every { getType(FILE_URI) } returns "application/pdf"
        }
        val pickerContext = object : ContextWrapper(context) {
            override fun getContentResolver(): ContentResolver = resolver
        }
        var appendAfterUpload: ((String) -> Unit)? = null

        composeTestRule.setContent {
            BottomSheetClickWorkaroundTheme {
                CompositionLocalProvider(
                    LocalActivityResultRegistryOwner provides owner,
                    LocalContext provides pickerContext
                ) {
                    ReplyTextField(
                        modifier = Modifier.fillMaxWidth(),
                        replyMode = activeMode.value,
                        onFileSelected = { uri, append ->
                            assertEquals(FILE_URI, uri)
                            appendAfterUpload = append
                        },
                        updateFailureState = {},
                        conversationName = "Conversation",
                        emojiService = EmojiServiceStub
                    )
                }
            }
        }

        composeTestRule.onNodeWithContentDescription("Add attachment or reference").performClick()
        composeTestRule.onNodeWithText("Upload Attachment").performClick()
        // A tablet can change the active editor while the system picker is open.
        composeTestRule.runOnIdle {
            firstEdit.onUpdate(TextFieldValue("First edit continued", selection = TextRange(20)))
            activeMode.value = secondEdit
        }
        composeTestRule.waitForIdle()
        composeTestRule.runOnIdle { registry.select(FILE_URI) }
        composeTestRule.runOnIdle {
            assertNotNull(appendAfterUpload)
            activeMode.value = normalMode
        }
        composeTestRule.waitForIdle()

        composeTestRule.runOnIdle {
            appendAfterUpload!!(" [report.pdf](https://artemis.example/api/files/report.pdf)")
            assertEquals(
                "First edit continued [report.pdf](https://artemis.example/api/files/report.pdf)",
                firstEdit.currentText.value.text
            )
            assertEquals("Second edit", secondEdit.currentText.value.text)
            assertEquals("Normal draft", normalText.value.text)
        }
        composeTestRule.onNodeWithTag(TEST_TAG_MARKDOWN_TEXTFIELD).assertTextEquals("Normal draft")
    }

    private class FilePickerRegistry : ActivityResultRegistry() {
        private var launchedRequest: Int? = null

        override fun <I, O> onLaunch(
            requestCode: Int,
            contract: ActivityResultContract<I, O>,
            input: I,
            options: ActivityOptionsCompat?
        ) {
            launchedRequest = requestCode
        }

        fun select(uri: Uri) {
            dispatchResult(requireNotNull(launchedRequest), uri)
        }
    }

    private companion object {
        val FILE_URI: Uri = Uri.parse("content://composer-attachment/report.pdf")
    }
}
