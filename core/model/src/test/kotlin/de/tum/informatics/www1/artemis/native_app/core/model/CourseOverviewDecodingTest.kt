package de.tum.informatics.www1.artemis.native_app.core.model

import de.tum.informatics.www1.artemis.native_app.core.common.test.UnitTest
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.TextExercise
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Artemis 10 answers the course overview from one endpoint per kind of content rather than from a
 * single course dashboard endpoint. The payloads below are real responses, so these tests fail if
 * the app stops being able to read what the server sends.
 *
 * The configuration mirrors the one the network layer installs, in particular
 * {@code ignoreUnknownKeys}: the responses carry fields no screen reads.
 */
@Category(UnitTest::class)
@RunWith(RobolectricTestRunner::class)
class CourseOverviewDecodingTest {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        explicitNulls = false
    }

    @Test
    fun `decodes the course itself`() {
        val response = """
            {"id":1,"title":"Audit Course","startDate":"2026-01-01T00:00:00Z",
             "endDate":"2027-01-01T00:00:00Z","testCourse":true,"onlineCourse":false,
             "enrollmentEnabled":true,"enrollmentEndDate":"2027-01-01T00:00:00Z",
             "unenrollmentEnabled":false,
             "courseInformationSharingConfiguration":"COMMUNICATION_AND_MESSAGING",
             "courseInformationSharingMessagingCodeOfConduct":"Be excellent to each other",
             "accuracyOfScores":1,"complaintsEnabled":true,"maxComplaints":3,
             "requestMoreFeedbackEnabled":true,"athenaGradingFeedbackEnabled":false,
             "courseNotificationCount":0}
        """.trimIndent()

        val course = json.decodeFromString<Course>(response)

        assertEquals(1L, course.id)
        assertEquals("Audit Course", course.title)
        assertTrue(course.testCourse)
        assertEquals(
            Course.CourseInformationSharingConfiguration.COMMUNICATION_AND_MESSAGING,
            course.courseInformationSharingConfiguration
        )
        assertEquals(
            "Be excellent to each other",
            course.courseInformationSharingMessagingCodeOfConduct
        )
        // The server sends this as a JSON integer, the model holds a Float.
        assertEquals(1f, course.accuracyOfScores, 0f)
    }

    @Test
    fun `decodes the available tabs`() {
        val response = """
            {"lectures":false,"exams":false,"competencies":false,"tutorialGroups":false,
             "iris":false,"faq":true,"learningPaths":false,"communication":true,"training":false}
        """.trimIndent()

        val tabs = json.decodeFromString<CourseAvailableTabs>(response)

        assertTrue(tabs.faq)
        assertTrue(tabs.communication)
        assertFalse(tabs.lectures)
    }

    @Test
    fun `decodes an exercise into its concrete type using the discriminator`() {
        val response = """
            {"exercises":[{"type":"text","id":1,"title":"Audit Text Exercise","maxPoints":10.0,
             "bonusPoints":0.0,"releaseDate":"2026-01-02T00:00:00Z","dueDate":"2027-01-01T00:00:00Z",
             "assessmentType":"MANUAL","difficulty":"EASY","mode":"INDIVIDUAL","teamMode":false,
             "includedInOverallScore":"INCLUDED_COMPLETELY","presentationScoreEnabled":false,
             "allowFeedbackRequests":false,"studentAssignedTeamIdComputed":false}]}
        """.trimIndent()

        val overview = json.decodeFromString<CourseExercisesForOverview>(response)

        assertEquals(1, overview.exercises.size)
        val exercise = overview.exercises.single()
        assertTrue(exercise is TextExercise)
        assertEquals(1L, exercise.id)
        assertEquals("Audit Text Exercise", exercise.title)
        assertEquals(10f, exercise.maxPoints!!, 0f)
    }

    @Test
    fun `a course without exercises omits the field entirely`() {
        // The response is @JsonInclude(NON_EMPTY), so an empty set is left out rather than sent as [].
        val response = """
            {"totalScores":{"maxPoints":0.0,"reachablePoints":0.0,"studentScores":{}}}
        """.trimIndent()

        val overview = json.decodeFromString<CourseExercisesForOverview>(response)

        assertTrue(overview.exercises.isEmpty())
    }

    @Test
    fun `decodes the lectures of a course that has none`() {
        val lectures = json.decodeFromString<List<
            de.tum.informatics.www1.artemis.native_app.core.model.lecture.Lecture>>("[]")

        assertTrue(lectures.isEmpty())
    }

    @Test
    fun `decodes a lecture projected for the overview`() {
        val response = """
            [{"id":3,"title":"Week 1","startDate":"2026-01-02T00:00:00Z",
              "endDate":"2026-01-03T00:00:00Z","isTutorialLecture":false}]
        """.trimIndent()

        val lectures = json.decodeFromString<List<
            de.tum.informatics.www1.artemis.native_app.core.model.lecture.Lecture>>(response)

        assertEquals(1, lectures.size)
        assertEquals(3L, lectures.single().id)
        assertEquals("Week 1", lectures.single().title)
        // Not part of the overview projection; the lecture detail endpoint carries them.
        assertTrue(lectures.single().lectureUnits.isEmpty())
    }
}
