package de.tum.informatics.www1.artemis.native_app.feature.lectureview.service.impl

import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.cookieAuth
import de.tum.informatics.www1.artemis.native_app.core.data.performNetworkCall
import de.tum.informatics.www1.artemis.native_app.core.data.service.Api
import de.tum.informatics.www1.artemis.native_app.core.data.service.KtorProvider
import de.tum.informatics.www1.artemis.native_app.core.model.lecture.Lecture
import de.tum.informatics.www1.artemis.native_app.core.model.lecture.Attachment
import de.tum.informatics.www1.artemis.native_app.core.model.lecture.lecture_units.LectureUnitAttachmentVideo
import de.tum.informatics.www1.artemis.native_app.core.model.lecture.lecture_units.LectureSlide
import kotlinx.serialization.Serializable
import de.tum.informatics.www1.artemis.native_app.feature.lectureview.service.LectureService
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.http.ContentType
import io.ktor.http.appendPathSegments
import io.ktor.http.contentType

class LectureServiceImpl(private val ktorProvider: KtorProvider) : LectureService {
    // Metadata is always fetched from the configured server. A redirect must be retried explicitly,
    // rather than forwarding its Artemis cookie to another origin.
    private val lectureClient = ktorProvider.ktorClient.config { followRedirects = false }
    override suspend fun loadLecture(
        lectureId: Long,
        serverUrl: String,
        authToken: String
    ): NetworkResponse<Lecture> {
        return performNetworkCall {
            lectureClient.get(serverUrl) {
                url {
                    appendPathSegments(*Api.Lecture.Lectures.path, lectureId.toString(), "details")
                }

                contentType(ContentType.Application.Json)
                cookieAuth(authToken)
            }.body()
        }
    }

    override suspend fun loadCourseLecturesWithSlides(courseId: Long, serverUrl: String, authToken: String): NetworkResponse<List<Lecture>> {
        return performNetworkCall {
            lectureClient.get(serverUrl) {
                url { appendPathSegments(*Api.Lecture.path, "courses", courseId.toString(), "lectures-with-slides") }
                contentType(ContentType.Application.Json)
                cookieAuth(authToken)
            }.body<List<LectureSlideMetadata>>().map { it.toLecture() }
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
                        *Api.Lecture.Lectures.path,
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
// This endpoint deliberately sends a projection, not full lecture/attachment details.
@Serializable
private data class LectureSlideMetadata(val id: Long, val lectureUnits: List<UnitSlideMetadata> = emptyList()) {
    fun toLecture() = Lecture(id = id, lectureUnits = lectureUnits.filter { it.type == "attachment" }.map { unit ->
        LectureUnitAttachmentVideo(id = unit.id, slides = unit.slides, attachment = unit.attachment?.let {
            Attachment(id = it.id, link = it.link, name = it.name, attachmentType = Attachment.AttachmentType.FILE)
        })
    })
}
@Serializable
private data class UnitSlideMetadata(val id: Long, val type: String, val slides: List<LectureSlide> = emptyList(), val attachment: SlideAttachmentMetadata? = null)
@Serializable
private data class SlideAttachmentMetadata(val id: Int, val link: String? = null, val name: String? = null)
