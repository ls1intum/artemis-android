package de.tum.informatics.www1.artemis.native_app.feature.dashboard

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import de.tum.informatics.www1.artemis.native_app.core.common.test.EndToEndTest
import de.tum.informatics.www1.artemis.native_app.core.test.coreTestModules
import de.tum.informatics.www1.artemis.native_app.feature.login.test.performTestLogin
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.koin.androidx.compose.getViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.core.context.startKoin
import org.koin.test.KoinTest
import org.robolectric.RobolectricTestRunner

@Category(EndToEndTest::class)
@RunWith(RobolectricTestRunner::class)
class DashboardE2eTest : KoinTest {

    private val composeTestRule = createComposeRule()

    @Before
    fun setup() {
        startKoin {
            modules(coreTestModules)
            modules(dashboardModule)
        }

        runTest {
            performTestLogin()
        }
    }

    @Test
    fun `test bla`() {
        composeTestRule.setContent {
            CoursesOverview(
                modifier = Modifier.fillMaxSize(),
                viewModel = getViewModel(),
                onOpenSettings = { },
                onClickRegisterForCourse = { },
                onViewCourse = {}
            )
        }

//        composeTestRule.onNode()
    }
}