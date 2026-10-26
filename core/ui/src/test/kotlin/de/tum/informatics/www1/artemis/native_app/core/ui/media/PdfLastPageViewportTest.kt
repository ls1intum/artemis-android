package de.tum.informatics.www1.artemis.native_app.core.ui.media

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import com.sun.net.httpserver.HttpServer
import de.tum.informatics.www1.artemis.native_app.core.common.test.UnitTest
import de.tum.informatics.www1.artemis.native_app.core.ui.remote_resources.pdf.PdfFile
import de.tum.informatics.www1.artemis.native_app.core.ui.remote_resources.pdf.render.VerticalPdfView
import de.tum.informatics.www1.artemis.native_app.core.ui.remote_resources.pdf.render.state.VerticalPdfReaderState
import java.net.InetSocketAddress
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[34],qualifiers="w600dp-h1200dp-mdpi",shadows=[FixturePdfRendererShadow::class,FixturePdfPageShadow::class])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Category(UnitTest::class)
class PdfLastPageViewportTest {
    @get:Rule val compose=createComposeRule()
    @Test(timeout=60000) fun exactLastPageCanAlignAtTopEvenWhenTheWholeShortDeckFits() {
        val bytes=fixturePdf(3)
        val server=HttpServer.create(InetSocketAddress("127.0.0.1",0),0)
        server.createContext("/"){ e -> e.sendResponseHeaders(200,bytes.size.toLong());e.responseBody.use{it.write(bytes)} };server.start()
        val origin="http://127.0.0.1:${server.address.port}"
        val state=VerticalPdfReaderState(PdfFile("$origin/slides.pdf","","slides.pdf",origin,"last-page"))
        try {
            compose.setContent {
                Box(Modifier.size(200.dp,1000.dp)) { VerticalPdfView(state,Modifier.size(200.dp,1000.dp)) }
                LaunchedEffect(state.pdfPageCount) { if(state.pdfPageCount>0)state.lazyState.scrollToItem(2) }
            }
            compose.waitUntil(10000){state.pdfPageCount==3}
            compose.runOnIdle {
                assertEquals("The requested last slide is the first visible page",3,state.currentPage)
                assertEquals(2,state.lazyState.firstVisibleItemIndex)
                assertEquals(0,state.lazyState.firstVisibleItemScrollOffset)
            }
        } finally { server.stop(0) }
    }
}
