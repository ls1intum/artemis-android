package de.tum.informatics.www1.artemis.native_app.core.test.test_setup.course_creation

import android.util.Log
import de.tum.informatics.www1.artemis.native_app.core.data.cookieAuth
import de.tum.informatics.www1.artemis.native_app.core.data.service.Api
import de.tum.informatics.www1.artemis.native_app.core.data.service.KtorProvider
import de.tum.informatics.www1.artemis.native_app.core.data.service.impl.JsonProvider
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.model.Course
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.Exercise
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.QuizExercise
import de.tum.informatics.www1.artemis.native_app.core.model.lecture.Attachment
import de.tum.informatics.www1.artemis.native_app.core.model.lecture.Lecture
import de.tum.informatics.www1.artemis.native_app.core.model.lecture.lecture_units.LectureUnit
import de.tum.informatics.www1.artemis.native_app.core.model.lecture.lecture_units.LectureUnitAttachment
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.generateId
import io.ktor.client.call.body
import io.ktor.client.request.accept
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.appendPathSegments
import io.ktor.http.contentType
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

private const val DEFAULT = "default"
private val studentGroupName: String get() = System.getenv("studentGroupName") ?: DEFAULT
private val teachingAssistantGroupName: String get() = System.getenv("tutorGroupName") ?: DEFAULT
private val editorGroupName: String get() = System.getenv("editorGroupName") ?: DEFAULT
private val instructorGroupName: String get() = System.getenv("instructorGroupName") ?: DEFAULT

private const val TAG = "CourseCreationService"

val KoinComponent.jsonProvider: JsonProvider get() = get()
val KoinComponent.ktorProvider: KtorProvider get() = get()
val KoinComponent.serverConfigurationService: ServerConfigurationService get() = get()

suspend fun KoinComponent.createCourse(
    accessToken: String,
    courseName: String = "Course ${generateId()}",
    courseShortName: String = "ae2e${generateId()}",
    forceSelfRegistration: Boolean = false
): Course {
    Log.i(
        TAG, """
        Creating new course with name $courseName and shortName $courseShortName
        Using studentGroupName=$studentGroupName, teachingAssistantGroupName=$teachingAssistantGroupName, editorGroupName=$editorGroupName, instructorGroupName=$instructorGroupName
        """.trimIndent()
    )

    val courseJsonString = if (forceSelfRegistration) {
        createCourseWithSelfRegistration(
            title = courseName,
            shortName = courseShortName
        )
    } else {
        val course = Course(
            id = null,
            title = courseName,
            shortName = courseShortName,
            testCourse = true,
            courseInformationSharingConfiguration = Course.CourseInformationSharingConfiguration.COMMUNICATION_AND_MESSAGING,
            studentGroupName = studentGroupName,
            teachingAssistantGroupName = teachingAssistantGroupName,
            editorGroupName = editorGroupName,
            instructorGroupName = instructorGroupName,
            courseInformationSharingMessagingCodeOfConduct = "Code of conduct…"
        )
        jsonProvider.applicationJsonConfiguration.encodeToString(course)
    }

    return ktorProvider.ktorClient.submitFormWithBinaryData(
        formData {
            append(
                "course",
                courseJsonString,
                Headers.build {
                    set("Content-Type", "application/json")
                    set("name", "course")
                    set("filename", "blob")
                })
        }
    ) {
        url(serverConfigurationService.serverUrl.first())
        url {
            appendPathSegments(*Api.Core.path, "admin", "courses")
        }

        cookieAuth(accessToken)

        contentType(ContentType.MultiPart.FormData)
        accept(ContentType.Application.Json)
    }
        .body()
}

suspend fun KoinComponent.createExercise(
    accessToken: String,
    courseId: Long,
    exerciseName: String = "Exercise ${generateId()}",
    pathSegments: Array<out String>,
    creator: (String, Long) -> String
): Exercise {
    return ktorProvider.ktorClient.post(serverConfigurationService.serverUrl.first()) {
        url {
            appendPathSegments(*pathSegments)
        }

        cookieAuth(accessToken)

        setBody(creator(exerciseName, courseId))

        contentType(ContentType.Application.Json)
        accept(ContentType.Application.Json)
    }
        .body()
}

suspend fun KoinComponent.createExerciseFormBodyWithPng(
    accessToken: String,
    courseId: Long,
    exerciseName: String = "Exercise ${generateId()}",
    pngByteArray: ByteArray,
    pngFilePath: String,
    pathSegments: Array<out String>,
    creator: (String, Long) -> String
): Exercise {
    return ktorProvider.ktorClient.submitFormWithBinaryData(
        formData {
            append(
                "exercise",
                creator(exerciseName, courseId),
                Headers.build {
                    set("Content-Type", "application/json")
                    set("filename", "blob")
                }
            )
            append(
                "files",
                pngByteArray,
                headers = Headers.build {
                    set(HttpHeaders.ContentType, ContentType.Image.PNG.contentType)
                    set(HttpHeaders.ContentDisposition, "filename=\"$pngFilePath\"")
                }
            )
        }
    ) {
        url(serverConfigurationService.serverUrl.first())
        url {
            appendPathSegments(*pathSegments)
        }

        cookieAuth(accessToken)

        contentType(ContentType.MultiPart.FormData)
        accept(ContentType.Application.Json)
    }.body()
}

