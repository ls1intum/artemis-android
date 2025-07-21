package de.tum.informatics.www1.artemis.native_app.feature.push

import de.tum.informatics.www1.artemis.native_app.core.common.test.UnitTest
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.MetisContext
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.visiblemetiscontextreporter.VisibleMetisContext
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.visiblemetiscontextreporter.VisiblePostList
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.visiblemetiscontextreporter.VisibleStandalonePostDetails
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.ArtemisNotification
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.CommunicationArtemisNotification
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.CommunicationNotificationType
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.CourseNotificationDTO
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.CourseNotificationParameters
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.NotificationCategory
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.NotificationStatus
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.ReplyPostCommunicationNotificationType
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.StandalonePostCommunicationNotificationType
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.target.CommunicationPostTarget
import de.tum.informatics.www1.artemis.native_app.feature.push.service.impl.PushNotificationHandlerImpl
import kotlinx.datetime.Clock
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
            type = StandalonePostCommunicationNotificationType.NEW_POST_NOTIFICATION,
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
            type = StandalonePostCommunicationNotificationType.NEW_POST_NOTIFICATION,
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
            type = StandalonePostCommunicationNotificationType.NEW_POST_NOTIFICATION,
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
            type = ReplyPostCommunicationNotificationType.NEW_ANSWER_NOTIFICATION,
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
            type = ReplyPostCommunicationNotificationType.NEW_ANSWER_NOTIFICATION,
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
            courseNotificationDTO = CourseNotificationDTO(
                notificationType = type,
                notificationId = 1L,
                courseId = target.courseId,
                creationDate = Clock.System.now(),
                category = NotificationCategory.COMMUNICATION,
                parameters = CourseNotificationParameters(
                    postId = if (type is ReplyPostCommunicationNotificationType) null else target.postId,
                    replyId = if (type is ReplyPostCommunicationNotificationType) target.postId else null,
                    channelId = target.conversationId,
                    courseTitle = "Test Course"
                ),
                status = NotificationStatus.UNSEEN
            ),
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