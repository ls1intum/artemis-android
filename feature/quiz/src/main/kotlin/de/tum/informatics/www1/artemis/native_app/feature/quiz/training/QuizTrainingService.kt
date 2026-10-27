package de.tum.informatics.www1.artemis.native_app.feature.quiz.training

import de.tum.informatics.www1.artemis.native_app.core.data.cookieAuth
import de.tum.informatics.www1.artemis.native_app.core.data.service.*
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.quiz.SubmittedAnswer
import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.json.JsonObject

internal interface QuizTrainingApi {
    suspend fun loadOverview(): TrainingOverview

    suspend fun loadSettings(): TrainingPrivacy

    suspend fun updatePrivacy(show: Boolean)

    suspend fun loadPage(request: TrainingPageRequest): TrainingPage

    suspend fun submit(question: TrainingQuestion, payload: JsonObject): SubmittedAnswer
}

/**
 * Every call is pinned to the originating account and rejects redirects. No automatic POST retries.
 */
internal class QuizTrainingService(
    private val provider: KtorProvider,
    private val origin: String,
    private val token: String,
    private val courseId: Long,
) : QuizTrainingApi {
    override suspend fun loadOverview(): TrainingOverview =
        provider.studentReviewGet(
            origin,
            token,
            "api",
            "quiz",
            "courses",
            courseId.toString(),
            "training",
            "leaderboard",
        )

    override suspend fun loadSettings(): TrainingPrivacy =
        provider.studentReviewGet(origin, token, "api", "quiz", "leaderboard-settings")

    override suspend fun updatePrivacy(show: Boolean) {
        request<Unit, TrainingPrivacy>(
            HttpMethod.Put,
            listOf("leaderboard-settings"),
            TrainingPrivacy(show),
        )
    }

    override suspend fun loadPage(request: TrainingPageRequest): TrainingPage {
        val client =
            provider.ktorClient.config {
                followRedirects = false
                expectSuccess = false
            }
        try {
            val response =
                client.post(origin) {
                    cookieAuth(token)
                    contentType(ContentType.Application.Json)
                    setBody(request.questionIds)
                    url {
                        appendPathSegments(
                            "api",
                            "quiz",
                            "courses",
                            courseId.toString(),
                            "training-questions",
                        )
                        parameters.append("page", request.page.toString())
                        parameters.append("size", "10")
                        parameters.append("sort", "id,asc")
                        parameters.append("isNewSession", request.isNewSession.toString())
                    }
                }
            if (!response.status.isSuccess())
                throw StudentReviewHttpException(response.status.value)
            val hasNext =
                response.headers["X-Has-Next"]?.toBooleanStrictOrNull()
                    ?: error("Missing training pagination header")
            return TrainingPage(response.body(), hasNext)
        } finally {
            client.close()
        }
    }

    override suspend fun submit(question: TrainingQuestion, payload: JsonObject): SubmittedAnswer =
        request(
            HttpMethod.Post,
            listOf(
                "courses",
                courseId.toString(),
                "training-questions",
                question.quizQuestionWithSolutionDTO.id.toString(),
                "submit",
            ),
            payload,
            mapOf("isRated" to question.isRated.toString()),
        )

    private suspend inline fun <reified T, reified B> request(
        method: HttpMethod,
        path: List<String>,
        payload: B,
        query: Map<String, String> = emptyMap(),
    ): T {
        val client =
            provider.ktorClient.config {
                followRedirects = false
                expectSuccess = false
            }
        try {
            val response =
                client.request(origin) {
                    this.method = method
                    cookieAuth(token)
                    contentType(ContentType.Application.Json)
                    setBody(payload)
                    url {
                        appendPathSegments("api", "quiz", *path.toTypedArray())
                        query.forEach { (key, value) -> parameters.append(key, value) }
                    }
                }
            if (!response.status.isSuccess())
                throw StudentReviewHttpException(response.status.value)
            return if (T::class == Unit::class) Unit as T else response.body()
        } finally {
            client.close()
        }
    }
}
