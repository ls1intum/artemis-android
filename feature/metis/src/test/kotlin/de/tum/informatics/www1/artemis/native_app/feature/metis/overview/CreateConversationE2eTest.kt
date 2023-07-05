package de.tum.informatics.www1.artemis.native_app.feature.metis.overview

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.lifecycle.SavedStateHandle
import de.tum.informatics.www1.artemis.native_app.core.common.test.EndToEndTest
import de.tum.informatics.www1.artemis.native_app.core.data.filterSuccess
import de.tum.informatics.www1.artemis.native_app.core.model.account.User
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.DefaultTimeoutMillis
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.testServerUrl
import de.tum.informatics.www1.artemis.native_app.feature.metis.ConversationBaseTest
import de.tum.informatics.www1.artemis.native_app.feature.metis.content.Conversation
import de.tum.informatics.www1.artemis.native_app.feature.metis.content.GroupChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.content.OneToOneChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.service.getConversation
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.create_personal_conversation.CreatePersonalConversationScreen
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.create_personal_conversation.CreatePersonalConversationViewModel
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.create_personal_conversation.TEST_TAG_CREATE_PERSONAL_CONVERSATION_BUTTON
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.member_selection.TEST_TAG_MEMBER_SELECTION_SEARCH_FIELD
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.withTimeout
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.koin.test.get
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertIs

@Category(EndToEndTest::class)
@RunWith(RobolectricTestRunner::class)
class CreateConversationE2eTest : ConversationBaseTest() {

    /**
     * Tests that upon entering the name of user2 their name will be displayed in the potential recipients list.
     */
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun `displays users on search`() {
        val testDispatcher = UnconfinedTestDispatcher()

        val viewModel = setupUiAndViewModel(testDispatcher)

        composeTestRule
            .onNodeWithTag(TEST_TAG_MEMBER_SELECTION_SEARCH_FIELD)
            .performTextInput(user2Name)

        testDispatcher.scheduler.advanceTimeBy(1000)

        runBlocking {
            withTimeout(DefaultTimeoutMillis) {
                viewModel.potentialRecipients.filterSuccess().filter { it.isNotEmpty() }.first()
            }
        }

        composeTestRule.waitUntilAtLeastOneExists(
            hasText(user2Name, substring = true) and !hasAnyAncestor(
                hasTestTag(
                    TEST_TAG_MEMBER_SELECTION_SEARCH_FIELD
                )
            ),
            DefaultTimeoutMillis
        )
    }

    @Test
    fun `can create personal conversation`() {
        val createdConversation = createConversationTestImpl(
            listOf(User(username = user2Username))
        )

        assertIs<OneToOneChat>(createdConversation, "Created conversation is not a OneToOneChat")
    }

    @Test
    fun `can create group conversation`() {
        val createdConversation = createConversationTestImpl(
            listOf(
                User(username = user2Username),
                User(username = user3Username)
            )
        )

        assertIs<GroupChat>(createdConversation, "Created conversation is not a group conversation")
    }

    private fun createConversationTestImpl(recipients: List<User>): Conversation {
        var createdConversationId: Long? = null

        val viewModel = setupUiAndViewModel(
            UnconfinedTestDispatcher(),
            onConversationCreated = { conversationId ->
                createdConversationId = conversationId
            }
        )

        recipients.forEach(viewModel::addRecipient)

        composeTestRule
            .onNodeWithTag(TEST_TAG_CREATE_PERSONAL_CONVERSATION_BUTTON)
            .performClick()

        composeTestRule.waitUntil(DefaultTimeoutMillis) { createdConversationId != null }

        return runBlocking {
            conversationService.getConversation(
                course.id!!,
                createdConversationId!!,
                accessToken,
                testServerUrl
            )
                .orThrow("Could not load created conversation")
        }
    }

    private fun setupUiAndViewModel(
        testDispatcher: TestDispatcher,
        onConversationCreated: (Long) -> Unit = {}
    ): CreatePersonalConversationViewModel {
        val viewModel = CreatePersonalConversationViewModel(
            courseId = course.id!!,
            conversationService = get(),
            accountService = get(),
            serverConfigurationService = get(),
            networkStatusProvider = get(),
            savedStateHandle = SavedStateHandle(),
            coroutineContext = testDispatcher
        )

        composeTestRule.setContent {
            CreatePersonalConversationScreen(
                modifier = Modifier.fillMaxSize(),
                viewModel = viewModel,
                onConversationCreated = onConversationCreated,
                onNavigateBack = {}
            )
        }

        return viewModel
    }
}