package de.tum.informatics.www1.artemis.native_app.feature.metis.shared.service.network.impl

import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.cookieAuth
import de.tum.informatics.www1.artemis.native_app.core.data.performNetworkCall
import de.tum.informatics.www1.artemis.native_app.core.data.service.KtorProvider
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.BasePost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.service.model.SavedPostStatus
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.service.network.SavedPostService
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.appendPathSegments

class SavedPostServiceImpl(
    private val ktorProvider: KtorProvider
) : SavedPostService {

    override suspend fun getSavedPosts(
        status: SavedPostStatus,
        courseId: Long,
        authToken: String,
        serverUrl: String
    ): NetworkResponse<List<BasePost>> {
        return performNetworkCall {
            ktorProvider.ktorClient.get(serverUrl) {
                url {
                    appendPathSegments("api", "saved-posts", courseId.toString(), status.ordinal.toString())
                }

                cookieAuth(authToken)
            }.body()
        }
    }


}