suspend fun KoinComponent.createLecture(
    accessToken: String,
    courseId: Long,
    lectureName: String = "Lecture ${generateId()}",
): Lecture {
    return ktorProvider.ktorClient.post(serverConfigurationService.serverUrl.first()) {
        url {
            appendPathSegments(*Api.Lecture.Lectures.path)
        }

        setBody(
            Lecture(
                id = null,
                title = lectureName,
                course = Course(id = courseId),
                description = "some description"
            )
        )

        cookieAuth(accessToken)
        contentType(ContentType.Application.Json)
        accept(ContentType.Application.Json)
    }.body()
}

suspend fun KoinComponent.createLectureUnit(
    accessToken: String,
    lectureId: Long,
    endpoint: String,
    creator: (String) -> String,
    lectureUnitName: String = "Lecture Unit ${generateId()}"
): LectureUnit {
    return ktorProvider.ktorClient.post(serverConfigurationService.serverUrl.first()) {
        url {
            appendPathSegments(*Api.Lecture.Lectures.path, lectureId.toString(), endpoint)
        }

        setBody(
            creator(lectureUnitName)
        )

        cookieAuth(accessToken)
        contentType(ContentType.Application.Json)
        accept(ContentType.Application.Json)
    }.body()
}

suspend fun KoinComponent.createAttachmentUnit(
    accessToken: String,
    lectureId: Long,
    lectureUnitName: String = "Attachment lecture unit ${generateId()}"
): LectureUnitAttachment {
    return ktorProvider.ktorClient.submitFormWithBinaryData(
        formData {
            append(
                "file",
                "file content".encodeToByteArray(),
                Headers.build {
                    append(HttpHeaders.ContentDisposition, "filename=file.txt")
                }
            )

            append(
                "attachment",
                """
                    {"name":"$lectureUnitName","releaseDate":null,"version":1,"attachmentType":"FILE"}
                """.trimIndent(),
                Headers.build {
                    set("Content-Type", "application/json")
                    set("filename", "blob")
                }
            )

            append(
                "attachmentUnit",
                """
                  {"competencies":[],"type":"attachment","description":"Description ${generateId()}"}  
                """.trimIndent(),
                Headers.build {
                    set("Content-Type", "application/json")
                    set("filename", "blob")
                }
            )
        }
    ) {
        url(serverConfigurationService.serverUrl.first())

        url {
            appendPathSegments(*Api.Lecture.Lectures.path, lectureId.toString(), "attachment-units")
        }

        parameter("keepFilename", true)

        cookieAuth(accessToken)

        contentType(ContentType.MultiPart.FormData)
        accept(ContentType.Application.Json)
    }
        .body()
}

suspend fun KoinComponent.createAttachment(
    accessToken: String,
    lectureId: Long,
    attachmentName: String = "Attachment${generateId()}"
): Attachment {
    return ktorProvider.ktorClient.submitFormWithBinaryData(
        formData {
            append(
                "file",
                "file content".encodeToByteArray(),
                Headers.build {
                    append(HttpHeaders.ContentDisposition, "filename=file.txt")
                }
            )

            append(
                "attachment",
                """
                    {
                      "name": "$attachmentName",
                      "link": "$attachmentName.txt",
                      "version": 1,
                      "attachmentType": "FILE",
                      "lecture": {
                        "id": $lectureId
                      }
                    }
                """.trimIndent(),
                Headers.build {
                    set("Content-Type", "application/json")
                    set("filename", "blob")
                }
            )
        }
    ) {
        url(serverConfigurationService.serverUrl.first())

        url {
            appendPathSegments(*Api.Lecture.path, "attachments")
        }

        cookieAuth(accessToken)

        contentType(ContentType.MultiPart.FormData)
        accept(ContentType.Application.Json)
    }
        .body()
}

suspend fun KoinComponent.addQuizExerciseBatch(
    accessToken: String,
    exerciseId: Long
): QuizExercise.QuizBatch {
    return ktorProvider.ktorClient.put(serverConfigurationService.serverUrl.first()) {
        url {
            appendPathSegments(*Api.Quiz.QuizExercises.path, exerciseId.toString(), "add-batch")
        }

        cookieAuth(accessToken)

        contentType(ContentType.Application.Json)
    }.body()
}

suspend fun KoinComponent.startQuizExerciseBatch(
    accessToken: String,
    exerciseId: Long,
    batch: QuizExercise.QuizBatch
) {
    return ktorProvider.ktorClient.put(serverConfigurationService.serverUrl.first()) {
        url {
            appendPathSegments(*Api.Quiz.QuizExercises.path, exerciseId.toString(), "start-batch")
        }

        setBody(batch)

        cookieAuth(accessToken)

        contentType(ContentType.Application.Json)
    }.body()
}

@Serializable
private data class FileUploadResponse(val path: String)