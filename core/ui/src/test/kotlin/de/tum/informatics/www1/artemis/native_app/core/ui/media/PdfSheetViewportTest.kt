package de.tum.informatics.www1.artemis.native_app.core.ui.media

import android.content.Context
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.core.app.ApplicationProvider
import de.tum.informatics.www1.artemis.native_app.core.ui.R
import de.tum.informatics.www1.artemis.native_app.core.ui.compose.LinkBottomSheet
import de.tum.informatics.www1.artemis.native_app.core.ui.compose.LinkBottomSheetState
import de.tum.informatics.www1.artemis.native_app.core.ui.remote_resources.pdf.PdfFile
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class PdfSheetViewportTest {
    @get:Rule val compose = createComposeRule()
    @Test fun pdfStartsExpandedRatherThanHidingItsStudyViewportBelowTheFold() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val file = PdfFile("http://127.0.0.1:1/slides.pdf", "", "slides.pdf", "http://127.0.0.1:1", "sheet-test")
        compose.setContent { MaterialTheme { LinkBottomSheet(Modifier.fillMaxSize(), LinkBottomSheetState.PDFVIEWSTATE(file), {}) } }
        val error = context.getString(R.string.pdf_view_error_loading)
        compose.waitUntil(10_000) { compose.onAllNodes(androidx.compose.ui.test.hasText(error)).fetchSemanticsNodes().isNotEmpty() }
        val sheet = compose.onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsProperties.PaneTitle)).fetchSemanticsNodes().maxBy { it.size.height }
        assertTrue("PDF opens with its viewport expanded", sheet.boundsInRoot.top < sheet.size.height / 3f)
    }
}
