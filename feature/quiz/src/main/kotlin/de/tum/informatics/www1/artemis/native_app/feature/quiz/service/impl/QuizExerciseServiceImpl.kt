package de.tum.informatics.www1.artemis.native_app.feature.quiz.service.impl

import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.performNetworkCall
import de.tum.informatics.www1.artemis.native_app.core.data.service.impl.KtorProvider
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.QuizExercise
import de.tum.informatics.www1.artemis.native_app.feature.quiz.service.QuizExerciseService
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.Serializable

internal class QuizExerciseServiceImpl(
    private val ktorProvider: KtorProvider
) : QuizExerciseService {

    companion object {
        private val resourcePathSegments = listOf("api", "quiz-exercises")
    }

    override suspend fun start(
        exerciseId: Long,
        serverUrl: String,
        authToken: String
    ): NetworkResponse<QuizExercise> {
        return performNetworkCall {
            ktorProvider.ktorClient.put(serverUrl) {
                url {
                    appendPathSegments(resourcePathSegments)
                    appendPathSegments(exerciseId.toString(), "start-now")
                }

                contentType(ContentType.Application.Json)
                bearerAuth(authToken)
                setBody<QuizExercise?>(null)
            }.body()
        }
    }

    override suspend fun end(
        exerciseId: Long,
        serverUrl: String,
        authToken: String
    ): NetworkResponse<QuizExercise> {
        return performNetworkCall {
            ktorProvider.ktorClient.put(serverUrl) {
                url {
                    appendPathSegments(resourcePathSegments)
                    appendPathSegments(exerciseId.toString(), "end-now")
                }

                contentType(ContentType.Application.Json)
                bearerAuth(authToken)
                setBody<QuizExercise?>(null)
            }.body()
        }
    }

    override suspend fun findForStudent(
        exerciseId: Long,
        serverUrl: String,
        authToken: String
    ): NetworkResponse<QuizExercise> {
        return performNetworkCall {
            ktorProvider.ktorClient.get(serverUrl) {
                url {
                    appendPathSegments(resourcePathSegments)
                    appendPathSegments(exerciseId.toString(), "for-student")
                }

                contentType(ContentType.Application.Json)
                bearerAuth(authToken)
            }.body()
        }
    }

    override suspend fun join(
        exerciseId: Long,
        password: String,
        serverUrl: String,
        authToken: String
    ): NetworkResponse<QuizExercise.QuizBatch> {
        return performNetworkCall {
            ktorProvider.ktorClient.post(serverUrl) {
                url {
                    appendPathSegments(resourcePathSegments)
                    appendPathSegments(exerciseId.toString(), "join")
                }

                setBody(Password(password))
                contentType(ContentType.Application.Json)
                bearerAuth(authToken)
            }.body()
        }
    }

    @Serializable
    private data class Password(val password: String)
}