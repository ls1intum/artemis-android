package de.tum.informatics.www1.artemis.native_app.feature.metis

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasParent
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.printToString
import androidx.lifecycle.SavedStateHandle
import de.tum.informatics.www1.artemis.native_app.core.common.test.EndToEndTest
import de.tum.informatics.www1.artemis.native_app.core.data.filterSuccess
import de.tum.informatics.www1.artemis.native_app.core.model.account.User
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.create_personal_conversation.CreatePersonalConversationScreen
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.create_personal_conversation.CreatePersonalConversationViewModel
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.create_personal_conversation.TEST_TAG_CREATE_PERSONAL_CONVERSATION_BUTTON
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.member_selection.TEST_TAG_MEMBER_SELECTION_SEARCH_FIELD
import kotlinx.coroutines.delay
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

@Category(EndToEndTest::class)
@RunWith(RobolectricTestRunner::class)
class CreatePersonalConversationE2eTest : ConversationBaseTest() {

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
            withTimeout(1000) {
                viewModel.potentialRecipients.filterSuccess().filter { it.isNotEmpty() }.first()
            }
        }

        composeTestRule.waitUntilAtLeastOneExists(
            hasText(user2Name, substring = true) and !hasAnyAncestor(
                hasTestTag(
                    TEST_TAG_MEMBER_SELECTION_SEARCH_FIELD
                )
            )
        )
    }

    @Test
    fun `can create personal conversation`() {
        var conversationCreated = false

        val viewModel = setupUiAndViewModel(
            UnconfinedTestDispatcher(),
            onConversationCreated = {
                conversationCreated = true
            }
        )

        viewModel.addRecipient(User(username = user2Username))

        composeTestRule
            .onNodeWithTag(TEST_TAG_CREATE_PERSONAL_CONVERSATION_BUTTON)
            .performClick()

        composeTestRule.waitUntil { conversationCreated }
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