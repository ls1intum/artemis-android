package de.tum.informatics.www1.artemis.native_app.feature.metistest


import de.tum.informatics.www1.artemis.native_app.core.common.test.testServerUrl
import de.tum.informatics.www1.artemis.native_app.feature.login.test.user2Username
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.network.MetisModificationService
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.shared.service.network.MetisService
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.MetisContext
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.DisplayPriority
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.StandalonePost
import kotlinx.datetime.Clock
import org.junit.Before
import org.koin.test.get

abstract class ConversationMessagesBaseTest : ConversationBaseTest() {

    protected val metisModificationService: MetisModificationService get() = get()

    protected lateinit var conversation: de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.Conversation
    protected lateinit var metisContext: MetisContext

    protected val metisService: MetisService get() = get()

    @Before
    override fun setup() {
        super.setup()

        runBlockingWithTestTimeout {
            conversation = conversationService.createOneToOneConversation(
                courseId = course.id!!,
                partner = user2Username,
                authToken = accessToken,
                serverUrl = testServerUrl
            ).orThrow("Could not create one to one conversation")

            metisContext = MetisContext.Conversation(course.id!!, conversation.id)
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