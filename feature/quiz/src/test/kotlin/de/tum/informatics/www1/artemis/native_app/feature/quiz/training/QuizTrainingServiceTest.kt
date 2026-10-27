package de.tum.informatics.www1.artemis.native_app.feature.quiz.training

import de.tum.informatics.www1.artemis.native_app.core.common.test.UnitTest
import de.tum.informatics.www1.artemis.native_app.core.data.service.KtorProvider
import de.tum.informatics.www1.artemis.native_app.core.data.service.impl.JsonProvider
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.quiz.MultipleChoiceQuizQuestion
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.*
import io.ktor.http.content.TextContent
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.experimental.categories.Category

@org.junit.runner.RunWith(org.robolectric.RobolectricTestRunner::class)
@Category(UnitTest::class)
class QuizTrainingServiceTest {
    private fun client(engine: MockEngine) =
        HttpClient(engine) {
            install(ContentNegotiation) { json(JsonProvider().applicationJsonConfiguration) }
        }

    @Test
    fun `captured requests preserve page snapshot and live entity payload without extra entry creation`() =
        runBlocking {
            val paths = mutableListOf<String>()
            val engine = MockEngine { r ->
                paths += r.url.encodedPath
                assertEquals("jwt=captured-token", r.headers[HttpHeaders.Cookie])
                assertEquals("captured.example", r.url.host)
                when {
                    r.url.encodedPath.endsWith("leaderboard") -> {
                        assertEquals(HttpMethod.Get, r.method)
                        respond(
                            """{"hasUserSetSettings":false} """,
                            headers = headersOf(HttpHeaders.ContentType, "application/json"),
                        )
                    }
                    r.url.encodedPath.endsWith("submit") -> {
                        assertEquals("false", r.url.parameters["isRated"])
                        val body = Json.parseToJsonElement((r.body as TextContent).text).jsonObject
                        assertEquals(
                            42L,
                            body["quizQuestion"]!!.jsonObject["id"]!!.jsonPrimitive.long,
                        )
                        assertEquals(
                            8L,
                            body["selectedOptions"]!!
                                .jsonArray
                                .single()
                                .jsonObject["id"]!!
                                .jsonPrimitive
                                .long,
                        )
                        respond(
                            """{"type":"multiple-choice","quizQuestion":{"type":"multiple-choice","id":42},"scoreInPoints":0.5} """,
                            headers = headersOf(HttpHeaders.ContentType, "application/json"),
                        )
                    }
                    else -> {
                        assertEquals("1", r.url.parameters["page"])
                        assertEquals("10", r.url.parameters["size"])
                        assertEquals("id,asc", r.url.parameters["sort"])
                        assertEquals("false", r.url.parameters["isNewSession"])
                        assertEquals("[80,90]", (r.body as TextContent).text)
                        respond(
                            "[]",
                            headers =
                                headersOf(
                                    HttpHeaders.ContentType to listOf("application/json"),
                                    "X-Has-Next" to listOf("false"),
                                ),
                        )
                    }
                }
            }
            val c = client(engine)
            try {
                val api =
                    QuizTrainingService(
                        object : KtorProvider {
                            override val ktorClient = c
                        },
                        "https://captured.example",
                        "captured-token",
                        7,
                    )
                assertEquals(false, api.loadOverview().hasUserSetSettings)
                api.loadPage(TrainingPageRequest(1, listOf(80, 90), false))
                val q =
                    TrainingQuestion(
                        MultipleChoiceQuizQuestion(
                            id = 42,
                            answerOptions = listOf(MultipleChoiceQuizQuestion.AnswerOption(id = 8)),
                        ),
                        false,
                        null,
                        false,
                    )
                assertEquals(
                    0.5,
                    api.submit(
                            q,
                            trainingPayload(
                                q.quizQuestionWithSolutionDTO,
                                TrainingDraft(options = setOf(8)),
                            ),
                        )
                        .scoreInPoints,
                )
                assertEquals(
                    listOf(
                        "/api/quiz/courses/7/training/leaderboard",
                        "/api/quiz/courses/7/training-questions",
                        "/api/quiz/courses/7/training-questions/42/submit",
                    ),
                    paths,
                )
            } finally {
                c.close()
            }
        }

    @Test
    fun `redirects are rejected without following and missing pagination header fails`() =
        runBlocking {
            for (redirect in listOf(true, false)) {
                var calls = 0
                val c =
                    client(
                        MockEngine {
                            calls++
                            if (redirect)
                                respond(
                                    "",
                                    HttpStatusCode.Found,
                                    headersOf(HttpHeaders.Location, "https://foreign.example"),
                                )
                            else
                                respond(
                                    "[]",
                                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                                )
                        }
                    )
                try {
                    val api =
                        QuizTrainingService(
                            object : KtorProvider {
                                override val ktorClient = c
                            },
                            "https://captured.example",
                            "token",
                            7,
                        )
                    assertTrue(runCatching { api.loadPage(TrainingPageRequest()) }.isFailure)
                    assertEquals(1, calls)
                } finally {
                    c.close()
                }
            }
        }
}
