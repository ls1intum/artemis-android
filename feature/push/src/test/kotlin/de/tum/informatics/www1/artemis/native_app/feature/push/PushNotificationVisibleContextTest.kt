package de.tum.informatics.www1.artemis.native_app.feature.push

import de.tum.informatics.www1.artemis.native_app.core.common.test.UnitTest
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.MetisContext
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.visiblemetiscontextreporter.VisibleMetisContext
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.visiblemetiscontextreporter.VisiblePostList
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.visiblemetiscontextreporter.VisibleStandalonePostDetails
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.ArtemisNotification
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.CommunicationArtemisNotification
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.CommunicationNotificationType
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.ReplyPostCommunicationNotificationType
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.StandalonePostCommunicationNotificationType
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.target.CommunicationPostTarget
import de.tum.informatics.www1.artemis.native_app.feature.push.service.impl.PushNotificationHandlerImpl
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@Category(UnitTest::class)
@RunWith(RobolectricTestRunner::class)
class PushNotificationVisibleContextTest {

    private val courseId = 1L
    private val conversationId = 2L
    private val postId = 3L

    @Test
    fun `test GIVEN no visible context WHEN calling isNotificationContextInVisibleMetisContexts THEN return false`() {
        val notification = buildNotification(
            type = StandalonePostCommunicationNotificationType.CONVERSATION_NEW_MESSAGE,
            target = buildTarget()
        )
        val visibleMetisContexts = emptyList<VisibleMetisContext>()

        val result = PushNotificationHandlerImpl.isNotificationContextInVisibleMetisContexts(
            notification = notification,
            visibleMetisContexts = visibleMetisContexts
        )

        assert(!result)
    }

    @Test
    fun `test GIVEN a visible chat context and related notification target WHEN calling isNotificationContextInVisibleMetisContexts THEN return true`() {
        val notification = buildNotification(
            type = StandalonePostCommunicationNotificationType.CONVERSATION_NEW_MESSAGE,
            target = buildTarget()
        )
        val visibleMetisContexts = listOf(VisiblePostList(metisContext = MetisContext.Conversation(courseId, conversationId)))

        val result = PushNotificationHandlerImpl.isNotificationContextInVisibleMetisContexts(
            notification = notification,
            visibleMetisContexts = visibleMetisContexts
        )

        assert(result)
    }

    @Test
    fun `test GIVEN a visible chat context and unrelated notification target WHEN calling isNotificationContextInVisibleMetisContexts THEN return false`() {
        val notification = buildNotification(
            type = StandalonePostCommunicationNotificationType.CONVERSATION_NEW_MESSAGE,
            target = buildTarget(conversationId = 4L)
        )
        val visibleMetisContexts = listOf(VisiblePostList(metisContext = MetisContext.Conversation(courseId, conversationId)))

        val result = PushNotificationHandlerImpl.isNotificationContextInVisibleMetisContexts(
            notification = notification,
            visibleMetisContexts = visibleMetisContexts
        )

        assert(!result)
    }

    @Test
    fun `test GIVEN a visible chat context but a thread notification target WHEN calling isNotificationContextInVisibleMetisContexts THEN return false`() {
        val notification = buildNotification(
            type = ReplyPostCommunicationNotificationType.CONVERSATION_NEW_REPLY_MESSAGE,
            target = buildTarget()
        )
        val visibleMetisContexts = listOf(VisiblePostList(metisContext = MetisContext.Conversation(courseId, conversationId)))

        val result = PushNotificationHandlerImpl.isNotificationContextInVisibleMetisContexts(
            notification = notification,
            visibleMetisContexts = visibleMetisContexts
        )

        assert(!result)
    }

    @Test
    fun `test GIVEN a visible thread context and a thread notification target WHEN calling isNotificationContextInVisibleMetisContexts THEN return true`() {
        val notification = buildNotification(
            type = ReplyPostCommunicationNotificationType.CONVERSATION_NEW_REPLY_MESSAGE,
            target = buildTarget()
        )
        val visibleMetisContexts = listOf(VisibleStandalonePostDetails(
            metisContext = MetisContext.Conversation(courseId, conversationId),
            postId = postId
        ))

        val result = PushNotificationHandlerImpl.isNotificationContextInVisibleMetisContexts(
            notification = notification,
            visibleMetisContexts = visibleMetisContexts
        )

        assert(result)
    }


    private fun buildNotification(
        type: CommunicationNotificationType,
        target: CommunicationPostTarget,
    ): ArtemisNotification<CommunicationNotificationType> {
        return CommunicationArtemisNotification(
            type = type,
            notificationPlaceholders = emptyList(),
            target = Json.encodeToString(target),
            date = Clock.System.now(),
            version = 1
        )
    }

    private fun buildTarget(
        courseId: Long = this.courseId,
        conversationId: Long = this.conversationId,
        postId: Long = this.postId
    ): CommunicationPostTarget {
        return CommunicationPostTarget(
            message = "",
            entity = "",
            postId = postId,
            courseId = courseId,
            conversationId = conversationId
        )
    }
}