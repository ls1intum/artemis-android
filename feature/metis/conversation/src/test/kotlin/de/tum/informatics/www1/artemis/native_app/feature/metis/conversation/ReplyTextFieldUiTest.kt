package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performClick
import androidx.compose.ui.text.input.TextFieldValue
import androidx.test.ext.junit.runners.AndroidJUnit4
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.ReplyTextField
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.ReplyMode
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.AutoCompleteCategory
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.AutoCompleteHint
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.LocalReplyAutoCompleteHintProvider
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.ReplyAutoCompleteHintProvider
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.TEST_TAG_MARKDOWN_TEXTFIELD
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReplyTextFieldUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `test GIVEN an empty reply textField WHEN entering the @ character THEN a list of autocompletionHints for users shows`() {

        val autoCompleteHints = listOf(
            AutoCompleteCategory(R.string.markdown_textfield_autocomplete_category_users, listOf(
                AutoCompleteHint("User1", "<User1>", "1"),
                AutoCompleteHint("User2", "<User2>", "2"),
                AutoCompleteHint("User3", "<User3>", "3")
            ))
        )

        val mockHintProvider = object : ReplyAutoCompleteHintProvider {
            override val legalTagChars: List<Char> = listOf('@')
            override fun produceAutoCompleteHints(tagChar: Char, query: String): Flow<DataState<List<AutoCompleteCategory>>> {
                return flowOf(DataState.Success(autoCompleteHints))
            }
        }

        composeTestRule.setContent {
            CompositionLocalProvider(LocalReplyAutoCompleteHintProvider provides mockHintProvider) {
                val text = remember { mutableStateOf(TextFieldValue()) }

                ReplyTextField(
                    modifier = Modifier.fillMaxSize(),
                    replyMode = ReplyMode.NewMessage(
                        text,
                        onUpdateTextUpstream = { text.value = it }
                    ) {
                        CompletableDeferred()
                    },
                    updateFailureState = {},
                    title = "TestChat"
                )
            }
        }

        // Click the fake TextField to expand the ReplyTextField and show the real TextField
        composeTestRule.onNodeWithText("TestChat", substring = true).performClick()
        composeTestRule.onNodeWithTag(TEST_TAG_MARKDOWN_TEXTFIELD).performTextInput("@")

        // Verify that auto-complete hints are displayed
        composeTestRule.onNodeWithText("User1").assertExists()
        composeTestRule.onNodeWithText("User2").assertExists()
        composeTestRule.onNodeWithText("User3").assertExists()

        // Simulate clicking on an auto-complete hint
        composeTestRule.onNodeWithText("User1").performClick()

        // Verify that the text field contains the selected user tag
        composeTestRule.onNodeWithTag(TEST_TAG_MARKDOWN_TEXTFIELD).assertTextEquals("<User1>")

        // Verify that the auto-complete hints are no longer displayed
        composeTestRule.onNodeWithText("User1").assertDoesNotExist()
        composeTestRule.onNodeWithText("User2").assertDoesNotExist()
        composeTestRule.onNodeWithText("User3").assertDoesNotExist()
    }
}