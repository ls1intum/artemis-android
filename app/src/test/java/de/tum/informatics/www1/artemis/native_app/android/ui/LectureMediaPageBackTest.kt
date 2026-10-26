package de.tum.informatics.www1.artemis.native_app.android.ui

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.lifecycle.ViewModelStore
import androidx.navigation.NavHostController
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.composable
import androidx.navigation.createGraph
import de.tum.informatics.www1.artemis.native_app.core.common.artemis_context.*
import de.tum.informatics.www1.artemis.native_app.core.ui.*
import de.tum.informatics.www1.artemis.native_app.core.ui.media.*
import de.tum.informatics.www1.artemis.native_app.feature.lectureview.completeLectureMediaReference
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.After
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = NavigationBoundaryTestApplication::class)
class LectureMediaPageBackTest {
    @get:Rule val compose = createEmptyComposeRule()
    private val session = ArtemisContextImpl.LoggedIn("https://a.test", "session-token", "alice")
    private val provider =
        object : ArtemisContextProvider {
            override val stateFlow = MutableStateFlow<ArtemisContext>(session)

            override fun setCourseId(courseId: Long) {}

            override fun clearCourseId() {}
        }

    @After
    fun stopServices() {
        org.koin.core.context.stopKoin()
    }

    @Test
    fun launchedPdfBackReturnsToTheSameThreadAndLateCompletionCannotPopIt() {
        loadKoinModules(module { single<ArtemisContextProvider> { provider } })
        val origin = Robolectric.buildActivity(ComponentActivity::class.java).setup()
        val activity = origin.get()
        val nav = NavHostController(activity)
        nav.navigatorProvider.addNavigator(ComposeNavigator())
        nav.setViewModelStore(ViewModelStore())
        nav.graph =
            nav.createGraph(startDestination = "thread") {
                composable("thread") {}
                composable("lookup") {}
            }
        val thread = nav.currentBackStackEntry!!.id
        nav.navigate("lookup")
        val lookup = nav.currentBackStackEntry!!.id
        activity.setContent {
            MaterialTheme {
                CompositionLocalProvider(
                    LocalArtemisContextProvider provides provider,
                    LocalLinkOpener provides AppLinkOpener(activity, nav),
                ) {
                    AttachmentHandler(
                        "https://a.test/slides.pdf",
                        {},
                        3,
                        onPageOpened = { nav.completeLectureMediaReference(lookup) },
                    )
                }
            }
        }
        compose.runOnIdle { assertEquals(thread, nav.currentBackStackEntry!!.id) }
        val intent = Shadows.shadowOf(activity).nextStartedActivity
        assertEquals(LectureMediaActivity::class.java.name, intent.component!!.className)
        val media = Robolectric.buildActivity(LectureMediaActivity::class.java, intent).setup()
        compose.runOnIdle { media.get().onBackPressedDispatcher.onBackPressed() }
        assertTrue(media.get().isFinishing)
        assertEquals(thread, nav.currentBackStackEntry!!.id)
        assertFalse(nav.completeLectureMediaReference(lookup))
        assertEquals(thread, nav.currentBackStackEntry!!.id)
        media.pause().stop().destroy()
        origin.pause().stop().destroy()
    }

    @Test
    fun killedProcessSourceRequiresFreshOriginLookupInsteadOfReusingAnOldMappedPage() {
        loadKoinModules(module { single<ArtemisContextProvider> { provider } })
        val context =
            androidx.test.core.app.ApplicationProvider.getApplicationContext<
                android.content.Context
            >()
        val request =
            MediaPageRequest(
                "https://a.test/slides.pdf?signature=old",
                "Slides",
                "lecture/1/attachment/3",
                mediaPageScope(session),
                MediaPageKind.PDF,
                3,
            )
        val intent = LectureMediaActivity.intent(context, request).putExtra("epoch", "dead-process")
        val controller =
            Robolectric.buildActivity(LectureMediaActivity::class.java, intent)
                .create(android.os.Bundle())
                .start()
                .resume()
                .visible()
        val message =
            context.getString(
                de.tum.informatics.www1.artemis.native_app.core.ui.R.string
                    .lecture_media_reopen_source
            )
        compose.onNodeWithText(message).assertExists()
        compose.onNodeWithTag("media_pdf_page").assertDoesNotExist()
        compose.onNodeWithContentDescription("Back").performClick()
        assertTrue(controller.get().isFinishing)
        controller.pause().stop().destroy()
    }
}
