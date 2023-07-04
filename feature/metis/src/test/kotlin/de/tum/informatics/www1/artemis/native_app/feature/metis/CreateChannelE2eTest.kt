package de.tum.informatics.www1.artemis.native_app.feature.metis

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.lifecycle.SavedStateHandle
import de.tum.informatics.www1.artemis.native_app.core.common.test.EndToEndTest
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.DefaultTimeoutMillis
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.testServerUrl
import de.tum.informatics.www1.artemis.native_app.feature.metis.content.ChannelChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.service.getConversation
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.create_channel.CreateChannelScreen
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.create_channel.CreateChannelViewModel
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.create_channel.TEST_TAG_CREATE_CHANNEL_BUTTON
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.koin.test.get
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertEquals
import kotlin.test.assertIs

@Category(EndToEndTest::class)
@RunWith(RobolectricTestRunner::class)
class CreateChannelE2eTest : ConversationBaseTest() {

    @Test
    fun `can create channel`() {
        val channelName = "testchannel"
        val channelDescription = "testchannel description"

        val viewModel = CreateChannelViewModel(
            course.id!!,
            get(),
            get(),
            get(),
            SavedStateHandle(),
            UnconfinedTestDispatcher()
        )

        var createdConversationId: Long? = null

        composeTestRule.setContent {
            CreateChannelScreen(
                modifier = Modifier.fillMaxSize(),
                viewModel = viewModel,
                onConversationCreated = { createdConversationId = it },
                onNavigateBack = {}
            )
        }

        composeTestRule
            .onNodeWithText(context.getString(R.string.create_channel_text_field_name_label))
            .performTextInput(channelName)

        composeTestRule
            .onNodeWithText(context.getString(R.string.create_channel_text_field_description_label))
            .performTextInput(channelDescription)

        composeTestRule
            .onNodeWithTag(TEST_TAG_CREATE_CHANNEL_BUTTON)
            .performClick()

        composeTestRule.waitUntil(DefaultTimeoutMillis) { createdConversationId != null }

        val createdConversation = runBlocking {
            conversationService
                .getConversation(
                    courseId = course.id!!,
                    conversationId = createdConversationId!!,
                    authToken = accessToken,
                    serverUrl = testServerUrl
                )
                .orThrow("Could not load created channel")
        }

        val channel = assertIs<ChannelChat>(createdConversation, "Created conversation is not a channel")
        assertEquals(channelName, channel.name)
        assertEquals(channelDescription, channel.description)
        assertEquals(true, channel.isPublic, "Channel was set to be public but is not public")
        assertEquals(true, channel.isAnnouncementChannel, "Channel was set to be an announcement channel but is not an announcement channel")
    }
}