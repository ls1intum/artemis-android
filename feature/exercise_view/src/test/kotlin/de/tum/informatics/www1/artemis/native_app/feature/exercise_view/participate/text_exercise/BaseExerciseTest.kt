package de.tum.informatics.www1.artemis.native_app.feature.exercise_view.participate.text_exercise

import android.content.Context
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import de.tum.informatics.www1.artemis.native_app.core.model.Course
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.TextExercise
import de.tum.informatics.www1.artemis.native_app.core.test.coreTestModules
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.DefaultTimeoutMillis
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.course_creation.createCourse
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.course_creation.createExercise
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.course_creation.createTextExercise
import de.tum.informatics.www1.artemis.native_app.core.test.testWebsocketModule
import de.tum.informatics.www1.artemis.native_app.feature.exercise_view.exerciseModule
import de.tum.informatics.www1.artemis.native_app.feature.login.loginModule
import de.tum.informatics.www1.artemis.native_app.feature.login.test.getAdminAccessToken
import de.tum.informatics.www1.artemis.native_app.feature.login.test.performTestLogin
import de.tum.informatics.www1.artemis.native_app.feature.login.test.testLoginModule
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.withTimeout
import org.junit.Before
import org.junit.Rule
import org.koin.android.ext.koin.androidContext
import org.koin.test.KoinTest
import org.koin.test.KoinTestRule
import org.robolectric.shadows.ShadowLog

abstract class BaseExerciseTest : KoinTest {

    protected val testDispatcher = UnconfinedTestDispatcher()

    protected val context: Context get() = InstrumentationRegistry.getInstrumentation().context

    @get:Rule
    val composeTestRole = createComposeRule()

    @get:Rule
    val koinTestRule = KoinTestRule.create {
        androidContext(InstrumentationRegistry.getInstrumentation().context)

        modules(coreTestModules)
        modules(loginModule, exerciseModule, testLoginModule, testWebsocketModule(testDispatcher))
    }

    protected lateinit var accessToken: String

    protected lateinit var course: Course
    protected lateinit var exercise: TextExercise

    @Before
    open fun setup() {
        ShadowLog.stream = System.out

        runBlocking {
            withTimeout(DefaultTimeoutMillis) {
                accessToken = performTestLogin()

                course = createCourse(getAdminAccessToken())
                exercise = createExercise(
                    getAdminAccessToken(),
                    course.id!!,
                    endpoint = "text-exercises",
                    creator = ::createTextExercise
                ) as TextExercise
            }
        }
    }
}