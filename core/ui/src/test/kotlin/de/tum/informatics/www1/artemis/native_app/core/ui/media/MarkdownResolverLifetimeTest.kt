package de.tum.informatics.www1.artemis.native_app.core.ui.media

import android.content.Context
import android.view.View
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import de.tum.informatics.www1.artemis.native_app.core.common.artemis_context.*
import de.tum.informatics.www1.artemis.native_app.core.ui.*
import de.tum.informatics.www1.artemis.native_app.core.ui.R
import de.tum.informatics.www1.artemis.native_app.core.ui.markdown.link_resolving.MarkdownLinkResolverImpl
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class MarkdownResolverLifetimeTest {
    @get:Rule val compose = createComposeRule()
    @Test fun courseOnlyContextChangeStillOpensTheLivePdfSheet() {
        val provider = object : ArtemisContextProvider {
            override val stateFlow = MutableStateFlow<ArtemisContext>(ArtemisContextImpl.LoggedIn("http://127.0.0.1:1", "test-token", "alice"))
            override fun setCourseId(courseId: Long) { stateFlow.value = ArtemisContextImpl.Course("http://127.0.0.1:1", "test-token", "alice", courseId) }
            override fun clearCourseId() {}
        }
        compose.setContent {
            MaterialTheme {
                CompositionLocalProvider(LocalArtemisContextProvider provides provider) {
                    val resolver = MarkdownLinkResolverImpl().rememberMarkdownLinkResolver()
                    val context = LocalContext.current
                    Button(onClick = { resolver.resolve(View(context), "http://127.0.0.1:1/slides.pdf") }) { Text("Open slides") }
                }
            }
        }
        compose.runOnIdle { provider.setCourseId(2) }
        compose.onNodeWithText("Open slides").performClick()
        val error = ApplicationProvider.getApplicationContext<Context>().getString(R.string.pdf_view_error_loading)
        compose.waitUntil(8_000) { compose.onAllNodes(androidx.compose.ui.test.hasText(error)).fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText(error).assertExists()
    }
}
