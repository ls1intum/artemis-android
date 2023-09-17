package de.tum.informatics.www1.artemis.native_app.feature.metis

import de.tum.informatics.www1.artemis.native_app.core.model.Course
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.TextExercise
import de.tum.informatics.www1.artemis.native_app.core.test.BaseComposeTest
import de.tum.informatics.www1.artemis.native_app.core.test.coreTestModules
import de.tum.informatics.www1.artemis.native_app.core.test.testWebsocketModule
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.course_creation.createCourse
import de.tum.informatics.www1.artemis.native_app.core.common.test.testServerUrl
import de.tum.informatics.www1.artemis.native_app.feature.login.loginModule
import de.tum.informatics.www1.artemis.native_app.feature.login.test.getAdminAccessToken
import de.tum.informatics.www1.artemis.native_app.feature.login.test.performTestLogin
import de.tum.informatics.www1.artemis.native_app.feature.login.test.testLoginModule
import de.tum.informatics.www1.artemis.native_app.feature.login.test.user2Username
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.OneToOneChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.service.network.ConversationService
import de.tum.informatics.www1.artemis.native_app.feature.metistest.MetisDatabaseProviderMock
import org.junit.Before
import org.junit.Rule
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import org.koin.test.KoinTestRule
import org.koin.test.get
import org.robolectric.shadows.ShadowLog

abstract class ConversationBaseTest : BaseComposeTest() {

    @get:Rule
    val koinTestRule = KoinTestRule.create {
        androidContext(context)

        modules(coreTestModules)
        modules(loginModule, communicationModule, testLoginModule, module {
            single<de.tum.informatics.www1.artemis.native_app.feature.metis.shared.MetisDatabaseProvider> {
                MetisDatabaseProviderMock(
                    context
                )
            }
        }, testWebsocketModule)
    }

    protected lateinit var accessToken: String

    protected lateinit var course: Course
    protected lateinit var exercise: TextExercise

    protected val conversationService: de.tum.informatics.www1.artemis.native_app.feature.metis.shared.service.network.ConversationService get() = get()

    @Before
    open fun setup() {
        ShadowLog.stream = System.out

        runBlockingWithTestTimeout {
            accessToken = performTestLogin()

            course = createCourse(getAdminAccessToken())
        }
    }

    protected suspend fun createPersonalConversation(): de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.OneToOneChat =
        conversationService.createOneToOneConversation(
            course.id!!,
            user2Username,
            accessToken,
            testServerUrl
        ).orThrow("Could not create one to one conversation")
}