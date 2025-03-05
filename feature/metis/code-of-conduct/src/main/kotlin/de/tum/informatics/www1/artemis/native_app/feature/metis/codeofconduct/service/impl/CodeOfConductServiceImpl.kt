package de.tum.informatics.www1.artemis.native_app.feature.metis.codeofconduct.service.impl

import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.cookieAuth
import de.tum.informatics.www1.artemis.native_app.core.data.performNetworkCall
import de.tum.informatics.www1.artemis.native_app.core.data.service.Api
import de.tum.informatics.www1.artemis.native_app.core.data.service.KtorProvider
import de.tum.informatics.www1.artemis.native_app.core.model.account.User
import de.tum.informatics.www1.artemis.native_app.feature.metis.codeofconduct.service.CodeOfConductService
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.http.ContentType
import io.ktor.http.appendPathSegments
import io.ktor.http.contentType
import io.ktor.http.isSuccess

class CodeOfConductServiceImpl(
    private val ktorProvider: KtorProvider
) : CodeOfConductService {

    override suspend fun getCodeOfConductTemplate(
        courseId: Long,
        serverUrl: String,
        authToken: String
    ): NetworkResponse<String> {
        return performNetworkCall {
            ktorProvider.ktorClient.get(serverUrl) {
                url {
                    appendPathSegments(*Api.Core.path, "files", "templates", "code-of-conduct")
                }

                cookieAuth(authToken)
                contentType(ContentType.Text.Html)
            }
                .body()
        }
    }

    override suspend fun getIsCodeOfConductAccepted(
        courseId: Long,
        serverUrl: String,
        authToken: String
    ): NetworkResponse<Boolean> {
        return performNetworkCall {
            ktorProvider.ktorClient.get(serverUrl) {
                url {
                    appendPathSegments(
                        *Api.Communication.Courses.path,
                        courseId.toString(),
                        "code-of-conduct/agreement"
                    )
                }

                cookieAuth(authToken)
                contentType(ContentType.Application.Json)
            }.body()
        }
    }

    override suspend fun acceptCodeOfConduct(
        courseId: Long,
        serverUrl: String,
        authToken: String
    ): NetworkResponse<Unit> {
        return performNetworkCall {
            val response = ktorProvider.ktorClient.patch(serverUrl) {
                url {
                    appendPathSegments(
                        *Api.Communication.Courses.path,
                        courseId.toString(),
                        "code-of-conduct/agreement"
                    )
                }

                cookieAuth(authToken)
                contentType(ContentType.Application.Json)
            }

            if (!response.status.isSuccess()) {
                throw RuntimeException("Response ${response.status} is not success")
            }
        }
    }

    override suspend fun getResponsibleUsers(
        courseId: Long,
        serverUrl: String,
        authToken: String
    ): NetworkResponse<List<User>> {
        return performNetworkCall {
            ktorProvider.ktorClient.get(serverUrl) {
                url {
                    appendPathSegments(
                        *Api.Communication.Courses.path,
                        courseId.toString(),
                        "code-of-conduct/responsible-users"
                    )
                }

                cookieAuth(authToken)
                contentType(ContentType.Application.Json)
            }
                .body()
        }
    }
}