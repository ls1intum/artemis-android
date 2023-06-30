package de.tum.informatics.www1.artemis.native_app.core.test.test_setup.course_creation

import android.util.Log
import de.tum.informatics.www1.artemis.native_app.core.data.cookieAuth
import de.tum.informatics.www1.artemis.native_app.core.data.service.KtorProvider
import de.tum.informatics.www1.artemis.native_app.core.data.service.impl.JsonProvider
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.model.Course
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.generateId
import io.ktor.client.call.body
import io.ktor.client.request.accept
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.appendPathSegments
import io.ktor.http.contentType
import kotlinx.coroutines.flow.first
import kotlinx.serialization.encodeToString
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

private const val DEFAULT = "default"
private val studentGroupName: String get() = System.getenv("studentGroupName") ?: DEFAULT
private val teachingAssistantGroupName: String get() = System.getenv("tutorGroupName") ?: DEFAULT
private val editorGroupName: String get() = System.getenv("editorGroupName") ?: DEFAULT
private val instructorGroupName: String get() = System.getenv("instructorGroupName") ?: DEFAULT

private const val TAG = "CourseCreationService"

suspend fun KoinComponent.createCourse(
    accessToken: String,
    courseName: String = "Course ${generateId()}",
    courseShortName: String = "ae2e${generateId()}"
): Course {
    Log.i(
        TAG, """
        Creating new course with name $courseName and shortName $courseShortName
        Using studentGroupName=$studentGroupName, teachingAssistantGroupName=$teachingAssistantGroupName, editorGroupName=$editorGroupName, instructorGroupName=$instructorGroupName
        """.trimIndent()
    )

    val course = Course(
        id = null,
        title = courseName,
        shortName = courseShortName,
        testCourse = true,
        courseInformationSharingConfiguration = Course.CourseInformationSharingConfiguration.MESSAGING_ONLY,
        studentGroupName = studentGroupName,
        teachingAssistantGroupName = teachingAssistantGroupName,
        editorGroupName = editorGroupName,
        instructorGroupName = instructorGroupName
    )

    val jsonProvider: JsonProvider = get()
    val ktorProvider: KtorProvider = get()
    val serverConfigurationService: ServerConfigurationService = get()

    return ktorProvider.ktorClient.submitFormWithBinaryData(
        formData {
            append("course", jsonProvider.applicationJsonConfiguration.encodeToString(course), Headers.build {
                set("Content-Type", "application/json")
                set("name", "course")
                set("filename", "blob")
            })
        }
    ) {
        url(serverConfigurationService.serverUrl.first())
        url {
            appendPathSegments("api", "admin", "courses")
        }

        cookieAuth(accessToken)

        contentType(ContentType.MultiPart.FormData)
        accept(ContentType.Application.Json)
    }
        .body()
}
