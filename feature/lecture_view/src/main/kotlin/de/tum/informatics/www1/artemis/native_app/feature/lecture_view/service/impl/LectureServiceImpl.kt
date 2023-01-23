package de.tum.informatics.www1.artemis.native_app.feature.lecture_view.service.impl

import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.cookieAuth
import de.tum.informatics.www1.artemis.native_app.core.data.performNetworkCall
import de.tum.informatics.www1.artemis.native_app.core.data.service.impl.KtorProvider
import de.tum.informatics.www1.artemis.native_app.core.model.lecture.Lecture
import de.tum.informatics.www1.artemis.native_app.feature.lecture_view.service.LectureService
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class LectureServiceImpl(private val ktorProvider: KtorProvider) : LectureService {
    override suspend fun loadLecture(
        lectureId: Long,
        serverUrl: String,
        authToken: String
    ): NetworkResponse<Lecture> {
        return performNetworkCall {
            ktorProvider.ktorClient.get(serverUrl) {
                url {
                    appendPathSegments("api", "lectures", lectureId.toString(), "details")
                }

                contentType(ContentType.Application.Json)
                cookieAuth(authToken)
            }.body()
        }
    }

    override suspend fun completeLectureUnit(
        lectureUnitId: Long,
        lectureId: Long,
        completed: Boolean,
        serverUrl: String,
        authToken: String
    ): NetworkResponse<Unit> {
        return performNetworkCall {
            ktorProvider.ktorClient.post(serverUrl) {
                url {
                    appendPathSegments(
                        "api",
                        "lectures",
                        lectureId.toString(),
                        "lecture-units",
                        lectureUnitId.toString(),
                        "completion"
                    )

                    parameters.append("completed", completed.toString())
                }

                contentType(ContentType.Application.Json)
                cookieAuth(authToken)
            }
        }
    }
}