package de.tum.informatics.www1.artemis.native_app.android.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.ViewModelStore
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import androidx.navigation.createGraph
import androidx.test.core.app.ApplicationProvider
import de.tum.informatics.www1.artemis.native_app.core.ui.deeplinks.LectureDeeplinks
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowToast

@RunWith(RobolectricTestRunner::class)
@Config(application = NavigationBoundaryTestApplication::class)
class AppLinkOpenerTest {
    @org.junit.After
    fun stopTestServices() {
        org.koin.core.context.stopKoin()
    }

    @Test
    fun nativeMediaLaunchIsPrivateAndDoesNotForwardAuthentication() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val controller = NavHostController(context)
        val request =
            de.tum.informatics.www1.artemis.native_app.core.ui.media.MediaPageRequest(
                "https://a.test/file.pdf?signature=private",
                "Slides",
                "lecture/1/attachment/3",
                "scope",
                de.tum.informatics.www1.artemis.native_app.core.ui.media.MediaPageKind.PDF,
                3,
            )
        assertTrue(AppLinkOpener(context, controller).openMediaPage(request))
        val launched = org.robolectric.Shadows.shadowOf(context as Application).nextStartedActivity
        assertEquals(
            "de.tum.informatics.www1.artemis.native_app.android.ui.LectureMediaActivity",
            launched.component!!.className,
        )
        assertEquals("https://a.test/file.pdf?signature=private", launched.getStringExtra("source"))
        assertFalse(launched.extras.toString().contains("session-token"))
        val info = context.packageManager.getActivityInfo(launched.component!!, 0)
        assertFalse(info.exported)
    }

    @Test
    fun malformedMediaRoutesRecoverThroughTheRealOpener() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val controller = NavHostController(context)
        controller.navigatorProvider.addNavigator(ComposeNavigator())
        controller.setViewModelStore(ViewModelStore())
        controller.graph =
            controller.createGraph(startDestination = "home") { composable("home") {} }
        val opener = AppLinkOpener(context, controller)
        for (url in
            listOf(
                "artemis://courses/not-a-number/lecture-media?referenceType=slide&reference=%231",
                "artemis://courses/1/lecture-media",
                "artemis://courses/1/extra/lecture-media?referenceType=slide&reference=%231",
                "artemis://courses/0/lecture-media?referenceType=slide&reference=%231",
                "artemis://courses:bad-port/1/lecture-media?referenceType=slide&reference=%231",
                "artemis://alice@courses/1/lecture-media?referenceType=slide&reference=%231",
                "artemis://courses/1/lecture-media?referenceType=unknown&reference=%231",
                // Valid shape, but this graph has no media destination: navigation failure must
                // also recover.
                "artemis://courses/1/lecture-media?referenceType=slide&reference=%231",
            )) {
            run {
                val result = runCatching { opener.openLink(url) }
                assertTrue("Malformed/unmatched link must not crash: $url", result.isSuccess)
                assertNotNull(ShadowToast.getTextOfLatestToast())
            }
        }
        controller.graph =
            controller.createGraph(startDestination = "home") {
                composable("home") {}
                composable(
                    "media/{courseId}?referenceType={referenceType}&reference={reference}",
                    deepLinks = LectureDeeplinks.ToLectureMediaReference.generateLinks(),
                ) {}
            }
        opener.openLink("artemis://courses/2/lecture-media?referenceType=slide&reference=%2342")
        assertTrue(controller.currentDestination?.route.orEmpty().startsWith("media/"))
        assertEquals("2", controller.currentBackStackEntry?.arguments?.getString("courseId"))
    }
}

// Robolectric instantiates manifest receivers before Application.onCreate. They are unrelated
// to navigation, but their constructor dependency must exist in this app-level test process.
class NavigationBoundaryTestApplication : Application() {
    init {
        org.koin.core.context.startKoin {
            modules(
                org.koin.dsl.module {
                    single<
                        de.tum.informatics.www1.artemis.native_app.feature.push.service.CommunicationNotificationManager
                    > {
                        object :
                            de.tum.informatics.www1.artemis.native_app.feature.push.service.CommunicationNotificationManager {
                            override suspend fun popNotification(
                                artemisNotification:
                                    de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.ArtemisNotification<
                                        de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.CommunicationNotificationType
                                    >
                            ) = Unit

                            override suspend fun addSelfMessage(
                                parentId: Long,
                                authorLoginName: String,
                                authorName: String,
                                authorImageUrl: String?,
                                body: String,
                                date: kotlin.time.Instant,
                            ) = Unit

                            override suspend fun repopNotification(parentId: Long) = Unit

                            override suspend fun deleteCommunication(parentId: Long) = Unit
                        }
                    }
                }
            )
        }
    }
}
