package de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.overview

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.lifecycle.SavedStateHandle
import de.tum.informatics.www1.artemis.native_app.core.common.test.DefaultTestTimeoutMillis
import de.tum.informatics.www1.artemis.native_app.core.common.test.EndToEndTest
import de.tum.informatics.www1.artemis.native_app.core.common.test.testServerUrl
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.DefaultTimeoutMillis
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.create_channel.CreateChannelScreen
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.create_channel.CreateChannelViewModel
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.create_channel.TEST_TAG_CREATE_CHANNEL_BUTTON
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.create_channel.TEST_TAG_SET_ANNOUNCEMENT_UNRESTRICTED_SWITCH
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.create_channel.TEST_TAG_SET_COURSE_WIDE_SELECTIVE_SWITCH
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.create_channel.TEST_TAG_SET_PRIVATE_PUBLIC_SWITCH
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.ChannelChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.service.network.getConversation
import de.tum.informatics.www1.artemis.native_app.feature.metistest.ConversationBaseTest
import kotlinx.coroutines.runBlocking
import org.junit.Ignore
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.koin.test.get
import org.robolectric.RobolectricTestRunner
import org.robolectric.util.Logger
import kotlin.test.assertEquals
import kotlin.test.assertIs

@Category(EndToEndTest::class)
@RunWith(RobolectricTestRunner::class)
class CreateChannelE2eTest : ConversationBaseTest() {

    private val privateIndex = 1
    private val courseWideIndex = 0

    @Ignore("This test does not work anymore, see https://github.com/ls1intum/artemis-android/issues/495")
    @Test(timeout = DefaultTestTimeoutMillis)
    fun `can create channel`() {
        canCreateChannelTestImpl(
            channelName = "testchannel1",
            channelDescription = "testchannel description",
            isPrivate = false,
            isAnnouncement = true,
            isCourseWide = false
        )
    }

    @Ignore("This test does not work anymore, see https://github.com/ls1intum/artemis-android/issues/495")
    @Test(timeout = DefaultTestTimeoutMillis)
    fun `fun create channel without description`() {
        canCreateChannelTestImpl(
            channelName = "testchannel2",
            channelDescription = "",
            isPrivate = false,
            isAnnouncement = true,
            isCourseWide = false
        )
    }

    @Ignore("This test does not work anymore, see https://github.com/ls1intum/artemis-android/issues/495")
    @Test(timeout = DefaultTestTimeoutMillis)
    fun `fun create private and unrestricted channel`() {
        canCreateChannelTestImpl(
            channelName = "testchannel3",
            channelDescription = "description",
            isPrivate = true,
            isAnnouncement = false,
            isCourseWide = false
        )
    }

    @Ignore("This test does not work anymore, see https://github.com/ls1intum/artemis-android/issues/495")
    @Test(timeout = DefaultTestTimeoutMillis)
    fun `can create course-wide channel`() {
        canCreateChannelTestImpl(
            channelName = "testchannel4",
            channelDescription = "Course-wide channel test",
            isPrivate = false,
            isAnnouncement = false,
            isCourseWide = true
        )
    }

    private fun canCreateChannelTestImpl(
        channelName: String,
        channelDescription: String,
        isPrivate: Boolean,
        isAnnouncement: Boolean,
        isCourseWide: Boolean
    ) {
        Logger.info(
            "Testing create channel with properties: channelname=$channelName, channelDescription$channelDescription, isPublic=$isPrivate, isAnnouncement=$isAnnouncement, isCourseWide=$isCourseWide"
        )

        val viewModel = CreateChannelViewModel(
            courseId = course.id!!,
            conversationService = get(),
            accountService = get(),
            serverConfigurationService = get(),
            savedStateHandle = SavedStateHandle(),
            coroutineContext = testDispatcher
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

        if (isPrivate) {
            composeTestRule
                .onNodeWithTag(TEST_TAG_SET_PRIVATE_PUBLIC_SWITCH)
                .performScrollTo()
            composeTestRule
                .onNodeWithTag(TEST_TAG_SET_PRIVATE_PUBLIC_SWITCH + privateIndex)
                .assertIsSelected()
        } else {
            composeTestRule
                .onNodeWithTag(TEST_TAG_SET_PRIVATE_PUBLIC_SWITCH + privateIndex)
                .assertIsNotSelected()
        }

        if (isAnnouncement) {
            composeTestRule
                .onNodeWithTag(TEST_TAG_SET_ANNOUNCEMENT_UNRESTRICTED_SWITCH)
                .performScrollTo()
                .performClick()
            composeTestRule
                .onNodeWithTag(TEST_TAG_SET_ANNOUNCEMENT_UNRESTRICTED_SWITCH)
                .assertIsOn()
        } else {
            composeTestRule
                .onNodeWithTag(TEST_TAG_SET_ANNOUNCEMENT_UNRESTRICTED_SWITCH)
                .assertIsOff()
        }

        if (isCourseWide) {
            composeTestRule
                .onNodeWithTag(TEST_TAG_SET_COURSE_WIDE_SELECTIVE_SWITCH)
                .performScrollTo()
            composeTestRule
                .onNodeWithTag(TEST_TAG_SET_COURSE_WIDE_SELECTIVE_SWITCH + courseWideIndex)
                .assertIsSelected()
        } else {
            composeTestRule
                .onNodeWithTag(TEST_TAG_SET_COURSE_WIDE_SELECTIVE_SWITCH + courseWideIndex)
                .assertIsNotSelected()
        }

        composeTestRule
            .onNodeWithTag(TEST_TAG_CREATE_CHANNEL_BUTTON)
            .performScrollTo()
            .assertIsEnabled()
            .performClick()

        composeTestRule.onNodeWithText(context.getString(R.string.create_channel_failed_title)).assertDoesNotExist()
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
        assertEquals(!isPrivate, channel.isPublic, "Public property not set correctly")
        assertEquals(
            isAnnouncement,
            channel.isAnnouncementChannel,
            "Announcement property not set correctly"
        )
        assertEquals(
            isCourseWide,
            channel.isCourseWide,
            "Course Wide property not set correctly"
        )
    }
}