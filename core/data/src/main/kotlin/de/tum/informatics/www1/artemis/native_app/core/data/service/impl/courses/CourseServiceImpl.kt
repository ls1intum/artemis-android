package de.tum.informatics.www1.artemis.native_app.core.data.service.impl.courses

import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.cookieAuth
import de.tum.informatics.www1.artemis.native_app.core.data.performNetworkCall
import de.tum.informatics.www1.artemis.native_app.core.data.retryOnInternet
import de.tum.informatics.www1.artemis.native_app.core.data.service.impl.KtorProvider
import de.tum.informatics.www1.artemis.native_app.core.device.NetworkStatusProvider
import de.tum.informatics.www1.artemis.native_app.core.model.Course
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.flow.Flow

internal class CourseServiceImpl(
    private val ktorProvider: KtorProvider,
    private val networkStatusProvider: NetworkStatusProvider
) :
    de.tum.informatics.www1.artemis.native_app.core.data.service.CourseService {

    override suspend fun getCourse(
        courseId: Long,
        serverUrl: String,
        authToken: String
    ): Flow<DataState<Course>> {
        return retryOnInternet(networkStatusProvider.currentNetworkStatus) {
            performNetworkCall {
                ktorProvider.ktorClient.get(serverUrl) {
                    url {
                        appendPathSegments("api", "courses", courseId.toString(), "for-dashboard")
                    }

                    contentType(ContentType.Application.Json)
                    cookieAuth(authToken)
                }.body()
            }
        }
    }
}