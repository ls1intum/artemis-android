package de.tum.informatics.www1.artemis.native_app.feature.quiz

import de.tum.informatics.www1.artemis.native_app.core.data.service.ParticipationService
import de.tum.informatics.www1.artemis.native_app.core.model.Course
import de.tum.informatics.www1.artemis.native_app.core.test.BaseComposeTest
import de.tum.informatics.www1.artemis.native_app.core.test.coreTestModules
import de.tum.informatics.www1.artemis.native_app.core.test.testWebsocketModule
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.DefaultTimeoutMillis
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.course_creation.createCourse
import de.tum.informatics.www1.artemis.native_app.feature.login.loginModule
import de.tum.informatics.www1.artemis.native_app.feature.login.test.getAdminAccessToken
import de.tum.informatics.www1.artemis.native_app.feature.login.test.performTestLogin
import de.tum.informatics.www1.artemis.native_app.feature.login.test.testLoginModule
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Before
import org.junit.Rule
import org.koin.android.ext.koin.androidContext
import org.koin.test.KoinTestRule
import org.koin.test.get
import org.robolectric.shadows.ShadowLog

abstract class QuizParticipationBaseE2eTest : BaseComposeTest() {

    protected var courseId: Long = 0L
    protected lateinit var course: Course

    protected lateinit var accessToken: String

    @get:Rule
    val koinTestRule = KoinTestRule.create {
        androidContext(context)

        modules(coreTestModules)
        modules(loginModule, testLoginModule, testWebsocketModule, quizParticipationModule)
    }

    protected val participationService: ParticipationService get() = get()

    @Before
    fun setup() {
        ShadowLog.stream = System.out

        runBlocking {
            withTimeout(DefaultTimeoutMillis) {
                course = createCourse(getAdminAccessToken())
                courseId = course.id!!

                accessToken = performTestLogin()

                setupHook()
            }
        }
    }

    open suspend fun setupHook() {}
}