package de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.overview

import android.app.Application
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToKey
import de.tum.informatics.www1.artemis.native_app.core.common.test.DefaultTestTimeoutMillis
import de.tum.informatics.www1.artemis.native_app.core.common.test.EndToEndTest
import de.tum.informatics.www1.artemis.native_app.core.common.test.testServerUrl
import de.tum.informatics.www1.artemis.native_app.core.data.filterSuccess
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.DefaultTimeoutMillis
import de.tum.informatics.www1.artemis.native_app.feature.login.test.user1Username
import de.tum.informatics.www1.artemis.native_app.feature.login.test.user2Username
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ConversationCollections
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.overview.ConversationOverviewBody
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.overview.ConversationOverviewViewModel
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.overview.KEY_SUFFIX_CHANNELS
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.overview.KEY_SUFFIX_FAVORITES
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.overview.KEY_SUFFIX_GROUPS
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.overview.KEY_SUFFIX_HIDDEN
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.overview.KEY_SUFFIX_PERSONAL
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.overview.SECTION_HIDDEN_KEY
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.overview.TEST_TAG_CONVERSATION_LIST
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.overview.TEST_TAG_HEADER_EXPAND_ICON
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.overview.tagForConversation
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.overview.tagForConversationOptions
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.ChannelChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.Conversation
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.GroupChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.OneToOneChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.humanReadableName
import de.tum.informatics.www1.artemis.native_app.feature.metistest.ConversationBaseTest
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Ignore
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.koin.test.get
import org.robolectric.RobolectricTestRunner
import kotlin.random.Random

@OptIn(ExperimentalTestApi::class)
@Category(EndToEndTest::class)
@RunWith(RobolectricTestRunner::class)
class ConversationOverviewE2eTest : ConversationBaseTest() {

    /**
     * Checks that conversations are correctly displayed.
     */
    @Test(timeout = DefaultTestTimeoutMillis)
    fun `displays personal chat, group chat and channel`() {
        val channelName = "channel${Random.nextInt(10000)}"

        val (oneToOneChat, groupChat, channel) = runBlocking {
            val personal = conversationService.createOneToOneConversation(
                courseId = course.id!!,
                partner = user2Username,
                authToken = accessToken,
                serverUrl = testServerUrl
            ).orThrow("Could not create one to one conversation")

            val groupChat = conversationService.createGroupChat(
                courseId = course.id!!,
                groupMembers = listOf(user1Username, user2Username),
                authToken = accessToken,
                serverUrl = testServerUrl
            )
                .orThrow("Could not create group chat")

            val channel = conversationService.createChannel(
                courseId = course.id!!,
                name = channelName,
                description = "some description",
                isPublic = true,
                isAnnouncement = false,
                authToken = accessToken,
                serverUrl = testServerUrl
            )
                .orThrow("Could not create channel")

            Triple(personal, groupChat, channel)
        }

        setupUiAndViewModel()

        val verifyConversation = { conversation: Conversation ->
            scrollToConversation(conversation)

            composeTestRule
                .onNodeWithTag(getTagForConversation(conversation))
                .assert(hasAnyDescendant(hasText(conversation.humanReadableName)))
        }

        verifyConversation(oneToOneChat)
        verifyConversation(groupChat)
        verifyConversation(channel)
    }

    @Test(timeout = DefaultTestTimeoutMillis)
    fun `can mark conversation as favorite`() {
        val chat = runBlocking { createPersonalConversation() }

        markConversationImpl(
            originalTag = getTagForConversation(chat),
            newTag = tagForConversation(chat.id, KEY_SUFFIX_FAVORITES),
            textToClick = context.getString(R.string.conversation_overview_conversation_item_mark_as_favorite),
            checkExists = { favorites.conversations.any { conv -> conv.id == chat.id } }
        )
    }

    @Test(timeout = DefaultTestTimeoutMillis)
    fun `can mark favorite conversation as not favorite`() {
        val chat = runBlocking {
            val chat = createPersonalConversation()

            conversationService.markConversationAsFavorite(
                courseId = course.id!!,
                conversationId = chat.id,
                favorite = true,
                authToken = accessToken,
                serverUrl = testServerUrl
            ).orThrow("Could not mark conversation as favorite")

            chat
        }

        markConversationImpl(
            originalTag = tagForConversation(chat.id, KEY_SUFFIX_FAVORITES),
            newTag = getTagForConversation(chat),
            textToClick = context.getString(R.string.conversation_overview_conversation_item_unmark_as_favorite),
            checkExists = { favorites.conversations.none { conv -> conv.id == chat.id } }
        )
    }

