package de.tum.informatics.www1.artemis.native_app.feature.metis

import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasAnySibling
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
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
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.create_channel.TEST_TAG_SET_ANNOUNCEMENT_BUTTON
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.create_channel.TEST_TAG_SET_PRIVATE_BUTTON
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.create_channel.TEST_TAG_SET_PUBLIC_BUTTON
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.create_channel.TEST_TAG_SET_UNRESTRICTED_BUTTON
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

    companion object {
        private const val TAG = "CreateChannelE2eTest"
    }

    @Test
    fun `can create channel`() {
        canCreateChannelTestImpl(
            channelName = "testchannel1",
            channelDescription = "testchannel description",
            isPublic = true,
            isAnnouncement = true
        )
    }

    @Test
    fun `fun create channel without description`() {
        canCreateChannelTestImpl(
            channelName = "testchannel2",
            channelDescription = "",
            isPublic = true,
            isAnnouncement = true
        )
    }

    @Test
    fun `fun create private and unrestricted channel`() {
        canCreateChannelTestImpl(
            channelName = "testchannel3",
            channelDescription = "description",
            isPublic = false,
            isAnnouncement = false
        )
    }

    private fun canCreateChannelTestImpl(
        channelName: String,
        channelDescription: String,
        isPublic: Boolean,
        isAnnouncement: Boolean
    ) {
        Log.i(
            TAG,
            "Testing create channel with properties: channelname=$channelName, channelDescription$channelDescription, isPublic=$isPublic, isAnnouncement=$isAnnouncement"
        )

        val viewModel = CreateChannelViewModel(
            courseId = course.id!!,
            conversationService = get(),
            accountService = get(),
            serverConfigurationService = get(),
            savedStateHandle = SavedStateHandle(),
            coroutineContext = UnconfinedTestDispatcher()
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
            .performScrollTo()
            .performTextInput(channelName)

        composeTestRule
            .onNodeWithText(context.getString(R.string.create_channel_text_field_description_label))
            .performScrollTo()
            .performTextInput(channelDescription)

        composeTestRule
            .onNodeWithTag(if (isPublic) TEST_TAG_SET_PUBLIC_BUTTON else TEST_TAG_SET_PRIVATE_BUTTON)
            .performScrollTo()
            .performClick()

        composeTestRule
            .onNodeWithTag(if (isPublic) TEST_TAG_SET_PUBLIC_BUTTON else TEST_TAG_SET_PRIVATE_BUTTON)
            .assertIsSelected()

        composeTestRule
            .onNodeWithTag(if (isAnnouncement) TEST_TAG_SET_ANNOUNCEMENT_BUTTON else TEST_TAG_SET_UNRESTRICTED_BUTTON)
            .performScrollTo()
            .performClick()

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

        val channel =
            assertIs<ChannelChat>(createdConversation, "Created conversation is not a channel")
        assertEquals(channelName, channel.name, "Name not set correctly")
        assertEquals(channelDescription.ifBlank { null }, channel.description, "Description not set correctly")
        assertEquals(isPublic, channel.isPublic, "Public property not set correctly")
        assertEquals(
            isAnnouncement,
            channel.isAnnouncementChannel,
            "Announcement property not set correctly"
        )
    }
}