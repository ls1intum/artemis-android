package de.tum.informatics.www1.artemis.native_app.feature.exerciseview

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import de.tum.informatics.www1.artemis.native_app.core.common.test.UnitTest
import de.tum.informatics.www1.artemis.native_app.core.test.BaseComposeTest
import de.tum.informatics.www1.artemis.native_app.core.ui.LocalWindowSizeClassProvider
import de.tum.informatics.www1.artemis.native_app.core.ui.WindowSizeClassProvider
import de.tum.informatics.www1.artemis.native_app.core.ui.common.tablet.LayoutAwareTwoColumnLayout
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
@Category(UnitTest::class)
class ExercisePaneNavigationTest : BaseComposeTest() {
    @Test fun `open assessment and selected attempt survive adaptive pane relocation and activity restoration`() {
        var portrait by mutableStateOf(false)
        var exerciseId by mutableStateOf(12L)
        val restoration = StateRestorationTester(composeTestRule)
        restoration.setContent {
            val configuration = Configuration(LocalConfiguration.current).apply {
                orientation = if (portrait) Configuration.ORIENTATION_PORTRAIT else Configuration.ORIENTATION_LANDSCAPE
            }
            CompositionLocalProvider(
                LocalConfiguration provides configuration,
                LocalWindowSizeClassProvider provides object : WindowSizeClassProvider {
                    @Composable override fun provideWindowSizeClass() = WindowSizeClass.calculateFromSize(DpSize(1000.dp, 900.dp))
                },
            ) {
                val detailPane = rememberExerciseDetailPane(exerciseId) {
                    val navigation = rememberNavController()
                    NavHost(navigation, startDestination = "overview") {
                        composable("overview") { TextButton(onClick = { navigation.navigate("result") }) { Text("View result") } }
                        composable("result") {
                            var attempt by androidx.compose.runtime.saveable.rememberSaveable { mutableIntStateOf(3) }
                            Column {
                                Text("Assessment $exerciseId attempt $attempt")
                                TextButton(onClick = { attempt = 2 }) { Text("Earlier assessment") }
                            }
                        }
                    }
                }
                LayoutAwareTwoColumnLayout(isSidebarOpen = false, onSidebarToggle = {}, title = "Course", optionalColumn = {}, priorityColumn = detailPane)
            }
        }
        composeTestRule.onNodeWithText("View result").performClick()
        composeTestRule.onNodeWithText("Earlier assessment").performClick()
        composeTestRule.runOnIdle { portrait = true }
        composeTestRule.onNodeWithText("Assessment 12 attempt 2").assertExists()
        restoration.emulateSavedInstanceStateRestore()
        composeTestRule.onNodeWithText("Assessment 12 attempt 2").assertExists()
        composeTestRule.runOnIdle { portrait = false }
        composeTestRule.onNodeWithText("Assessment 12 attempt 2").assertExists()
        composeTestRule.runOnIdle { exerciseId = 13 }
        composeTestRule.onNodeWithText("View result").assertExists()
        composeTestRule.onNodeWithText("Assessment 12 attempt 2").assertDoesNotExist()
    }
}
