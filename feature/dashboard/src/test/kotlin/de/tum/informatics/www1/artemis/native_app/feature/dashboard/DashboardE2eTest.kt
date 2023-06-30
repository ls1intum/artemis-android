package de.tum.informatics.www1.artemis.native_app.feature.dashboard

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performScrollToIndex
import androidx.test.platform.app.InstrumentationRegistry
import de.tum.informatics.www1.artemis.native_app.core.common.test.EndToEndTest
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.filterSuccess
import de.tum.informatics.www1.artemis.native_app.core.model.Dashboard
import de.tum.informatics.www1.artemis.native_app.core.test.coreTestModules
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.course_creation.createCourse
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.setTestServerUrl
import de.tum.informatics.www1.artemis.native_app.feature.login.loginModule
import de.tum.informatics.www1.artemis.native_app.feature.login.test.getAdminAccessToken
import de.tum.informatics.www1.artemis.native_app.feature.login.test.performTestLogin
import de.tum.informatics.www1.artemis.native_app.feature.login.test.testLoginModule
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.test.KoinTest
import org.koin.test.get
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertIs

@Category(EndToEndTest::class)
@RunWith(RobolectricTestRunner::class)
class DashboardE2eTest : KoinTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun setup() {
        startKoin {
            androidContext(InstrumentationRegistry.getInstrumentation().context)

            modules(coreTestModules)
            modules(loginModule, dashboardModule, testLoginModule)
        }

        runTest {
            setTestServerUrl()
            performTestLogin()
        }
    }

    @Test
    fun `shows created course in course list`() {
        val createdCourse = runBlocking(UnconfinedTestDispatcher()) {
            createCourse(getAdminAccessToken())
        }
        val viewModel = CourseOverviewViewModel(
            get(),
            get(),
            get(),
            get(),
            UnconfinedTestDispatcher()
        )

        composeTestRule.setContent {
            CoursesOverview(
                modifier = Modifier.fillMaxSize(),
                viewModel = viewModel,
                onOpenSettings = { },
                onClickRegisterForCourse = { },
                onViewCourse = { }
            )
        }

        runBlocking(UnconfinedTestDispatcher()) {
            viewModel.dashboard.filterSuccess().first()
        }

        val dashboard: DataState.Success<Dashboard> = assertIs(viewModel.dashboard.value)
        assert(dashboard.data.courses.any { it.course.id == createdCourse.id }) { "Could not find created course in the dashboard " }

        val indexOfCreatedCourse =
            dashboard.data.courses.indexOfFirst { it.course.id == createdCourse.id }
        assert(indexOfCreatedCourse != -1) { "Could not find index of created course in the dashboard list" }

        // Scroll to item so that it is visible.
        composeTestRule.onNodeWithTag(CourseListTestTag).performScrollToIndex(indexOfCreatedCourse)

        composeTestRule.onNodeWithTag("CourseId${createdCourse.id}").assertExists()
    }
}