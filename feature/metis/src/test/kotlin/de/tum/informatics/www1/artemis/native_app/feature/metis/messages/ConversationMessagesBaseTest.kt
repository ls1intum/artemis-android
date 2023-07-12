package de.tum.informatics.www1.artemis.native_app.feature.metis.messages

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.DefaultTimeoutMillis
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.testServerUrl
import de.tum.informatics.www1.artemis.native_app.feature.login.test.user2Username
import de.tum.informatics.www1.artemis.native_app.feature.metis.ConversationBaseTest
import de.tum.informatics.www1.artemis.native_app.feature.metis.MetisModificationService
import de.tum.informatics.www1.artemis.native_app.feature.metis.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.content.Conversation
import de.tum.informatics.www1.artemis.native_app.feature.metis.model.MetisContext
import de.tum.informatics.www1.artemis.native_app.feature.metis.model.dto.DisplayPriority
import de.tum.informatics.www1.artemis.native_app.feature.metis.model.dto.StandalonePost
import de.tum.informatics.www1.artemis.native_app.feature.metis.service.MetisService
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.reply.TEST_TAG_CAN_CREATE_REPLY
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.reply.TEST_TAG_REPLY_SEND_BUTTON
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.reply.TEST_TAG_REPLY_TEXT_FIELD
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.datetime.Clock
import org.junit.Before
import org.koin.test.get

@OptIn(ExperimentalTestApi::class)
abstract class ConversationMessagesBaseTest : ConversationBaseTest() {

    protected val metisModificationService: MetisModificationService get() = get()

    protected val replyTextFieldMatcher =
        hasAnyAncestor(hasTestTag(TEST_TAG_REPLY_TEXT_FIELD)) and hasSetTextAction()

    protected lateinit var conversation: Conversation
    protected lateinit var metisContext: MetisContext

    protected val metisService: MetisService get() = get()

    @Before
    override fun setup() {
        super.setup()

        runBlocking {
            withTimeout(DefaultTimeoutMillis) {
                conversation = conversationService.createOneToOneConversation(
                    courseId = course.id!!,
                    partner = user2Username,
                    authToken = accessToken,
                    serverUrl = testServerUrl
                ).orThrow("Could not create one to one conversation")

                metisContext = MetisContext.Conversation(course.id!!, conversation.id)
            }
        }
    }

    /**
     * Enters the given text into the reply text field and clicks send. Checks if the entered text
     * appears in the node specified by [listTestTag]
     */
    fun canSendTestImpl(text: String, listTestTag: String, forceRefresh: () -> Unit) {
        composeTestRule
            .onNodeWithText(context.getString(R.string.create_answer_click_to_write))
            .performClick()

        composeTestRule
            .onNode(replyTextFieldMatcher)
            .performTextInput(text)

        composeTestRule
            .onNodeWithTag(TEST_TAG_REPLY_SEND_BUTTON)
            .performClick()

        testDispatcher.scheduler.runCurrent()

        composeTestRule
            .waitUntilExactlyOneExists(hasTestTag(TEST_TAG_CAN_CREATE_REPLY), DefaultTimeoutMillis)

        forceRefresh()

        testDispatcher.scheduler.runCurrent()

        composeTestRule
            .waitUntilExactlyOneExists(
                hasAnyAncestor(hasTestTag(listTestTag)) and hasText(
                    text
                ),
                DefaultTimeoutMillis
            )
    }

    protected fun postDefaultMessage(additionalSetup: suspend (StandalonePost) -> Unit = {}): StandalonePost {
        return runBlocking {
            withTimeout(DefaultTimeoutMillis) {
                metisModificationService.createPost(
                    context = metisContext,
                    post = createPost("test message"),
                    serverUrl = testServerUrl,
                    authToken = accessToken
                ).orThrow("Could not create message")
                    .also { additionalSetup(it) }
            }
        }
    }

    protected fun createPost(content: String): StandalonePost {
        return StandalonePost(
            id = null,
            title = null,
            tags = null,
            content = content,
            conversation = conversation,
            creationDate = Clock.System.now(),
            displayPriority = DisplayPriority.NONE
        )
    }
}