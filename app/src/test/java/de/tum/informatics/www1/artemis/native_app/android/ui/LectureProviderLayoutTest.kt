package de.tum.informatics.www1.artemis.native_app.android.ui

import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import org.junit.Assert.assertEquals
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application=NavigationBoundaryTestApplication::class)
class LectureProviderLayoutTest {
    @get:Rule val compose=createEmptyComposeRule()
    @After fun stopServices(){ org.koin.core.context.stopKoin() }
    @Test(timeout=60000) fun providerHasABoundedLayoutViewportForCssVideoSizing() {
        val controller=org.robolectric.Robolectric.buildActivity(ComponentActivity::class.java).setup()
        val activity=controller.get()
        activity.setContent { ProviderPage("https://provider.test/watch",Modifier.fillMaxSize()) }
        compose.runOnIdle {
            fun web(view:View):WebView? = if(view is WebView)view else if(view is ViewGroup)(0 until view.childCount).firstNotNullOfOrNull{web(view.getChildAt(it))} else null
            val page=requireNotNull(web(activity.window.decorView))
            assertEquals(ViewGroup.LayoutParams.MATCH_PARENT,page.layoutParams.height)
            assertEquals(ViewGroup.LayoutParams.MATCH_PARENT,page.layoutParams.width)
        }
        controller.pause().stop().destroy()
    }
}
