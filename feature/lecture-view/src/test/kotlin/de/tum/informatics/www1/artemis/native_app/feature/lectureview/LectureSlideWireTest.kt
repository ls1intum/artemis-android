package de.tum.informatics.www1.artemis.native_app.feature.lectureview

import com.sun.net.httpserver.HttpServer
import de.tum.informatics.www1.artemis.native_app.core.common.artemis_context.ArtemisContextImpl
import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.service.KtorProvider
import de.tum.informatics.www1.artemis.native_app.core.data.service.impl.JsonProvider
import de.tum.informatics.www1.artemis.native_app.core.model.lecture.*
import de.tum.informatics.www1.artemis.native_app.core.model.lecture.lecture_units.*
import de.tum.informatics.www1.artemis.native_app.feature.lectureview.service.LectureService
import de.tum.informatics.www1.artemis.native_app.feature.lectureview.service.impl.LectureServiceImpl
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import java.net.InetSocketAddress
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

@org.junit.runner.RunWith(org.robolectric.RobolectricTestRunner::class)
@org.junit.experimental.categories.Category(de.tum.informatics.www1.artemis.native_app.core.common.test.UnitTest::class)
class LectureSlideWireTest {
    private val file = Attachment(id=3, name="Slides", link="attachments/unit/8/original.pdf", studentVersion="attachments/unit/8/student/a.pdf", version=1, attachmentType=Attachment.AttachmentType.FILE)
    private val visible = LectureUnitAttachmentVideo(id=8, attachment=file.copy(studentVersion=null), slides=listOf(LectureSlide(12,1),LectureSlide(14,3),LectureSlide(19,8)))
    private val session = ArtemisContextImpl.LoggedIn("https://a.test", "token", "alice")

    @Test fun actualSlimMetadataWireDoesNotRequireDetailOnlyAttachmentFields() = runBlocking {
        val server=HttpServer.create(InetSocketAddress("127.0.0.1",0),0)
        server.createContext("/") { exchange ->
            val bytes="""[{"id":1,"lectureUnits":[{"id":8,"type":"attachment","slides":[{"id":19,"slideNumber":8}],"attachment":{"id":3,"name":"Slides","link":"attachments/unit/8/original.pdf"}}]}]""".toByteArray()
            exchange.responseHeaders.add("Content-Type","application/json");exchange.sendResponseHeaders(200,bytes.size.toLong());exchange.responseBody.use{it.write(bytes)}
        };server.start()
        val client=HttpClient { install(ContentNegotiation) { json(JsonProvider().applicationJsonConfiguration) } }
        try {
            val result=LectureServiceImpl(object:KtorProvider{override val ktorClient=client}).loadCourseLecturesWithSlides(182,"http://127.0.0.1:${server.address.port}","token")
            val lecture=result.orThrow().single()
            assertEquals(19L,(lecture.lectureUnits.single() as LectureUnitAttachmentVideo).slides.single().id)
        } finally { client.close();server.stop(0) }
    }

    @Test fun visibleMetadataIsHydratedWithTheActualStudentDocumentForBothReferenceForms() = runBlocking {
        for (reference in listOf("#19","attachment-unit/8/slide/8")) {
            val service=FixtureService()
            val result=loadLectureReference(service,session,182,"slide",reference).orThrow()
            assertEquals(3,result.page)
            assertEquals(listOf("metadata","details","metadata","details"),service.calls)
        }
    }
    @Test fun originalDocumentKeepsOriginalPageAndChangedDocumentRequiresRetry() = runBlocking {
        val original=FixtureService(student=false)
        assertEquals(8,loadLectureReference(original,session,182,"slide","#19").orThrow().page)
        assertTrue(loadLectureReference(FixtureService(changed=true),session,182,"slide","#19") is NetworkResponse.Failure)
    }
    @Test fun modernIdentityIsPreservedWhenVisibleMetadataChangesDuringLookup() = runBlocking {
        assertTrue(loadLectureReference(FixtureService(hide=true),session,182,"slide","#19") is NetworkResponse.Failure)
    }
    private inner class FixtureService(val changed:Boolean=false,val hide:Boolean=false,val student:Boolean=true):LectureService {
        val calls=mutableListOf<String>();var details=0;var metadata=0
        override suspend fun loadCourseLecturesWithSlides(courseId:Long,serverUrl:String,authToken:String):NetworkResponse<List<Lecture>> {
            calls+="metadata";metadata++
            return NetworkResponse.Response(listOf(Lecture(id=1,lectureUnits=listOf(if(hide&&metadata>1) visible.copy(slides=visible.slides.filter{it.id!=19L}) else visible))))
        }
        override suspend fun loadLecture(lectureId:Long,serverUrl:String,authToken:String):NetworkResponse<Lecture> {
            calls+="details";details++
            val actual=file.copy(studentVersion=if(student) file.studentVersion else null,version=if(changed&&details>1)2 else 1)
            return NetworkResponse.Response(Lecture(id=1,lectureUnits=listOf(visible.copy(attachment=actual,slides=emptyList()))))
        }
        override suspend fun completeLectureUnit(lectureUnitId:Long,lectureId:Long,completed:Boolean,serverUrl:String,authToken:String):NetworkResponse<Unit> = error("No completion")
    }
}
