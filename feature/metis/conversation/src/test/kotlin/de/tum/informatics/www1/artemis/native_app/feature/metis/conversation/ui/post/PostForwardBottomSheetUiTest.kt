package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.post

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.text.input.TextFieldValue
import de.tum.informatics.www1.artemis.native_app.core.common.test.UnitTest
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.test.BaseComposeTest
import de.tum.informatics.www1.artemis.native_app.core.ui.remote_images.LocalArtemisImageProvider
import de.tum.informatics.www1.artemis.native_app.core.ui.remote_images.impl.ArtemisImageProviderStub
import de.tum.informatics.www1.artemis.native_app.core.ui.test.BottomSheetClickWorkaroundTheme
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.shared.ui.ChatListItem
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.post.post_actions.ForwardMessageUseCase
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.post.post_actions.ForwardingMessageError
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.post.post_actions.PostForwardBottomSheet
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.TEST_TAG_MARKDOWN_TEXTFIELD
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.StandalonePost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.member_selection.util.InclusionList
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.member_selection.util.MemberSelectionItem
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@Category(UnitTest::class)
@RunWith(RobolectricTestRunner::class)
class PostForwardBottomSheetUiTest : BaseComposeTest() {

    @Config(qualifiers = "w1280dp-h480dp")
    @Test
    fun `forward send remains visible while editing in short tablet viewport`() {
        val recipient = MemberSelectionItem.Recipient("Ada Lovelace", "ada", "", 4)
        val draft = MutableStateFlow(TextFieldValue())
        val useCase = mockk<ForwardMessageUseCase>(relaxed = true) {
            every { recipients } returns MutableStateFlow(listOf(recipient))
            every { conversations } returns MutableStateFlow(emptyList())
            every { memberItems } returns MutableStateFlow(listOf(recipient))
            every { query } returns MutableStateFlow("")
            every { isQueryTooShort } returns MutableStateFlow(true)
            every { potentialRecipients } returns MutableStateFlow(DataState.Success(emptyList()))
            every { suggestions(any()) } returns MutableStateFlow(emptyList())
            every { inclusionList } returns MutableStateFlow(InclusionList())
            every { forwardingMessageError } returns mutableStateOf(ForwardingMessageError.NO_ERROR)
            every { forwardingServerHost } returns MutableStateFlow("artemis.example")
            every { editorSessionKey } returns MutableStateFlow<Any?>("forward-session")
            every { newMessageText } returns draft
            every { updateInitialReplyText(any()) } answers { draft.value = firstArg() }
        }
        val post = StandalonePost(id = 10, content = (1..30).joinToString("\n") { "Forwarded line $it" })
        composeTestRule.setContent {
            BottomSheetClickWorkaroundTheme {
                CompositionLocalProvider(LocalArtemisImageProvider provides ArtemisImageProviderStub()) {
                    PostForwardBottomSheet(
                        chatListItem = ChatListItem.PostItem.IndexedItem.Post(post, emptyList()),
                        forwardMessageUseCase = useCase,
                        onDismissRequest = {}
                    )
                }
            }
        }

        composeTestRule.onNodeWithTag(TEST_TAG_MARKDOWN_TEXTFIELD)
            .performClick()
            .performTextInput((1..20).joinToString("\n") { "Comment line $it" })

        composeTestRule.onNodeWithText(context.getString(R.string.post_forward_send))
            .assertIsDisplayed()
            .assertIsEnabled()
    }
}
