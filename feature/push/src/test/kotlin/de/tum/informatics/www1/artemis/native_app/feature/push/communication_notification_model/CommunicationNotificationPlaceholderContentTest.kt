package de.tum.informatics.www1.artemis.native_app.feature.push.communication_notification_model

import de.tum.informatics.www1.artemis.native_app.core.common.test.UnitTest
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.ReplyPostCommunicationNotificationType
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.StandalonePostCommunicationNotificationType
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@Category(UnitTest::class)
@RunWith(RobolectricTestRunner::class)
class CommunicationNotificationPlaceholderContentTest {

    private val realWorldNewMessageNotificationPlaceholders = listOf(
        "Practical Course: Interactive Learning WS24/25", "debug2", "2025-01-11T12:06:20.918711873Z[Etc/UTC]", "Test User 20 Artemis", "Test User 20 Artemis", "oneToOneChat", "/api/files/user/profile-pictures/52/ProfilePicture_2025-01-11T12-06-09-097_843dd81f.jpeg", "52", "1410"
    )

    private val realWorldNewReplyNotificationPlaceholders = listOf(
        "Practical Course: Interactive Learning WS24/25", "na du nugget", "2024-12-12T12:51:36.221Z", "Test User 1 Artemis", "answer", "2025-01-11T12:11:34.183911547Z[Etc/UTC]", "Test User 20 Artemis", "Test User 20 Artemis", "/api/files/user/profile-pictures/52/ProfilePicture_2025-01-11T12-09-05-525_4651e801.jpeg", "52", "639", "1079"
    )

    private val realWorldAnnouncementNotificationPlaceholders = listOf(
        "Test Notifications & Announcements", "content", "2025-01-17T10:33:30.517854965Z[Etc/UTC]", "announcement", "Martin Felber", "channel", "", "130", "776"
    )

    @Test
    fun `test GIVEN a standalone post notification WHEN parsing the notification placeholders THEN there are no errors`() {
        CommunicationNotificationPlaceholderContent.fromNotificationsPlaceholders(
            type = StandalonePostCommunicationNotificationType.NEW_POST_NOTIFICATION,
            notificationPlaceholders = realWorldNewMessageNotificationPlaceholders
        )
    }

    @Test
    fun `test GIVEN a reply post WHEN parsing the notification placeholders THEN there are no errors`() {
        CommunicationNotificationPlaceholderContent.fromNotificationsPlaceholders(
            type = ReplyPostCommunicationNotificationType.NEW_ANSWER_NOTIFICATION,
            notificationPlaceholders = realWorldNewReplyNotificationPlaceholders
        )
    }

    @Test
    fun `test GIVEN an announcement WHEN parsing the notification placeholders THEN there are no errors`() {
        CommunicationNotificationPlaceholderContent.fromNotificationsPlaceholders(
            type = StandalonePostCommunicationNotificationType.NEW_ANNOUNCEMENT_NOTIFICATION,
            notificationPlaceholders = realWorldAnnouncementNotificationPlaceholders
        )
    }

}