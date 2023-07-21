package de.tum.informatics.www1.artemis.native_app.feature.metis.messages

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasTestTag
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.DefaultTimeoutMillis
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.testServerUrl
import de.tum.informatics.www1.artemis.native_app.feature.login.test.user2Username
import de.tum.informatics.www1.artemis.native_app.feature.metis.ConversationBaseTest
import de.tum.informatics.www1.artemis.native_app.feature.metis.service.network.MetisModificationService
import de.tum.informatics.www1.artemis.native_app.feature.metis.model.dto.conversation.Conversation
import de.tum.informatics.www1.artemis.native_app.feature.metis.model.MetisContext
import de.tum.informatics.www1.artemis.native_app.feature.metis.model.dto.DisplayPriority
import de.tum.informatics.www1.artemis.native_app.feature.metis.model.dto.StandalonePost
import de.tum.informatics.www1.artemis.native_app.feature.metis.service.network.MetisService
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

    protected fun postDefaultMessage(additionalSetup: suspend (StandalonePost) -> Unit = {}): StandalonePost {
        return runBlockingWithTestTimeout {
            metisModificationService.createPost(
                context = metisContext,
                post = createPost("test message"),
                serverUrl = testServerUrl,
                authToken = accessToken
            ).orThrow("Could not create message")
                .also { additionalSetup(it) }
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