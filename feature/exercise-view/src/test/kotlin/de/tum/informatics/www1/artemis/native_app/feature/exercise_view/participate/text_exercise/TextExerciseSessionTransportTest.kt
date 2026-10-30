package de.tum.informatics.www1.artemis.native_app.feature.exercise_view.participate.text_exercise

import de.tum.informatics.www1.artemis.native_app.core.common.artemis_context.ArtemisContext
import de.tum.informatics.www1.artemis.native_app.core.common.artemis_context.ArtemisContextImpl
import de.tum.informatics.www1.artemis.native_app.core.common.artemis_context.ArtemisContextProvider
import de.tum.informatics.www1.artemis.native_app.core.common.test.UnitTest
import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.service.KtorProvider
import de.tum.informatics.www1.artemis.native_app.core.data.service.impl.JsonProvider
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.TextSubmission
import de.tum.informatics.www1.artemis.native_app.feature.exerciseview.service.impl.TextEditorServiceImpl
import de.tum.informatics.www1.artemis.native_app.feature.exerciseview.service.impl.TextSubmissionServiceImpl
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
@Category(UnitTest::class)
class TextExerciseSessionTransportTest {
    private val session = ArtemisContextImpl.LoggedIn("https://artemis.example", "captured-token", "student")
    private val context = object : ArtemisContextProvider {
        override val stateFlow = MutableStateFlow<ArtemisContext>(session)
        override fun setCourseId(courseId: Long) = Unit
        override fun clearCourseId() = Unit
    }

    @Test fun `editor redirect cannot forward the captured session to another origin`() = runBlocking {
        withRedirectingClient { provider, requests ->
            val response = TextEditorServiceImpl(provider, context).getParticipation(2, session)
            assertEquals(listOf("artemis.example"), requests)
            assertTrue(response is NetworkResponse.Failure)
        }
    }

    @Test fun `submission redirect cannot forward the captured session or answer to another origin`() = runBlocking {
        withRedirectingClient { provider, requests ->
            val response = TextSubmissionServiceImpl(provider, context).update(
                TextSubmission(id = 3, text = "Private answer", submitted = true), 1, session
            )
            assertEquals(listOf("artemis.example"), requests)
            assertTrue(response is NetworkResponse.Failure)
        }
    }

    private suspend fun withRedirectingClient(block: suspend (KtorProvider, List<String>) -> Unit) {
        val requests = mutableListOf<String>()
        val engine = MockEngine { request ->
            requests += request.url.host
            if (request.url.host == "artemis.example") {
                respond("", HttpStatusCode.TemporaryRedirect, headersOf(HttpHeaders.Location, "https://foreign.example/redirected"))
            } else {
                respond("{}", HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
            }
        }
        val client = HttpClient(engine) {
            expectSuccess = true
            install(ContentNegotiation) { json(JsonProvider().applicationJsonConfiguration) }
        }
        try { block(object : KtorProvider { override val ktorClient = client }, requests) }
        finally { client.close() }
    }
}