    @Test(timeout = DefaultTestTimeoutMillis)
    fun `can mark conversation as hidden`() {
        val chat = runBlocking { createPersonalConversation() }

        markConversationImpl(
            originalTag = getTagForConversation(chat),
            newTag = tagForConversation(chat.id, KEY_SUFFIX_HIDDEN),
            textToClick = context.getString(R.string.conversation_overview_conversation_item_mark_as_hidden),
            checkExists = { hidden.conversations.any { conv -> conv.id == chat.id } },
            doAfterAvailable = { viewModel ->
                expandHiddenSection(viewModel)
            }
        )
    }

    @Test(timeout = DefaultTestTimeoutMillis)
    fun `can mark hidden conversation as not hidden`() {
        val chat = runBlocking {
            val chat = createPersonalConversation()

            conversationService.markConversationAsHidden(
                courseId = course.id!!,
                conversationId = chat.id,
                hidden = true,
                authToken = accessToken,
                serverUrl = testServerUrl
            ).orThrow("Could not mark conversation as hidden")

            chat
        }

        markConversationImpl(
            originalTag = tagForConversation(chat.id, KEY_SUFFIX_HIDDEN),
            newTag = getTagForConversation(chat),
            textToClick = context.getString(R.string.conversation_overview_conversation_item_unmark_as_hidden),
            checkExists = { hidden.conversations.none { conv -> conv.id == chat.id } },
            doInitially = { viewModel ->
                expandHiddenSection(viewModel)
            }
        )
    }

    @Test(timeout = DefaultTestTimeoutMillis)
    fun `can mark conversation as muted`() {
        val chat = runBlocking { createPersonalConversation() }

        markConversationImpl(
            originalTag = getTagForConversation(chat),
            newTag = tagForConversation(chat.id, KEY_SUFFIX_PERSONAL),
            textToClick = context.getString(R.string.conversation_overview_conversation_item_mark_as_muted),
            checkExists = { conversations.any { conv -> conv.id == chat.id && conv.isMuted } }
        )
    }

    @Test(timeout = DefaultTestTimeoutMillis)
    fun `can mark hidden conversation as not muted`() {
        val chat = runBlocking {
            val chat = createPersonalConversation()

            conversationService.markConversationMuted(
                courseId = course.id!!,
                conversationId = chat.id,
                muted = true,
                authToken = accessToken,
                serverUrl = testServerUrl
            ).orThrow("Could not mark conversation as hidden")

            chat
        }

        markConversationImpl(
            originalTag = tagForConversation(chat.id, KEY_SUFFIX_PERSONAL),
            newTag = getTagForConversation(chat),
            textToClick = context.getString(R.string.conversation_overview_conversation_item_unmark_as_muted),
            checkExists = { conversations.none { conv -> conv.id == chat.id && conv.isMuted } }
        )
    }

    /**
     * Checks that updates to conversations are automatically received over the websocket connection.
     */
    @OptIn(ExperimentalTestApi::class)
    @Ignore("Websockets are currently very flaky -> disabled")
    @Test(timeout = DefaultTestTimeoutMillis)
    fun `receives websocket conversation updates`() {
        val chat = runBlocking {
            conversationService.createChannel(
                courseId = course.id!!,
                name = "channel${Random.nextInt(10000)}",
                description = "some description",
                isPublic = true,
                isAnnouncement = false,
                authToken = accessToken,
                serverUrl = testServerUrl
            )
                .orThrow("Could not create channel")
        }

        val viewModel = setupUiAndViewModel()

        waitUntilConversationsAreLoaded(viewModel)

        scrollToConversation(chat)

        composeTestRule
            .onNodeWithTag(getTagForConversation(chat))
            .assert(hasAnyDescendant(hasText(chat.name)))

        val newChannelName = "newname"

        // Rename the channel, the server should send a websocket message
        runBlocking {
            conversationService.updateConversation(
                courseId = course.id!!,
                conversationId = chat.id,
                newName = newChannelName,
                newDescription = null,
                newTopic = null,
                conversation = chat,
                authToken = accessToken,
                serverUrl = testServerUrl
            ).orThrow("Could not send name update")
        }

        composeTestRule.waitUntilExactlyOneExists(
            hasTestTag(getTagForConversation(chat)) and hasAnyDescendant(
                hasText(newChannelName)
            ),
            DefaultTimeoutMillis
        )
    }

