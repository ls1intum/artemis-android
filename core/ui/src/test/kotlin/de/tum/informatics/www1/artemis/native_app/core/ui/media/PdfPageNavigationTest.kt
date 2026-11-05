package de.tum.informatics.www1.artemis.native_app.core.ui.media

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import de.tum.informatics.www1.artemis.native_app.core.common.test.UnitTest
import de.tum.informatics.www1.artemis.native_app.core.ui.compose.PdfPage
import de.tum.informatics.www1.artemis.native_app.core.ui.remote_resources.pdf.PdfFile
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "w1280dp-h800dp-mdpi")
@Category(UnitTest::class)
class PdfPageNavigationTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun tabletPdfOwnsTheWholePageAndHasStandardBackWithoutAModalPane() {
        var backs = 0
        val file =
            PdfFile(
                "http://127.0.0.1:1/slides.pdf",
                "",
                "slides.pdf",
                "http://127.0.0.1:1",
                "page-test",
            )
        compose.setContent {
            MaterialTheme {
                PdfPage(Modifier.fillMaxSize(), file, "Lecture slides", null, 3, { backs++ })
            }
        }
        compose
            .onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsProperties.PaneTitle))
            .assertCountEquals(0)
        val page = compose.onNodeWithTag("media_pdf_page").fetchSemanticsNode().boundsInRoot
        val root = compose.onRoot().fetchSemanticsNode().boundsInRoot
        assertEquals(root.width, page.width, 1f)
        assertEquals(root.height, page.height, 1f)
        compose.onNodeWithContentDescription("Back").performClick()
        compose.runOnIdle { assertEquals(1, backs) }
    }
}
