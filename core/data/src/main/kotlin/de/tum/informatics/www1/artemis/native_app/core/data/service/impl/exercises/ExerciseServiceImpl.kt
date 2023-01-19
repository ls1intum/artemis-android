package de.tum.informatics.www1.artemis.native_app.core.data.service.impl.exercises

import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.performNetworkCall
import de.tum.informatics.www1.artemis.native_app.core.data.retryOnInternet
import de.tum.informatics.www1.artemis.native_app.core.data.service.impl.JsonProvider
import de.tum.informatics.www1.artemis.native_app.core.data.service.impl.KtorProvider
import de.tum.informatics.www1.artemis.native_app.core.device.NetworkStatusProvider
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.Exercise
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.appendPathSegments
import io.ktor.http.contentType
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString

internal class ExerciseServiceImpl(
    private val ktorProvider: KtorProvider,
    private val networkStatusProvider: NetworkStatusProvider,
    private val jsonProvider: JsonProvider
) :
    de.tum.informatics.www1.artemis.native_app.core.data.service.ExerciseService {
    override fun getExerciseDetails(
        exerciseId: Long,
        serverUrl: String,
        authToken: String
    ): Flow<DataState<Exercise>> {
        return retryOnInternet(networkStatusProvider.currentNetworkStatus) {
            performNetworkCall {
                ktorProvider.ktorClient.get(serverUrl) {
                    url {
                        appendPathSegments("api", "exercises", exerciseId.toString(), "details")
                    }

                    contentType(ContentType.Application.Json)
                    bearerAuth(authToken)
                }.body()
            }
        }
    }

    override fun getLatestDueDate(
        exerciseId: Long,
        serverUrl: String,
        authToken: String
    ): Flow<DataState<Instant?>> {
        return retryOnInternet(networkStatusProvider.currentNetworkStatus) {
            performNetworkCall {
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
                    bearerAuth(authToken)
                }.bodyAsText()

                return@performNetworkCall try {
                    jsonProvider.networkJsonConfiguration.decodeFromString<Instant>(timeString)
                } catch (e: SerializationException) {
                    null
                }
            }
        }
    }
}