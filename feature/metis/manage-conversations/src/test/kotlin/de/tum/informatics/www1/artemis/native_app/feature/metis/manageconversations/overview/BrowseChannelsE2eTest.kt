package de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.overview

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import de.tum.informatics.www1.artemis.native_app.core.common.test.DefaultTestTimeoutMillis
import de.tum.informatics.www1.artemis.native_app.core.common.test.EndToEndTest
import de.tum.informatics.www1.artemis.native_app.core.common.test.testServerUrl
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.DefaultTimeoutMillis
import de.tum.informatics.www1.artemis.native_app.feature.login.test.getAdminAccessToken
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.browse_channels.BrowseChannelsScreen
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.browse_channels.BrowseChannelsViewModel
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.browse_channels.testTagForBrowsedChannelItem
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.ChannelChat
import de.tum.informatics.www1.artemis.native_app.feature.metistest.ConversationBaseTest
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.koin.test.get
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
@Category(EndToEndTest::class)
@RunWith(RobolectricTestRunner::class)
class BrowseChannelsE2eTest : ConversationBaseTest() {

    private lateinit var channels: List<ChannelChat>

    override fun setup() {
        super.setup()

        runBlockingWithTestTimeout {
            channels = (0 until 2).map { index ->
                conversationService.createChannel(
                    courseId = course.id!!,
                    name = "channel$index",
                    description = "",
                    isPublic = true,
                    isAnnouncement = false,
                    isCourseWide = false,
                    authToken = getAdminAccessToken(),
                    serverUrl = testServerUrl
                ).orThrow("Could not create channel $index")
            }
        }
    }

    @Test(timeout = DefaultTestTimeoutMillis)
    fun `displays existing channels`() {
        setupUi()

        channels.forEach { channel ->
            println("Checking $channel exists.")
            composeTestRule.waitUntilExactlyOneExists(hasText(channel.name), DefaultTimeoutMillis)
        }
    }

    @Test(timeout = DefaultTestTimeoutMillis)
    fun `can join channel`() {
        var navigatedToChannelId: Long? = null

        setupUi {
            navigatedToChannelId = it
        }

        val channelToJoin = channels.first()

        val testTagForChannel = testTagForBrowsedChannelItem(channelToJoin.id)
        composeTestRule.waitUntilExactlyOneExists(
            hasTestTag(testTagForChannel), DefaultTimeoutMillis
        )

        composeTestRule
            .onNodeWithTag(testTagForChannel)
            .performScrollTo()
            .performClick()

        composeTestRule.waitUntil(DefaultTimeoutMillis) { navigatedToChannelId != null }
        assertEquals(channelToJoin.id, navigatedToChannelId, "Joined channel id is not the channel we navigated to")

        val conversations = runBlockingWithTestTimeout {
            conversationService.getConversations(course.id!!, accessToken, testServerUrl).orThrow("Could not load conversations")
        }

        assertTrue(conversations.any { it.id == channelToJoin.id }, "Conversations $conversations does not contain the channel we just joined.")
    }

    private fun setupUi(onNavigateToConversation: (Long) -> Unit = {}): BrowseChannelsViewModel {
        val viewModel = BrowseChannelsViewModel(
            courseId = course.id!!,
            accountService = get(),
            serverConfigurationService = get(),
            channelService = get(),
            networkStatusProvider = get(),
            accountDataService = get(),
            courseService = get(),
            coroutineContext = testDispatcher
        )

        composeTestRule.setContent {
            BrowseChannelsScreen(
                modifier = Modifier.fillMaxSize(),
                viewModel = viewModel,
                onNavigateToConversation = onNavigateToConversation,
                onNavigateBack = {}
            )
        }

        return viewModel
    }
}
