package de.tum.informatics.www1.artemis.native_app.core.ui.media

import android.view.View
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import de.tum.informatics.www1.artemis.native_app.core.common.artemis_context.*
import de.tum.informatics.www1.artemis.native_app.core.common.test.UnitTest
import de.tum.informatics.www1.artemis.native_app.core.ui.*
import de.tum.informatics.www1.artemis.native_app.core.ui.markdown.link_resolving.MarkdownLinkResolverImpl
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
@Category(UnitTest::class)
class MarkdownResolverLifetimeTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun courseOnlyContextChangeStillDispatchesTheLivePdfPage() {
        val provider =
            object : ArtemisContextProvider {
                override val stateFlow =
                    MutableStateFlow<ArtemisContext>(
                        ArtemisContextImpl.LoggedIn("http://127.0.0.1:1", "test-token", "alice")
                    )

                override fun setCourseId(courseId: Long) {
                    stateFlow.value =
                        ArtemisContextImpl.Course(
                            "http://127.0.0.1:1",
                            "test-token",
                            "alice",
                            courseId,
                        )
                }

                override fun clearCourseId() {}
            }
        var opened: MediaPageRequest? = null
        compose.setContent {
            MaterialTheme {
                CompositionLocalProvider(
                    LocalArtemisContextProvider provides provider,
                    LocalLinkOpener provides
                        object : LinkOpener {
                            override fun openLink(url: String) {}

                            override fun openMediaPage(request: MediaPageRequest): Boolean {
                                opened = request
                                return true
                            }
                        },
                ) {
                    val resolver = MarkdownLinkResolverImpl().rememberMarkdownLinkResolver()
                    val context = LocalContext.current
                    Button(
                        onClick = {
                            resolver.resolve(View(context), "http://127.0.0.1:1/slides.pdf")
                        }
                    ) {
                        Text("Open slides")
                    }
                }
            }
        }
        compose.runOnIdle { provider.setCourseId(2) }
        compose.onNodeWithText("Open slides").performClick()
        compose.runOnIdle {
            org.junit.Assert.assertNotNull(opened)
            org.junit.Assert.assertEquals(MediaPageKind.PDF, opened!!.kind)
            org.junit.Assert.assertEquals(mediaPageScope(provider.stateFlow.value), opened!!.scope)
        }
    }
}
