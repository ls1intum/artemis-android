package de.tum.informatics.www1.artemis.native_app.feature.metis

import android.content.Context
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import de.tum.informatics.www1.artemis.native_app.core.model.Course
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.TextExercise
import de.tum.informatics.www1.artemis.native_app.core.test.coreTestModules
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.course_creation.createCourse
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.testServerUrl
import de.tum.informatics.www1.artemis.native_app.core.websocket.impl.WebsocketProvider
import de.tum.informatics.www1.artemis.native_app.feature.login.loginModule
import de.tum.informatics.www1.artemis.native_app.feature.login.test.getAdminAccessToken
import de.tum.informatics.www1.artemis.native_app.feature.login.test.performTestLogin
import de.tum.informatics.www1.artemis.native_app.feature.login.test.testLoginModule
import de.tum.informatics.www1.artemis.native_app.feature.login.test.user2Username
import de.tum.informatics.www1.artemis.native_app.feature.metis.content.OneToOneChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.service.ConversationService
import de.tum.informatics.www1.artemis.native_app.feature.metis_test.MetisDatabaseProviderMock
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Before
import org.junit.Rule
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.KoinTestRule
import org.koin.test.get
import org.robolectric.shadows.ShadowLog

abstract class ConversationBaseTest : KoinTest {

    protected val testDispatcher = UnconfinedTestDispatcher()

    protected val context: Context get() = InstrumentationRegistry.getInstrumentation().context

    @get:Rule
    val composeTestRule = createComposeRule()

    @get:Rule
    val koinTestRule = KoinTestRule.create {
        androidContext(context)

        modules(coreTestModules)
        modules(loginModule, communicationModule, testLoginModule, module {
            single<MetisDatabaseProvider> {
                MetisDatabaseProviderMock(
                    context
                )
            }
            single<WebsocketProvider> {
                WebsocketProvider(
                    serverConfigurationService = get(),
                    accountService = get(),
                    jsonProvider = get(),
                    networkStatusProvider = get(),
                    coroutineContext = testDispatcher
                )
            }
        })
    }

    protected lateinit var accessToken: String

    protected lateinit var course: Course
    protected lateinit var exercise: TextExercise

    protected val conversationService: ConversationService get() = get()

    @Before
    open fun setup() {
        ShadowLog.stream = System.out

        runBlocking {
            accessToken = performTestLogin()

            course = createCourse(getAdminAccessToken())
        }
    }

    protected suspend fun createPersonalConversation(): OneToOneChat =
        conversationService.createOneToOneConversation(
            course.id!!,
            user2Username,
            accessToken,
            testServerUrl
        ).orThrow("Could not create one to one conversation")
}