    private fun expandHiddenSection(viewModel: ConversationOverviewViewModel) {
        composeTestRule
            .onNodeWithTag(TEST_TAG_CONVERSATION_LIST)
            .performScrollToKey(SECTION_HIDDEN_KEY)

        composeTestRule
            .onNode(
                hasAnyAncestor(hasTestTag(SECTION_HIDDEN_KEY)) and hasTestTag(
                    TEST_TAG_HEADER_EXPAND_ICON
                )
            )
            .performClick()

        runBlockingWithTestTimeout {
            viewModel
                .conversations
                .filter { it.bind { conv -> conv.hidden.isExpanded }.orElse(false) }
                .first()
        }
    }

    private fun markConversationImpl(
        originalTag: String,
        newTag: String,
        textToClick: String,
        checkExists: ConversationCollections.() -> Boolean,
        doInitially: (ConversationOverviewViewModel) -> Unit = {},
        doAfterAvailable: (ConversationOverviewViewModel) -> Unit = {}
    ) {
        val viewModel = setupUiAndViewModel()

        waitUntilConversationsAreLoaded(viewModel)

        doInitially(viewModel)

        composeTestRule
            .onNodeWithTag(TEST_TAG_CONVERSATION_LIST)
            .performScrollToKey(originalTag)

        val optionsTag = tagForConversationOptions(originalTag)
        composeTestRule
            .onNodeWithTag(optionsTag)
            .performClick()

        composeTestRule
            .onNodeWithText(textToClick)
            .performClick()

        runBlockingWithTestTimeout {
            viewModel
                .conversations
                .filter { state ->
                    state
                        .bind(checkExists)
                        .orElse(false)
                }
                .first()
        }

        doAfterAvailable(viewModel)

        composeTestRule
            .onNodeWithTag(TEST_TAG_CONVERSATION_LIST)
            .performScrollToKey(newTag)

        composeTestRule
            .onNodeWithTag(newTag)
            .assertExists()
    }

    private fun waitUntilConversationsAreLoaded(viewModel: ConversationOverviewViewModel) {
        runBlockingWithTestTimeout {
            viewModel.conversations.filterSuccess().first()
        }
    }

    private fun scrollToConversation(conversation: Conversation) {
        composeTestRule
            .onNodeWithTag(TEST_TAG_CONVERSATION_LIST)
            .performScrollToKey(getTagForConversation(conversation))
    }

    private fun getTagForConversation(conversation: Conversation): String {
        val suffix = when (conversation) {
            is ChannelChat -> KEY_SUFFIX_CHANNELS
            is GroupChat -> KEY_SUFFIX_GROUPS
            is OneToOneChat -> KEY_SUFFIX_PERSONAL
        }

        return tagForConversation(conversation.id, suffix)
    }

    private fun setupUiAndViewModel(): ConversationOverviewViewModel {
        val viewModel = ConversationOverviewViewModel(
            application = context.applicationContext as Application,
            courseId = course.id!!,
            conversationService = get(),
            serverConfigurationService = get(),
            accountService = get(),
            conversationPreferenceService = get(),
            websocketProvider = get(),
            networkStatusProvider = get(),
            accountDataService = get(),
            coroutineContext = testDispatcher,
            courseService = get()
        )

        composeTestRule.setContent {
            ConversationOverviewBody(
                modifier = Modifier.fillMaxSize(),
                viewModel = viewModel,
                onNavigateToConversation = {},
                onRequestCreatePersonalConversation = { },
                onRequestAddChannel = {},
                onRequestBrowseChannel = {},
                canCreateChannel = false
            )
        }

        composeTestRule.waitUntilAtLeastOneExists(
            hasText(context.getString(R.string.conversation_overview_section_general_channels)),
            DefaultTimeoutMillis
        )

        return viewModel
    }
}