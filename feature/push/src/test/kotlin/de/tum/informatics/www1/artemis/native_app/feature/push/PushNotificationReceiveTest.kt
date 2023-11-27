package de.tum.informatics.www1.artemis.native_app.feature.push

import androidx.test.platform.app.InstrumentationRegistry
import de.tum.informatics.www1.artemis.native_app.core.common.test.UnitTest
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.MiscArtemisNotification
import de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.MiscNotificationType
import de.tum.informatics.www1.artemis.native_app.feature.push.service.NotificationManager
import de.tum.informatics.www1.artemis.native_app.feature.push.service.PushNotificationHandler
import de.tum.informatics.www1.artemis.native_app.feature.push.service.impl.PushNotificationHandlerImpl
import de.tum.informatics.www1.artemis.native_app.feature.push.service.impl.notification_manager.NotificationManagerImpl
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.datetime.Clock
import kotlinx.serialization.encodeToString
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
        val notification = MiscArtemisNotification(
            type = MiscNotificationType.ATTACHMENT_CHANGE,
            notificationPlaceholders = emptyList(),
            target = "",
            date = Clock.System.now(),
            version = supportedVersion
        )


        pushNotificationHandler.handleServerPushNotification(Json.encodeToString(notification))
        coVerify(exactly = 1) { notificationManagerMock.popNotification(any(), any()) }
    }

    @Test
    fun `should discard notification with non-existing version`() {
        val notification = MiscArtemisNotification(
            type = MiscNotificationType.ATTACHMENT_CHANGE,
            notificationPlaceholders = emptyList(),
            target = "",
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
        val notification = MiscArtemisNotification(
            type = MiscNotificationType.ATTACHMENT_CHANGE,
            notificationPlaceholders = emptyList(),
            target = "",
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