package de.tum.informatics.www1.artemis.native_app.feature.lectureview

import android.net.Uri
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.core.app.ApplicationProvider
import de.tum.informatics.www1.artemis.native_app.core.common.artemis_context.*
import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.model.lecture.*
import de.tum.informatics.www1.artemis.native_app.core.model.lecture.lecture_units.*
import de.tum.informatics.www1.artemis.native_app.core.ui.LocalArtemisContextProvider
import de.tum.informatics.www1.artemis.native_app.feature.lectureview.service.LectureService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class LectureDescriptionCourseTest {
    @get:Rule val compose = createComposeRule()
    @Test fun freshLaunchUsesDisplayedCourseForDescriptionSlide() = follow(false)
    @Test fun staleSelectedCourseCannotRedirectDescriptionSlide() = follow(true)
    private fun follow(stale: Boolean) {
        val session = if (stale) ArtemisContextImpl.Course("https://a.test", "token", "alice", 1)
            else ArtemisContextImpl.LoggedIn("https://a.test", "token", "alice")
        val provider = object : ArtemisContextProvider {
            override val stateFlow = MutableStateFlow<ArtemisContext>(session)
            override fun setCourseId(courseId: Long) {}
            override fun clearCourseId() {}
        }
        var transformed = ""
        compose.setContent {
            CompositionLocalProvider(LocalArtemisContextProvider provides provider) {
                val transformer = rememberLectureArtemisMarkdownTransformer("https://a.test", displayedCourseId = 2)
                transformed = transformer.transformMarkdown("[slide]Slide B(#42)[/slide]")
                Text(transformed)
            }
        }
        compose.runOnIdle { assertTrue(transformed, transformed.contains("artemis://courses/2/lecture-media")) }
        val url = Uri.parse(Regex("\\]\\((artemis:.*?)\\)").find(transformed)!!.groupValues[1])
        var requestedCourse: Long? = null
        val slides = Attachment(id = 4, link = "/attachments/attachment-unit/8/a.pdf", attachmentType = Attachment.AttachmentType.FILE)
        val service = object : LectureService {
            override suspend fun loadLecture(lectureId: Long, serverUrl: String, authToken: String): NetworkResponse<Lecture> = error("Unexpected lecture fetch")
            override suspend fun completeLectureUnit(lectureUnitId: Long, lectureId: Long, completed: Boolean, serverUrl: String, authToken: String): NetworkResponse<Unit> = error("No completion")
            override suspend fun loadCourseLecturesWithSlides(courseId: Long, serverUrl: String, authToken: String): NetworkResponse<List<Lecture>> {
                requestedCourse = courseId
                return NetworkResponse.Response(if (courseId == 2L) listOf(Lecture(id = 7, lectureUnits = listOf(LectureUnitAttachmentVideo(id = 8, attachment = slides, slides = listOf(LectureSlide(42, 3)))))) else emptyList())
            }
        }
        val result = runBlocking { loadLectureReference(service, session, url.pathSegments.first().toLong(), url.getQueryParameter("referenceType")!!, url.getQueryParameter("reference")!!) }
        assertEquals(2L, requestedCourse)
        assertEquals(ResolvedLectureReference(7, 8, page = 3), result.orThrow())
    }
}
