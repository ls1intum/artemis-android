package de.tum.informatics.www1.artemis.native_app.feature.exercise_view.viewresult

import de.tum.informatics.www1.artemis.native_app.core.common.artemis_context.ArtemisContextImpl
import de.tum.informatics.www1.artemis.native_app.core.common.test.UnitTest
import de.tum.informatics.www1.artemis.native_app.core.data.service.KtorProvider
import de.tum.informatics.www1.artemis.native_app.core.data.service.impl.JsonProvider
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.TextExercise
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.Result
import de.tum.informatics.www1.artemis.native_app.feature.exerciseview.viewresult.*
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.experimental.categories.Category

@Category(UnitTest::class)
class ResultReviewServiceTest {
    private val session = ArtemisContextImpl.LoggedIn("https://artemis.example", "captured-token", "student")
    private val target = ReviewTarget(5, 11, Result(id = 7), null)

    @Test fun `text review fetches the exact result and never substitutes a different submission`() = runBlocking {
        val engine = MockEngine { request ->
            assertEquals(HttpMethod.Get, request.method)
            assertEquals("jwt=captured-token", request.headers[HttpHeaders.Cookie])
            val payload = when (request.url.encodedPath) {
                "/api/assessment/participations/5/results/7/details" -> """[{"id":1,"detailText":"Assessed feedback"}]"""
                "/api/text/participations/5/text-editor" -> {
                    assertEquals("7", request.url.parameters["resultId"])
                    """{"id":5,"submissions":[{"id":12,"text":"Newer draft","results":[]},{"id":11,"text":"Exact assessed answer","results":[{"id":7}]}]}"""
                }
                else -> error("Unexpected endpoint: ${request.url.encodedPath}")
            }
            respond(payload, HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
        }
        withService(engine) { service ->
            val content = service.load(session, TextExercise(id = 3), target)
            assertEquals(11L, content.submission?.id)
            assertEquals("Exact assessed answer", content.submission?.text)
            assertEquals("Assessed feedback", content.feedback.single().message)
        }
    }

    @Test fun `missing historical submission stays unavailable and does not read the new draft`() = runBlocking {
        val engine = MockEngine { request ->
            val payload = if (request.url.encodedPath.endsWith("details")) "[]" else """{"id":5,"submissions":[{"id":12,"text":"Newer work","results":[{"id":8}]}]}"""
            respond(payload, HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
        }
        withService(engine) { service ->
            try { service.load(session, TextExercise(id = 3), target); fail("Must not substitute latest work") }
            catch (error: ReviewHttpException) { assertEquals(404, error.status) }
        }
    }

    @Test fun `signed long output and build logs retain selected identity and captured account`() = runBlocking {
        val requests = mutableListOf<String>()
        val engine = MockEngine { request ->
            assertEquals("jwt=captured-token", request.headers[HttpHeaders.Cookie])
            requests += request.url.encodedPath
            if (request.url.encodedPath.endsWith("buildlogs")) {
                assertEquals("7", request.url.parameters["resultId"])
                respond("""[{"log":"Selected result log"}]""", HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
            } else respond("Full diagnostic output", HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "text/plain"))
        }
        withService(engine) { service ->
            assertEquals("Full diagnostic output", service.longFeedback(session, -9223372036854775000L))
            assertEquals("Selected result log", service.buildLogs(session, target).single().log)
            assertEquals(listOf("/api/assessment/feedbacks/-9223372036854775000/long-feedback", "/api/programming/participations/5/buildlogs"), requests)
        }
    }

    @Test fun `repository reads use the assessed full commit and never request HEAD`() = runBlocking {
        val commit = "a".repeat(40)
        val engine = MockEngine { request ->
            assertTrue(request.url.encodedPath.startsWith("/api/programming/participations/5/repository/commits/$commit/"))
            if (request.url.encodedPath.endsWith("files")) respond("""{"src/Search.java":"FILE","src":"FOLDER"}""", HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
            else {
                assertEquals("src/Search.java", request.url.parameters["file"])
                respond("class Search {}", HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/octet-stream"))
            }
        }
        withService(engine) { service ->
            assertEquals(listOf("src/Search.java"), service.commitFiles(session, target, commit))
            assertEquals("class Search {}", service.commitFile(session, target, commit, "src/Search.java"))
        }
    }

    @Test fun `access denial is distinguishable and protected source is not loaded`() = runBlocking {
        var calls = 0
        val engine = MockEngine { calls++; respond("", HttpStatusCode.Forbidden) }
        withService(engine) { service ->
            try { service.load(session, TextExercise(id = 3), target); fail("Access denied") }
            catch (error: Exception) { assertTrue(isReviewAccessDenied(error)) }
            assertEquals(1, calls)
        }
        assertFalse(sameReviewSession(session, ArtemisContextImpl.Empty))
        assertFalse(sameReviewSession(session, session.copy(loginName = "other")))
        assertFalse(sameReviewSession(session, session.copy(serverUrl = "https://other.example")))
        assertFalse(sameReviewSession(session, session.copy(authToken = "new-token")))
        assertTrue(sameReviewSession(session, session.copy()))
    }

    @Test fun `authenticated submitted file never follows redirects to a different server`() = runBlocking {
        var calls = 0
        val engine = MockEngine { request ->
            calls++
            if (request.url.host == "artemis.example") respond("", HttpStatusCode.Found, headersOf(HttpHeaders.Location, "https://other.example/stolen.pdf"))
            else respond("untrusted redirected file", HttpStatusCode.OK)
        }
        withService(engine) { service ->
            try { service.file(session, "https://artemis.example/api/core/files/answer.pdf"); fail("Redirect must stay unavailable") }
            catch (error: ReviewHttpException) { assertEquals(302, error.status) }
            assertEquals(1, calls)
        }
    }

    @Test fun `published solution uses its independent student authorization and validates exact exercise`() = runBlocking {
        val engine = MockEngine { request ->
            assertEquals(HttpMethod.Get, request.method)
            assertEquals("/api/exercise/exercises/3/example-solution", request.url.encodedPath)
            assertEquals("jwt=captured-token", request.headers[HttpHeaders.Cookie])
            respond("""{"id":3,"type":"modeling","exampleSolutionModel":"{\"nodes\":[]}","exampleSolutionExplanation":"Public model explanation"}""", HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
        }
        withService(engine) { service ->
            val exercise = service.publishedSolution(session, 3)
            assertEquals(3L, exercise.id)
            assertEquals("Public model explanation", (exercise as de.tum.informatics.www1.artemis.native_app.core.model.exercise.ModelingExercise).exampleSolutionExplanation)
        }
        assertEquals("https://artemis.example/courses/9/exercises/3", reviewExampleSolutionUrl(session.serverUrl, 9, 3))
        assertFalse(reviewExampleSolutionUrl(session.serverUrl, 9, 3).contains(session.authToken))
    }

    @Test fun `redirected result details never forward the captured cookie or read submitted work`() = runBlocking {
        var calls = 0
        val engine = MockEngine { request ->
            calls++
            if (request.url.host == "artemis.example") respond("", HttpStatusCode.Found, headersOf(HttpHeaders.Location, "https://foreign.example/details"))
            else respond("[]", HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
        }
        withService(engine) { service ->
            try { service.load(session, TextExercise(id = 3), target); fail("Must reject redirect before leaving captured origin") }
            catch (error: Exception) { assertEquals(1, calls) }
        }
    }

    private suspend fun withService(engine: MockEngine, block: suspend (ResultReviewService) -> Unit) {
        val client = HttpClient(engine) { expectSuccess = true; install(ContentNegotiation) { json(JsonProvider().applicationJsonConfiguration) } }
        try { block(ResultReviewService(object : KtorProvider { override val ktorClient = client })) }
        finally { client.close() }
    }
}
