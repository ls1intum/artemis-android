package de.tum.informatics.www1.artemis.native_app.feature.push

import androidx.test.platform.app.InstrumentationRegistry
import de.tum.informatics.www1.artemis.native_app.core.common.test.UnitTest
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.CourseNotificationDTO
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.CourseNotificationParameters
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.GeneralArtemisNotification
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.GeneralNotificationType
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.NotificationCategory
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.NotificationStatus
import de.tum.informatics.www1.artemis.native_app.feature.push.service.NotificationManager
import de.tum.informatics.www1.artemis.native_app.feature.push.service.PushNotificationHandler
import de.tum.informatics.www1.artemis.native_app.feature.push.service.impl.PushNotificationHandlerImpl
import de.tum.informatics.www1.artemis.native_app.feature.push.service.impl.notification_manager.NotificationManagerImpl
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json
import org.junit.Before
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@Category(UnitTest::class)
@RunWith(RobolectricTestRunner::class)
internal class PushNotificationReceiveTest {

    private val supportedVersion = 2

    private val notificationManagerMock: NotificationManager = mockk<NotificationManagerImpl>()

    private val pushNotificationHandler: PushNotificationHandler = PushNotificationHandlerImpl(
        InstrumentationRegistry.getInstrumentation().context,
        notificationManagerMock,
        supportedVersion
    )

    @Before
    fun setup() {
        coEvery { notificationManagerMock.popNotification(any(), any()) } returns Unit
    }

    @Test
    fun `should forward notification with valid version`() {
        val notification = GeneralArtemisNotification(
            courseNotificationDTO = CourseNotificationDTO(
                notificationType = GeneralNotificationType.ATTACHMENT_CHANGED_NOTIFICATION,
                notificationId = 1L,
                courseId = 1L,
                creationDate = Clock.System.now(),
                category = NotificationCategory.GENERAL,
                parameters = CourseNotificationParameters(
                    courseTitle = "Test Course",
                    attachmentName = "test.pdf"
                ),
                status = NotificationStatus.UNSEEN
            ),
            date = Clock.System.now(),
            version = supportedVersion
        )

        pushNotificationHandler.handleServerPushNotification(Json.encodeToString(notification))
        coVerify(exactly = 1) { notificationManagerMock.popNotification(any(), any()) }
    }

    @Test
    fun `should discard notification with non-existing version`() {
        val notification = GeneralArtemisNotification(
            courseNotificationDTO = CourseNotificationDTO(
                notificationType = GeneralNotificationType.ATTACHMENT_CHANGED_NOTIFICATION,
                notificationId = 1L,
                courseId = 1L,
                creationDate = Clock.System.now(),
                category = NotificationCategory.GENERAL,
                parameters = CourseNotificationParameters(),
                status = NotificationStatus.UNSEEN
            ),
            date = Clock.System.now(),
            version = supportedVersion
        )

        val versionRegex = ",\\s*\"version\"\\s*:\\s*\\d*".toRegex()
        val payload = Json.encodeToString(notification).replace(versionRegex, "")

        pushNotificationHandler.handleServerPushNotification(payload)
        coVerify(exactly = 0) { notificationManagerMock.popNotification(any(), any()) }
    }

    @Test
    fun `should discard notification with non-matching version`() {
        val notification = GeneralArtemisNotification(
            courseNotificationDTO = CourseNotificationDTO(
                notificationType = GeneralNotificationType.ATTACHMENT_CHANGED_NOTIFICATION,
                notificationId = 1L,
                courseId = 1L,
                creationDate = Clock.System.now(),
                category = NotificationCategory.GENERAL,
                parameters = CourseNotificationParameters(),
                status = NotificationStatus.UNSEEN
            ),
            date = Clock.System.now(),
            version = supportedVersion + 1
        )

        pushNotificationHandler.handleServerPushNotification(Json.encodeToString(notification))
        coVerify(exactly = 0) { notificationManagerMock.popNotification(any(), any()) }
    }

    @Test
    fun `should discard gibberish notification`() {
        pushNotificationHandler.handleServerPushNotification("{\"foo\": 143, \"version\": $supportedVersion}")
        coVerify(exactly = 0) { notificationManagerMock.popNotification(any(), any()) }
    }
}