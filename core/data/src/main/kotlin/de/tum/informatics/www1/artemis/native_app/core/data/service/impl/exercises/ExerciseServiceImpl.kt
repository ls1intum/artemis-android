package de.tum.informatics.www1.artemis.native_app.core.data.service.impl.exercises

import de.tum.informatics.www1.artemis.native_app.core.model.exercise.Exercise
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.performNetworkCall
import de.tum.informatics.www1.artemis.native_app.core.data.retryOnInternet
import de.tum.informatics.www1.artemis.native_app.core.data.service.impl.KtorProvider
import de.tum.informatics.www1.artemis.native_app.core.device.NetworkStatusProvider
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.flow.Flow

internal class ExerciseServiceImpl(
    private val ktorProvider: KtorProvider,
    private val networkStatusProvider: NetworkStatusProvider
) :
    de.tum.informatics.www1.artemis.native_app.core.data.service.ExerciseService {
    override suspend fun getExerciseDetails(
        exerciseId: Int,
        serverUrl: String,
        authToken: String
    ): Flow<DataState<Exercise>> {
        return retryOnInternet(networkStatusProvider.currentNetworkStatus) {
            val x: NetworkResponse<Exercise> = performNetworkCall {
                ktorProvider.ktorClient.get(serverUrl) {
                    url {
                        appendPathSegments("api", "exercises", exerciseId.toString(), "details")
                    }

                    contentType(ContentType.Application.Json)
                    bearerAuth(authToken)
                }.body()
            }
            x
        }
    }
}