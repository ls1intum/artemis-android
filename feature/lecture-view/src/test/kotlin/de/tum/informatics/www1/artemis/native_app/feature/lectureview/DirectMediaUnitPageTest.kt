package de.tum.informatics.www1.artemis.native_app.feature.lectureview

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.ViewModelStore
import androidx.navigation.NavHostController
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.composable
import androidx.navigation.createGraph
import de.tum.informatics.www1.artemis.native_app.core.common.artemis_context.*
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.model.lecture.*
import de.tum.informatics.www1.artemis.native_app.core.model.lecture.lecture_units.*
import de.tum.informatics.www1.artemis.native_app.core.ui.*
import de.tum.informatics.www1.artemis.native_app.core.ui.media.*
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DirectMediaUnitPageTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    private val video =
        LectureUnitAttachmentVideo(
            id = 3,
            name = "Recording",
            description = "Lecture details",
            videoSource = "https://a.test/recording.MP4",
        )
    private val pdf =
        LectureUnitAttachmentVideo(
            id = 8,
            name = "Slides",
            attachment =
                Attachment(
                    id = 4,
                    name = "Slides",
                    link = "attachments/unit/8/slides.pdf",
                    attachmentType = Attachment.AttachmentType.FILE,
                ),
        )
    private val requests = mutableListOf<MediaPageRequest>()
    private val provider =
        object : ArtemisContextProvider {
            override val stateFlow =
                MutableStateFlow<ArtemisContext>(
                    ArtemisContextImpl.LoggedIn("https://a.test", "token", "alice")
                )

            override fun setCourseId(courseId: Long) {}

            override fun clearCourseId() {}
        }

    @Composable
    private fun screen(
        lecture: DataState<Lecture>,
        units: List<LectureUnit>,
        initial: Long? = null,
        opened: () -> Unit = {},
    ) {
        MaterialTheme {
            CompositionLocalProvider(
                LocalArtemisContextProvider provides provider,
                LocalLinkOpener provides
                    object : LinkOpener {
                        override fun openLink(url: String) {}

                        override fun openMediaPage(request: MediaPageRequest): Boolean {
                            requests += request
                            return true
                        }
                    },
            ) {
                LectureScreen(
                    lectureDataState = lecture,
                    modifier = Modifier,
                    serverUrl = "https://a.test",
                    lectureChannel = DataState.Failure(Exception()),
                    lectureUnits = units,
                    onViewExercise = {},
                    onReloadLecture = {},
                    onUpdateLectureUnitIsComplete = { _, _ -> CompletableDeferred(true) },
                    onSidebarToggle = {},
                    initialUnitId = initial,
                    onMediaPageOpened = opened,
                )
            }
        }
    }

    @Test
    fun mediaRowsOpenTheirPagesDirectlyWithoutAUnitSheet() {
        compose.setContent {
            screen(
                DataState.Success(
                    Lecture(id = 7, title = "Lecture", lectureUnits = listOf(video, pdf))
                ),
                listOf(video, pdf),
            )
        }
        compose.onNodeWithTag(getLectureUnitTestTag(3)).performClick()
        compose.runOnIdle {
            assertEquals(1, requests.size)
            assertEquals(MediaPageKind.VIDEO, requests.single().kind)
        }
        compose
            .onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsProperties.PaneTitle))
            .assertCountEquals(0)
        compose.onNodeWithTag(getLectureUnitTestTag(8)).performClick()
        compose.runOnIdle {
            assertEquals(listOf(MediaPageKind.VIDEO, MediaPageKind.PDF), requests.map { it.kind })
        }
    }

    @Test
    fun asynchronouslyArrivingVideoReferenceReturnsToThreadAndNeverRelaunchesOnBack() {
        val nav = NavHostController(compose.activity)
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
        var loaded by mutableStateOf(false)
        var completed by mutableStateOf(false)
        compose.setContent {
            val units = if (loaded) listOf(video.copy(completed = completed)) else emptyList()
            screen(
                if (loaded)
                    DataState.Success(Lecture(id = 7, title = "Lecture", lectureUnits = units))
                else DataState.Loading(),
                units,
                3,
            ) {
                nav.completeLectureMediaReference(lookup)
            }
        }
        compose.runOnIdle { loaded = true }
        compose.runOnIdle {
            assertEquals("Async reference must launch directly", 1, requests.size)
            assertEquals(
                "One Back must return to the same thread",
                thread,
                nav.currentBackStackEntry!!.id,
            )
            completed = true
        }
        compose.runOnIdle { assertEquals(1, requests.size) }
    }
}
