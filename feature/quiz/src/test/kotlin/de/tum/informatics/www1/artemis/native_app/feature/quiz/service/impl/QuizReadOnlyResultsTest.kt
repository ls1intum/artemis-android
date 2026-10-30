package de.tum.informatics.www1.artemis.native_app.feature.quiz.service.impl

import de.tum.informatics.www1.artemis.native_app.core.common.test.UnitTest
import de.tum.informatics.www1.artemis.native_app.core.data.service.KtorProvider
import de.tum.informatics.www1.artemis.native_app.core.data.service.impl.JsonProvider
import de.tum.informatics.www1.artemis.native_app.feature.quiz.quizSubmission
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.experimental.categories.Category

@org.junit.runner.RunWith(org.robolectric.RobolectricTestRunner::class)
@Category(UnitTest::class)
class QuizReadOnlyResultsTest {
    @Test fun `result review uses only GET and pins the evaluated submission rather than a newer draft`() = runBlocking {
        val requests = mutableListOf<String>()
        val engine = MockEngine { request ->
            assertEquals(HttpMethod.Get, request.method)
            assertEquals("jwt=student-token", request.headers[HttpHeaders.Cookie])
            requests += request.url.encodedPath
            val payload = if (request.url.encodedPath.endsWith("details")) """{"exercise":{"id":42,"type":"quiz","studentParticipations":[{"id":5,"type":"student","submissions":[{"id":10,"submissionExerciseType":"quiz","results":[{"id":7,"rated":true,"completionDate":"2026-10-21T00:00:00Z"}]},{"id":11,"submissionExerciseType":"quiz","submitted":false}]}]}}""" else {
                assertEquals("10", request.url.parameters["submissionId"])
                """{"id":5,"type":"student","exercise":{"id":42,"type":"quiz"},"submissions":[{"id":10,"submissionExerciseType":"quiz","results":[{"id":7,"rated":true}]}]}"""
            }
            respond(payload, HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
        }
        val client = HttpClient(engine) { install(ContentNegotiation) { json(JsonProvider().applicationJsonConfiguration) } }
        try {
            val service = QuizExerciseServiceImpl(object : KtorProvider { override val ktorClient = client })
            val result = service.findResultParticipation(42, "https://artemis.example", "student-token").orThrow()
            assertEquals(10, result.quizSubmission?.id)
            assertEquals(listOf("/api/exercise/exercises/42/details", "/api/quiz/quiz-exercises/42/participations/5/result"), requests)
        } finally { client.close() }
    }
    @Test fun `default review retains graded context when a newer practice attempt exists and exposes supplied history`() = runBlocking {
        val details = """{"exercise":{"id":42,"type":"quiz","studentParticipations":[{"id":5,"type":"student","testRun":false,"submissions":[{"id":10,"submissionExerciseType":"quiz","submissionDate":"2026-10-21T00:00:00Z","results":[{"id":7,"rated":true,"completionDate":"2026-10-21T00:00:00Z"}]}]},{"id":6,"type":"student","testRun":true,"submissions":[{"id":11,"submissionExerciseType":"quiz","submissionDate":"2026-10-25T00:00:00Z","results":[{"id":8,"rated":false,"completionDate":"2026-10-25T00:00:00Z"}]}]}]}}"""
        val engine = MockEngine { request ->
            if (request.url.encodedPath.endsWith("details")) respond(details, HttpStatusCode.OK, headersOf(HttpHeaders.ContentType,"application/json"))
            else {
                assertEquals("/api/quiz/quiz-exercises/42/participations/5/result", request.url.encodedPath)
                assertEquals("10",request.url.parameters["submissionId"])
                respond("""{"id":5,"type":"student","exercise":{"id":42,"type":"quiz"},"submissions":[{"id":10,"submissionExerciseType":"quiz","results":[{"id":7,"rated":true}]}]}""",HttpStatusCode.OK,headersOf(HttpHeaders.ContentType,"application/json"))
            }
        }
        val client = HttpClient(engine) { install(ContentNegotiation) { json(JsonProvider().applicationJsonConfiguration) } }
        try {
            val service = QuizExerciseServiceImpl(object:KtorProvider { override val ktorClient=client })
            assertEquals(10, service.findResultParticipation(42,"https://artemis.example","token").orThrow().quizSubmission?.id)
            val attempts = service.findReviewAttempts(42,"https://artemis.example","token").orThrow()
            assertEquals(setOf(10,11),attempts.map { it.submissionId }.toSet())
            assertTrue(attempts.single { it.submissionId==11 }.practice)
            assertEquals(true, attempts.single { it.submissionId==10 }.rated)
        } finally { client.close() }
    }

    @Test fun `quiz metadata redirects never forward the captured cookie to another authority`() = runBlocking {
        var calls = 0
        val engine = MockEngine { calls++; respond("",HttpStatusCode.Found,headersOf(HttpHeaders.Location,"https://foreign.example/quiz")) }
        val client = HttpClient(engine) { install(ContentNegotiation) { json(JsonProvider().applicationJsonConfiguration) } }
        try {
            val service = QuizExerciseServiceImpl(object:KtorProvider { override val ktorClient=client })
            assertTrue(service.findForStudent(42,"https://artemis.example","private-token") is de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse.Failure)
            assertEquals(1,calls)
        } finally { client.close() }
    }

}
