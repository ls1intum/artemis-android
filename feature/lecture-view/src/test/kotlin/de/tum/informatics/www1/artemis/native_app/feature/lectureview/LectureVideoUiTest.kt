package de.tum.informatics.www1.artemis.native_app.feature.lectureview

import android.content.Context
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import de.tum.informatics.www1.artemis.native_app.core.ui.LinkOpener
import de.tum.informatics.www1.artemis.native_app.core.ui.LocalLinkOpener
import de.tum.informatics.www1.artemis.native_app.core.model.lecture.lecture_units.LectureUnitAttachmentVideo
import de.tum.informatics.www1.artemis.native_app.feature.lectureview.lecture_units.LectureUnitVideoUi
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import de.tum.informatics.www1.artemis.native_app.core.ui.R as CoreR

@RunWith(RobolectricTestRunner::class)
@org.junit.experimental.categories.Category(de.tum.informatics.www1.artemis.native_app.core.common.test.UnitTest::class)
class LectureVideoUiTest {
    @get:Rule val compose = createComposeRule()
    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Test fun providerButtonUsesIsolatedLecturePageOpener() {
        var provider: String? = null
        var external: String? = null
        val opener = object : LinkOpener {
            override fun openLink(url: String) { external = url }
            override fun openLectureVideoPage(url: String) { provider = url }
        }
        compose.setContent {
            MaterialTheme {
                CompositionLocalProvider(LocalLinkOpener provides opener) {
                    LectureUnitVideoUi(Modifier, LectureUnitAttachmentVideo(id = 1, videoSource = "https://provider.test/watch?v=1"), {}, {})
                }
            }
        }
        compose.onNodeWithText(context.getString(R.string.lecture_view_open_video_link_button)).performClick()
        compose.runOnIdle {
            assertEquals("https://provider.test/watch?v=1", provider)
            assertNull(external)
        }
    }

    @Test fun invalidSourceShowsAccessibleRecoveryWithoutOpeningBrowser() {
        var external = false
        compose.setContent {
            MaterialTheme {
                CompositionLocalProvider(LocalLinkOpener provides object : LinkOpener {
                    override fun openLink(url: String) { external = true }
                }) {
                    LectureUnitVideoUi(Modifier, LectureUnitAttachmentVideo(id = 2, videoSource = "javascript:alert(1)"), {}, {})
                }
            }
        }
        compose.onNodeWithText(context.getString(R.string.lecture_view_open_video_link_button)).performClick()
        compose.onNodeWithText(context.getString(CoreR.string.lecture_media_invalid)).assertIsDisplayed()
        compose.onNodeWithText(context.getString(CoreR.string.lecture_media_close)).performClick()
        compose.runOnIdle { assertFalse(external) }
    }
}
