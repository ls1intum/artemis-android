package de.tum.informatics.www1.artemis.native_app.feature.courseregistration

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.hasParent
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToKey
import androidx.test.platform.app.InstrumentationRegistry
import de.tum.informatics.www1.artemis.native_app.core.common.test.DefaultTestTimeoutMillis
import de.tum.informatics.www1.artemis.native_app.core.common.test.EndToEndTest
import de.tum.informatics.www1.artemis.native_app.core.model.Course
import de.tum.informatics.www1.artemis.native_app.core.test.BaseComposeTest
import de.tum.informatics.www1.artemis.native_app.core.test.coreTestModules
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.DefaultTimeoutMillis
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.course_creation.createCourse
import de.tum.informatics.www1.artemis.native_app.feature.login.loginModule
import de.tum.informatics.www1.artemis.native_app.feature.login.test.getAdminAccessToken
import de.tum.informatics.www1.artemis.native_app.feature.login.test.performTestLogin
import de.tum.informatics.www1.artemis.native_app.feature.login.test.testLoginModule
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.test.KoinTestRule
import org.koin.test.get
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertEquals

@Category(EndToEndTest::class)
@RunWith(RobolectricTestRunner::class)
class CourseRegistrationE2eTest : BaseComposeTest() {

    @get:Rule
    val koinTestRule = KoinTestRule.create {
        androidContext(InstrumentationRegistry.getInstrumentation().context)

        modules(coreTestModules)
        modules(loginModule, testLoginModule, courseRegistrationModule)
    }

    private lateinit var course: Course

    @Before
    fun setup() {
        runBlockingWithTestTimeout {
            performTestLogin()
            course = createCourse(getAdminAccessToken(), forceSelfRegistration = true)
        }
    }

    /**
     * Tests that the registrable course is displayed and registering to it is successful.
     */
    @Test(timeout = DefaultTestTimeoutMillis)
    fun `can successfully register in course`() {
        val viewModel = RegisterForCourseViewModel(get(), get(), get(), get(), UnconfinedTestDispatcher())

        var registeredCourseId: Long? = null

        composeTestRule.setContent {
            RegisterForCourseScreen(
                modifier = Modifier.fillMaxSize(),
                onNavigateUp = { },
                onRegisteredInCourse = { registeredCourseId = it },
                viewModel = viewModel
            )
        }

        composeTestRule
            .onNodeWithTag(TEST_TAG_REGISTRABLE_COURSE_LIST)
            .performScrollToKey(course.id!!)

        composeTestRule.onNodeWithText(course.title)
            .assertExists("Could not find registrable course in list")

        composeTestRule
            .onNode(
                hasParent(
                    hasTestTag(testTagForRegistrableCourse(course.id!!))
                ) and hasText(
                    context.getString(
                        R.string.course_registration_sign_up
                    )
                )
            )
            .performClick()

        composeTestRule
            .onNodeWithText(context.getString(R.string.course_registration_sign_up_dialog_positive_button))
            .performClick()

        // Wait until registered. Fails if not registered in time.
        composeTestRule.waitUntil(DefaultTimeoutMillis) { registeredCourseId != null }

        assertEquals(course.id!!, registeredCourseId)
    }
}