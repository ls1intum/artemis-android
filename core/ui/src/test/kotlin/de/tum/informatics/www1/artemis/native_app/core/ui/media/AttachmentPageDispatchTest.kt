package de.tum.informatics.www1.artemis.native_app.core.ui.media

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import de.tum.informatics.www1.artemis.native_app.core.common.artemis_context.*
import de.tum.informatics.www1.artemis.native_app.core.ui.*
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AttachmentPageDispatchTest {
    @get:Rule val compose=createComposeRule()
    @Test fun pdfDispatchesANewPageAndClearsItsOriginOnlyAfterSuccessfulLaunch() {
        var request:MediaPageRequest?=null
        var dismissed=0
        val session=ArtemisContextImpl.Course("https://artemis.test","session-token","alice",182)
        val provider=object:ArtemisContextProvider {
            override val stateFlow=MutableStateFlow<ArtemisContext>(session)
            override fun setCourseId(courseId:Long){}
            override fun clearCourseId(){}
        }
        compose.setContent {
            MaterialTheme { CompositionLocalProvider(LocalArtemisContextProvider provides provider, LocalLinkOpener provides object:LinkOpener {
                override fun openLink(url:String){}
                override fun openMediaPage(value:MediaPageRequest):Boolean {request=value;return true}
            }) { AttachmentHandler("https://artemis.test/slides.pdf?signature=private",{dismissed++},3,"lecture/1/attachment/3") } }
        }
        compose.runOnIdle {
            assertNotNull("A PDF must launch a page, not mount a sheet",request)
            assertEquals(MediaPageKind.PDF,request!!.kind)
            assertEquals(3,request!!.initialPage)
            assertEquals(1,dismissed)
        }
    }
    @Test fun failedPageLaunchKeepsOriginAndAnEnabledRetry() {
        var accept=false
        var dismissed=0
        var opened=0
        compose.setContent { MaterialTheme { CompositionLocalProvider(LocalLinkOpener provides object:LinkOpener {
            override fun openLink(url:String){}
            override fun openMediaPage(request:MediaPageRequest):Boolean {opened++;return accept}
        }) { AttachmentHandler("https://a.test/slides.pdf",{dismissed++}) } } }
        compose.runOnIdle { assertEquals(0,dismissed) }
        val retry=androidx.test.core.app.ApplicationProvider.getApplicationContext<android.content.Context>().getString(de.tum.informatics.www1.artemis.native_app.core.ui.R.string.lecture_media_retry)
        compose.onNodeWithText(retry).assertExists()
        compose.runOnIdle { accept=true }
        compose.onNodeWithText(retry).performClick()
        compose.runOnIdle { assertEquals(2,opened);assertEquals(1,dismissed) }
    }

}
