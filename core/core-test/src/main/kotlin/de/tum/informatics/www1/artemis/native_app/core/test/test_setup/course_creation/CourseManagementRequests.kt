package de.tum.informatics.www1.artemis.native_app.core.test.test_setup.course_creation

import android.util.Log
import de.tum.informatics.www1.artemis.native_app.core.data.cookieAuth
import de.tum.informatics.www1.artemis.native_app.core.data.service.Api
import de.tum.informatics.www1.artemis.native_app.core.data.service.KtorProvider
import de.tum.informatics.www1.artemis.native_app.core.data.service.impl.JsonProvider
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.model.Course
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.Exercise
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.UnknownExercise
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.QuizExercise
import de.tum.informatics.www1.artemis.native_app.core.model.lecture.Attachment
import de.tum.informatics.www1.artemis.native_app.core.model.lecture.Lecture
import de.tum.informatics.www1.artemis.native_app.core.model.lecture.lecture_units.LectureUnit
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.generateId
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.generateShortName
import kotlinx.coroutines.delay
import io.ktor.client.call.body
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.request.accept
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.appendPathSegments
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.flow.first
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

private const val TAG = "CourseCreationService"

val KoinComponent.jsonProvider: JsonProvider get() = get()
val KoinComponent.ktorProvider: KtorProvider get() = get()
val KoinComponent.serverConfigurationService: ServerConfigurationService get() = get()

suspend fun KoinComponent.createCourse(
    accessToken: String,
    courseName: String = "Course ${generateId()}",
    courseShortName: String = generateShortName(),
    forceSelfRegistration: Boolean = false
): Course {
    Log.i(TAG, "Creating new course with name $courseName and shortName $courseShortName")

    val courseJsonString = if (forceSelfRegistration) {
        createCourseWithSelfRegistration(
            title = courseName,
            shortName = courseShortName
        )
    } else {
        createCourseTemplate(title = courseName, shortName = courseShortName)
    }

    val course: Course = ktorProvider.ktorClient.submitFormWithBinaryData(
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
            appendPathSegments(*Api.Admin.path, "courses")
        }

        cookieAuth(accessToken)

        contentType(ContentType.MultiPart.FormData)
        accept(ContentType.Application.Json)
    }
        .body()

    // Every field of the course model has a default and unknown keys are ignored, so an error body
    // decodes into a course rather than failing: without this check a rejected creation surfaces
    // much later as a course with no id.
    check(course.id != null && course.id != 0L) {
        "Creating the course did not answer with one: $course"
    }

    return course
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
        .body<Exercise>()
        .also(::checkCreated)
}

/**
 * An error body decodes into an [UnknownExercise] rather than failing, because the exercise
 * hierarchy falls back to it for a type it does not recognise. Without this a rejected creation
 * surfaces later as a cast failure in whichever screen the test opens.
 */
private fun checkCreated(exercise: Exercise) {
    check(exercise !is UnknownExercise && exercise.id != null) {
        "Creating the exercise did not answer with one: $exercise"
    }
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
    }.body<Exercise>().also(::checkCreated)
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
    batch: QuizExercise.QuizBatch
) {
    val batchId = requireNotNull(batch.id) { "Cannot start a quiz batch without an id" }

    return ktorProvider.ktorClient.put(serverConfigurationService.serverUrl.first()) {
        url {
            appendPathSegments(*Api.Quiz.QuizBatches.path, batchId.toString(), "start-batch")
        }

        setBody(batch)

        cookieAuth(accessToken)

        contentType(ContentType.Application.Json)
    }.body()
}
/**
 * Adds the given user to the course as a student.
 *
 * Artemis 10 decides course membership from per-course roles rather than from the group names a
 * course used to carry, so creating a course and a user no longer relates the two: without this the
 * user is not a member and every endpoint that requires membership in this course answers 403.
 */
suspend fun KoinComponent.addStudentToCourse(
    accessToken: String,
    courseId: Long,
    studentLogin: String
) = addUserToCourse(accessToken, courseId, studentLogin, "students")

