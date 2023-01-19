package de.tum.informatics.www1.artemis.native_app.core.data.service.impl.courses

import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.performNetworkCall
import de.tum.informatics.www1.artemis.native_app.core.data.retryOnInternet
import de.tum.informatics.www1.artemis.native_app.core.data.service.impl.KtorProvider
import de.tum.informatics.www1.artemis.native_app.core.device.NetworkStatusProvider
import de.tum.informatics.www1.artemis.native_app.core.model.Course
import de.tum.informatics.www1.artemis.native_app.core.model.account.Account
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.http.ContentType
import io.ktor.http.appendPathSegments
import io.ktor.http.contentType
import kotlinx.coroutines.flow.Flow

internal class CourseRegistrationServiceImpl(
    private val ktorProvider: KtorProvider,
    private val networkStatusProvider: NetworkStatusProvider
) : de.tum.informatics.www1.artemis.native_app.core.data.service.CourseRegistrationService {

    override suspend fun fetchRegistrableCourses(
        serverUrl: String,
        authToken: String
    ): Flow<DataState<List<Course>>> {
        return retryOnInternet(networkStatusProvider.currentNetworkStatus){
            performNetworkCall {
                ktorProvider.ktorClient
                    .get(serverUrl) {
                        url {
                            appendPathSegments("api", "courses", "for-registration")
                        }

                        bearerAuth(authToken)
                        contentType(ContentType.Application.Json)
                    }
                    .body()
            }
        }
    }

    override suspend fun registerInCourse(
        serverUrl: String,
        authToken: String,
        courseId: Long
    ): NetworkResponse<Unit> {
        return performNetworkCall {
            val user: Account = ktorProvider.ktorClient
                .post(serverUrl) {
                    url {
                        appendPathSegments("api", courseId.toString(), "register")
                    }

                    bearerAuth(authToken)
                }.body()

            // TODO: sync groups
        }
    }
}