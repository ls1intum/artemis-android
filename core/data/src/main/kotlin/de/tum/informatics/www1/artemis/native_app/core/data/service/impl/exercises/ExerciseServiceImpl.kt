package de.tum.informatics.www1.artemis.native_app.core.data.service.impl.exercises

import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.cookieAuth
import de.tum.informatics.www1.artemis.native_app.core.data.performNetworkCall
import de.tum.informatics.www1.artemis.native_app.core.data.service.ExerciseService
import de.tum.informatics.www1.artemis.native_app.core.data.service.impl.JsonProvider
import de.tum.informatics.www1.artemis.native_app.core.data.service.impl.KtorProvider
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.Exercise
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.datetime.Instant
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString

internal class ExerciseServiceImpl(
    private val ktorProvider: KtorProvider,
    private val jsonProvider: JsonProvider
) : ExerciseService {
    override suspend fun getExerciseDetails(
        exerciseId: Long,
        serverUrl: String,
        authToken: String
    ): NetworkResponse<Exercise> {
        return performNetworkCall {
            ktorProvider.ktorClient.get(serverUrl) {
                url {
                    appendPathSegments("api", "exercises", exerciseId.toString(), "details")
                }

                contentType(ContentType.Application.Json)
                cookieAuth(authToken)
            }.body()
        }
    }

    override suspend fun getLatestDueDate(
        exerciseId: Long,
        serverUrl: String,
        authToken: String
    ): NetworkResponse<Instant?> {
        return performNetworkCall {
            val timeString = ktorProvider.ktorClient.get(serverUrl) {
                url {
                    appendPathSegments(
                        "api",
                        "exercises",
                        exerciseId.toString(),
                        "latest-due-date"
                    )
                }

                contentType(ContentType.Application.Json)
                cookieAuth(authToken)
            }.bodyAsText()

            return@performNetworkCall try {
                jsonProvider.applicationJsonConfiguration.decodeFromString<Instant>(timeString)
            } catch (e: SerializationException) {
                null
            }
        }
    }
}