package de.tum.informatics.www1.artemis.native_app.feature.coursenotifications

import androidx.test.platform.app.InstrumentationRegistry
import de.tum.informatics.www1.artemis.native_app.core.common.test.UnitTest
import de.tum.informatics.www1.artemis.native_app.core.model.Course
import de.tum.informatics.www1.artemis.native_app.core.test.BaseComposeTest
import de.tum.informatics.www1.artemis.native_app.core.test.coreTestModules
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.course_creation.createCourse
import de.tum.informatics.www1.artemis.native_app.feature.login.loginModule
import de.tum.informatics.www1.artemis.native_app.feature.login.test.getAdminAccessToken
import de.tum.informatics.www1.artemis.native_app.feature.login.test.performTestLogin
import de.tum.informatics.www1.artemis.native_app.feature.login.test.testLoginModule
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.test.KoinTestRule
import org.robolectric.RobolectricTestRunner
import org.robolectric.shadows.ShadowLog

@RunWith(RobolectricTestRunner::class)
@Category(UnitTest::class)
abstract class BaseCourseNotificationTest : BaseComposeTest() {

    @get:Rule
    val koinTestRule = KoinTestRule.create {
        androidContext(InstrumentationRegistry.getInstrumentation().context)

        modules(coreTestModules)
        modules(loginModule, courseNotificationsModule, testLoginModule)
    }

    protected lateinit var accessToken: String
    protected lateinit var course: Course

    @Before
    open fun setup() {
        ShadowLog.stream = System.out

        runBlocking {
            accessToken = performTestLogin()
            course = createCourse(getAdminAccessToken())
        }
    }
} 