/**
 * Sets the code of conduct of an existing course.
 *
 * The creation endpoint does not accept one: the field is absent from its payload and silently
 * dropped, and a course without a code of conduct reports it as already accepted, so the tests that
 * exercise accepting it have nothing to accept.
 */
suspend fun KoinComponent.setCodeOfConduct(
    accessToken: String,
    course: Course,
    codeOfConduct: String
) {
    val response = ktorProvider.ktorClient.submitFormWithBinaryData(
        formData {
            append(
                "course",
                updateCourseCodeOfConductTemplate(course, codeOfConduct),
                Headers.build {
                    set("Content-Type", "application/json")
                    set("name", "course")
                    set("filename", "blob")
                })
        }
    ) {
        url(serverConfigurationService.serverUrl.first())
        url {
            appendPathSegments(*Api.Course.Courses.path, course.id.toString())
        }
        method = HttpMethod.Put

        cookieAuth(accessToken)

        contentType(ContentType.MultiPart.FormData)
        accept(ContentType.Application.Json)
    }

    check(response.status.isSuccess()) {
        "Could not set the code of conduct of course ${course.id}: ${response.status}"
    }
}

/**
 * Adds the given user to the course as an instructor, which is what the server means by a user
 * responsible for the code of conduct.
 */
suspend fun KoinComponent.addInstructorToCourse(
    accessToken: String,
    courseId: Long,
    instructorLogin: String
) = addUserToCourse(accessToken, courseId, instructorLogin, "instructors")

/**
 * Number of attempts for [addUserToCourse]. Two is enough, because the collision can only happen
 * while the authority row is still missing: once any attempt has written it, no further attempt
 * inserts anything.
 */
private const val ADD_USER_ATTEMPTS = 2

private const val ADD_USER_RETRY_DELAY = 500L

/**
 * Grants [userLogin] a role in a course, retrying once if the server answers 5xx.
 *
 * The suite runs its modules in parallel and each of them grants the same few test users a staff
 * role in its own freshly created course. The server stores the course role first and then rebuilds
 * that user's *global* authorities, and Hibernate inserts only the authority rows the user does not
 * have yet -- so two grants collide only while `ROLE_INSTRUCTOR` is still missing for that user,
 * which on a wiped database is the first grant of a run. Both insert `(user, ROLE_INSTRUCTOR)`, one
 * wins, and the loser answers 500 although the course role it was asked for is already stored.
 *
 * Retrying therefore settles it rather than papering over anything: the second attempt finds the
 * role in place and returns early, and the authority was written by whichever attempt won. Verified
 * against a local server -- eight simultaneous first grants for one user answer 1x200 and 7x500
 * without this, and 8x200 with it, with the role stored in all eight courses either way.
 *
 * Only a test suite provokes this. Granting the same person their first staff role in several
 * courses within the same instant is not something the web client does.
 */
private suspend fun KoinComponent.addUserToCourse(
    accessToken: String,
    courseId: Long,
    userLogin: String,
    roleSegment: String
) {
    repeat(ADD_USER_ATTEMPTS) { attempt ->
        val failure = try {
            val response = ktorProvider.ktorClient.post(serverConfigurationService.serverUrl.first()) {
                url {
                    appendPathSegments(
                        *Api.Course.Courses.path,
                        courseId.toString(),
                        roleSegment,
                        userLogin
                    )
                }

                cookieAuth(accessToken)

                contentType(ContentType.Application.Json)
            }

            check(response.status.isSuccess()) {
                "Could not add $userLogin to course $courseId as $roleSegment: ${response.status}"
            }
            return
        } catch (e: ServerResponseException) {
            e
        }

        if (attempt == ADD_USER_ATTEMPTS - 1) {
            throw failure
        }
        Log.w("CourseManagementRequests", "Retrying to add $userLogin to course $courseId as $roleSegment", failure)
        delay(ADD_USER_RETRY_DELAY)
    }
}
