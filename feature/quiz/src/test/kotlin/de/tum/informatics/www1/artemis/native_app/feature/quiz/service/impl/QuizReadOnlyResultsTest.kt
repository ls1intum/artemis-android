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
}
