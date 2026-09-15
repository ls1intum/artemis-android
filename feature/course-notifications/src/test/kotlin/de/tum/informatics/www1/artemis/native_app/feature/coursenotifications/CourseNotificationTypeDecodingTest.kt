package de.tum.informatics.www1.artemis.native_app.feature.coursenotifications

import de.tum.informatics.www1.artemis.native_app.core.common.test.UnitTest
import de.tum.informatics.www1.artemis.native_app.feature.coursenotifications.course_notification_model.CourseNotificationType
import de.tum.informatics.www1.artemis.native_app.feature.coursenotifications.course_notification_model.NotificationSettingsInfo
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * The notification types are the values of a map, so an unrecognised one used to abort the whole
 * response and take the notification settings screen down with it. The server gains types
 * regularly, which makes that a question of when rather than whether.
 */
@Category(UnitTest::class)
@RunWith(RobolectricTestRunner::class)
class CourseNotificationTypeDecodingTest {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        explicitNulls = false
    }

    @Test
    fun `decodes the types the server sends`() {
        // Trimmed from a real GET api/notification/courses/info response.
        val response = """
            {"notificationTypes":{"1":"newPostNotification","2":"newAnswerNotification",
             "9":"quizExerciseStartedNotification","26":"irisResponseNeedsReviewNotification"},
             "channels":[],"presets":[]}
        """.trimIndent()

        val info = json.decodeFromString<NotificationSettingsInfo>(response)

        assertEquals(CourseNotificationType.NEW_POST_NOTIFICATION, info.notificationTypes["1"])
        assertEquals(
            CourseNotificationType.QUIZ_EXERCISE_STARTED_NOTIFICATION,
            info.notificationTypes["9"]
        )
        assertEquals(
            CourseNotificationType.IRIS_RESPONSE_NEEDS_REVIEW_NOTIFICATION,
            info.notificationTypes["26"]
        )
    }

    @Test
    fun `a type this version does not know does not fail the response`() {
        val response = """
            {"notificationTypes":{"1":"newPostNotification","99":"somethingAddedLaterNotification"},
             "channels":[],"presets":[]}
        """.trimIndent()

        val info = json.decodeFromString<NotificationSettingsInfo>(response)

        assertEquals(CourseNotificationType.NEW_POST_NOTIFICATION, info.notificationTypes["1"])
        assertEquals(CourseNotificationType.UNKNOWN, info.notificationTypes["99"])
    }

    @Test
    fun `every type the server can send is known to this version`() {
        // The full set from GET api/notification/courses/info.
        val serverTypes = listOf(
            "newPostNotification", "newAnswerNotification", "newMentionNotification",
            "newAnnouncementNotification", "newExerciseNotification",
            "exerciseOpenForPracticeNotification", "exerciseAssessedNotification",
            "exerciseUpdatedNotification", "quizExerciseStartedNotification",
            "attachmentChangedNotification", "newManualFeedbackRequestNotification",
            "duplicateTestCaseNotification", "newCpcPlagiarismCaseNotification",
            "newPlagiarismCaseNotification", "programmingBuildRunUpdateNotification",
            "programmingTestCasesChangedNotification", "plagiarismCaseVerdictNotification",
            "channelDeletedNotification", "addedToChannelNotification",
            "removedFromChannelNotification", "tutorialGroupAssignedNotification",
            "tutorialGroupDeletedNotification", "tutorialGroupUnassignedNotification",
            "registeredToTutorialGroupNotification", "deregisteredFromTutorialGroupNotification",
            "irisResponseNeedsReviewNotification"
        )

        val types = serverTypes.withIndex().joinToString(",") { (i, name) -> """"$i":"$name"""" }
        val info = json.decodeFromString<NotificationSettingsInfo>(
            """{"notificationTypes":{$types},"channels":[],"presets":[]}"""
        )

        assertEquals(serverTypes.size, info.notificationTypes.size)
        assertFalse(
            "no server type should decode as UNKNOWN",
            info.notificationTypes.values.contains(CourseNotificationType.UNKNOWN)
        )
        assertTrue(info.notificationTypes.values.toSet().size == serverTypes.size)
    }
}
