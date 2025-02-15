package de.tum.informatics.www1.artemis.native_app.feature.dashboard

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToKey
import androidx.test.platform.app.InstrumentationRegistry
import de.tum.informatics.www1.artemis.native_app.core.common.test.DefaultTestTimeoutMillis
import de.tum.informatics.www1.artemis.native_app.core.common.test.EndToEndTest
import de.tum.informatics.www1.artemis.native_app.core.test.BaseComposeTest
import de.tum.informatics.www1.artemis.native_app.core.test.coreTestModules
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.DefaultTimeoutMillis
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.course_creation.createCourse
import de.tum.informatics.www1.artemis.native_app.feature.dashboard.ui.CourseOverviewViewModel
import de.tum.informatics.www1.artemis.native_app.feature.dashboard.ui.CoursesOverview
import de.tum.informatics.www1.artemis.native_app.feature.dashboard.ui.TEST_TAG_COURSE_LIST
import de.tum.informatics.www1.artemis.native_app.feature.dashboard.ui.testTagForCourse
import de.tum.informatics.www1.artemis.native_app.feature.login.loginModule
import de.tum.informatics.www1.artemis.native_app.feature.login.test.getAdminAccessToken
import de.tum.informatics.www1.artemis.native_app.feature.login.test.performTestLogin
import de.tum.informatics.www1.artemis.native_app.feature.login.test.testLoginModule
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.test.KoinTestRule
import org.koin.test.get
import org.robolectric.RobolectricTestRunner

@Ignore("There seems to be a problem related to the docker files, where the mysql database is not " +
        "reset properly. This causes the newly created courses by this E2e test to pile up and " +
        "causes the server to take very long to return all the courses. This results in a timeout." +
        "Issue: https://github.com/ls1intum/artemis-android/issues/169")
@OptIn(ExperimentalTestApi::class)
@Category(EndToEndTest::class)
@RunWith(RobolectricTestRunner::class)
class DashboardE2eTest : BaseComposeTest() {

    @get:Rule
    val koinTestRule = KoinTestRule.create {
        androidContext(InstrumentationRegistry.getInstrumentation().context)

        modules(coreTestModules)
        modules(loginModule, dashboardModule, testLoginModule)
    }

    @Before
    fun setup() {
        runBlockingWithTestTimeout {
            performTestLogin()
        }
    }

    @Test(timeout = DefaultTestTimeoutMillis)
    fun `shows created course in course list`() {
        val createdCourse = runBlockingWithTestTimeout {
            createCourse(getAdminAccessToken())
        }

        val viewModel = CourseOverviewViewModel(
            dashboardService = get(),
            dashboardStorageService = get(),
            serverConfigurationService = get(),
            networkStatusProvider = get(),
            coroutineContext = testDispatcher
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

        val testTag = testTagForCourse(createdCourse.id!!)

        composeTestRule
            .waitUntilExactlyOneExists(
                hasTestTag(TEST_TAG_COURSE_LIST),
                DefaultTimeoutMillis
            )

        composeTestRule
            .onNodeWithTag(TEST_TAG_COURSE_LIST)
            .performScrollToKey(createdCourse.id!!)

        composeTestRule
            .onNodeWithTag(testTag)
            .performScrollTo()
            .assert(hasText(createdCourse.title))
    }
}
