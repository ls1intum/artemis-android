package de.tum.informatics.www1.artemis.native_app.feature.metis.settings

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import de.tum.informatics.www1.artemis.native_app.core.common.test.EndToEndTest
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.DefaultTimeoutMillis
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.testServerUrl
import de.tum.informatics.www1.artemis.native_app.feature.login.test.getAdminAccessToken
import de.tum.informatics.www1.artemis.native_app.feature.login.test.user1Username
import de.tum.informatics.www1.artemis.native_app.feature.metis.R
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
@Category(EndToEndTest::class)
@RunWith(RobolectricTestRunner::class)
class ChannelSettingsE2eTest : ConversationSettingsBaseE2eTest() {


    /**
     * Tests if we can leave a channel not created by ourselfes
     */
    @Test
    fun `can leave channel`() {
        val channel = runBlocking {
            withTimeout(DefaultTimeoutMillis) {
                conversationService.createChannel(
                    courseId = course.id!!,
                    name = "clcchannel",
                    description = "",
                    isPublic = true,
                    isAnnouncement = true,
                    authToken = getAdminAccessToken(),
                    serverUrl = testServerUrl
                )
                    .orThrow("Could not create channel")
                    .apply {
                        conversationService.registerMembers(
                            courseId = course.id!!,
                            conversation = this,
                            users = listOf(user1Username),
                            authToken = accessToken,
                            serverUrl = testServerUrl
                        )
                    }
            }
        }

        canLeaveConversationTestImpl(channel)
    }

    @Test
    fun `can archive channel`() {
        val channel = runBlocking {
            withTimeout(DefaultTimeoutMillis) {
                conversationService.createChannel(
                    courseId = course.id!!,
                    name = "cacchannel",
                    description = "",
                    isPublic = true,
                    isAnnouncement = true,
                    authToken = accessToken,
                    serverUrl = testServerUrl
                )
                    .orThrow("Could not create channel")
            }
        }

        setupUiAndViewModel(channel)

        archiveOrUnarchiveChannelImpl(true)
    }

    @Test
    fun `can unarchive archived channel`() {
        val channel = runBlocking {
            withTimeout(DefaultTimeoutMillis) {
                conversationService.createChannel(
                    courseId = course.id!!,
                    name = "cuacchannel",
                    description = "",
                    isPublic = true,
                    isAnnouncement = true,
                    authToken = accessToken,
                    serverUrl = testServerUrl
                )
                    .orThrow("Could not create channel")
                    .apply {
                        conversationService.archiveChannel(
                            course.id!!,
                            id,
                            accessToken,
                            testServerUrl
                        )
                            .orThrow("could not archive channel")
                    }
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
            .performClick()

        composeTestRule
            .waitUntilExactlyOneExists(hasText(if (archive) doUnarchiveButtonText else doArchiveButtonText))
    }

    @Test
    fun `can change channel name, description and topic`() {
        val channel = runBlocking {
            withTimeout(DefaultTimeoutMillis) {
                conversationService.createChannel(
                    courseId = course.id!!,
                    name = "ccndtchannel",
                    description = "some description",
                    isPublic = true,
                    isAnnouncement = true,
                    authToken = accessToken,
                    serverUrl = testServerUrl
                )
                    .orThrow("Could not create channel")
            }
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
}
