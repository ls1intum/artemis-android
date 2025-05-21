package de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.settings

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import de.tum.informatics.www1.artemis.native_app.core.common.test.DefaultTestTimeoutMillis
import de.tum.informatics.www1.artemis.native_app.core.common.test.EndToEndTest
import de.tum.informatics.www1.artemis.native_app.core.common.test.testServerUrl
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.DefaultTimeoutMillis
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.R
import org.junit.Ignore
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
@Category(EndToEndTest::class)
@RunWith(RobolectricTestRunner::class)
internal class ChannelSettingsE2eTest : ConversationSettingsBaseE2eTest() {

    @Test(timeout = DefaultTestTimeoutMillis)
    fun `can archive channel`() {
        val channel = runBlockingWithTestTimeout {
            conversationService.createChannel(
                courseId = course.id!!,
                name = "cacchannel",
                description = "",
                isPublic = true,
                isAnnouncement = true,
                isCourseWide = false,
                authToken = accessToken,
                serverUrl = testServerUrl
            )
                .orThrow("Could not create channel")
        }

        setupUiAndViewModel(channel)

        archiveOrUnarchiveChannelImpl(true)
    }

    @Test(timeout = DefaultTestTimeoutMillis)
    fun `can unarchive archived channel`() {
        val channel = runBlockingWithTestTimeout {
            conversationService.createChannel(
                courseId = course.id!!,
                name = "cuacchannel",
                description = "",
                isPublic = true,
                isAnnouncement = true,
                isCourseWide = false,
                authToken = accessToken,
                serverUrl = testServerUrl
            )
                .orThrow("Could not create channel")
                .also { channel ->
                    conversationService.archiveChannel(
                        course.id!!,
                        channel.id,
                        accessToken,
                        testServerUrl
                    )
                        .orThrow("could not archive channel")
                }
        }

        setupUiAndViewModel(channel)

        archiveOrUnarchiveChannelImpl(false)
    }

    private fun archiveOrUnarchiveChannelImpl(archive: Boolean) {
        val doArchiveButtonText =
            context.getString(R.string.conversation_settings_section_other_archive_channel)
        val doUnarchiveButtonText =
            context.getString(R.string.conversation_settings_section_other_unarchive_channel)

        composeTestRule
            .waitUntilAtLeastOneExists(
                hasText(if (archive) doArchiveButtonText else doUnarchiveButtonText),
                DefaultTimeoutMillis
            )

        composeTestRule
            .onNodeWithText(if (archive) doArchiveButtonText else doUnarchiveButtonText)
            .performScrollTo()
            .assertIsDisplayed()
            .performClick()

        composeTestRule
            .onNode(
                hasAnyAncestor(isDialog()) and hasText(
                    context.getString(
                        if (archive) R.string.conversation_settings_dialog_archive_channel_positive
                        else R.string.conversation_settings_dialog_unarchive_channel_positive
                    )
                )
            )
            .assertExists()
            .performClick()

        composeTestRule
            .waitUntilExactlyOneExists(
                hasText(if (archive) doUnarchiveButtonText else doArchiveButtonText),
                DefaultTimeoutMillis
            )
    }

    @Test(timeout = DefaultTestTimeoutMillis)
    fun `can change channel name, description and topic`() {
        val channel = runBlockingWithTestTimeout {
            conversationService.createChannel(
                courseId = course.id!!,
                name = "ccndtchannel",
                description = "some description",
                isPublic = true,
                isAnnouncement = true,
                isCourseWide = false,
                authToken = accessToken,
                serverUrl = testServerUrl
            )
                .orThrow("Could not create channel")
        }

        val newTitle = "ccndtchannel2"
        val newDescription = "another description"
        val newTopic = "new topic"

        changeConversationDetailsTestImpl(
            conversation = channel,
            performChanges = {
                changeTitleText(channel.name, newTitle)

                changeDescriptionText(channel.description, newDescription)

                changeConversationDetailsText(
                    channel.topic
                        ?: context.getString(R.string.conversation_settings_basic_data_topic),
                    context.getString(R.string.conversation_settings_basic_data_topic_empty),
                    newTopic
                )
            },
            verifyChanges = { updatedChannel ->
                assertEquals(newTitle, updatedChannel.name)
                assertEquals(newDescription, updatedChannel.description)
                assertEquals(newTopic, updatedChannel.topic)
            }
        )
    }

    @Test(timeout = DefaultTestTimeoutMillis)
    @Ignore("For some reason, this test started failing with the update to compose 1.8.0. " +
            "We do not know why, and the functionality is still working in the app.")
    fun `can delete channel`() {
        val channel = runBlockingWithTestTimeout {
            conversationService.createChannel(
                courseId = course.id!!,
                name = "deletechannel",
                description = "To be deleted",
                isPublic = true,
                isAnnouncement = false,
                isCourseWide = false,
                authToken = accessToken,
                serverUrl = testServerUrl
            )
                .orThrow("Could not create channel")
        }

        var channelDeleted = false
        setupUiAndViewModel(channel, onChannelDeleted = { channelDeleted = true })

        deleteChannelTestImpl()

        composeTestRule.waitUntil(DefaultTimeoutMillis) { channelDeleted }
    }

    @Test(timeout = DefaultTestTimeoutMillis)
    fun `can toggle channel privacy`() {
        val channel = runBlockingWithTestTimeout {
            conversationService.createChannel(
                courseId = course.id!!,
                name = "toggleprivacychannel",
                description = "privacy test",
                isPublic = true,
                isAnnouncement = false,
                isCourseWide = false,
                authToken = accessToken,
                serverUrl = testServerUrl
            ).orThrow("Could not create channel")
        }

        setupUiAndViewModel(channel)

        toggleChannelPrivacyTestImpl(channel)

    }
}