package de.tum.informatics.www1.artemis.native_app.feature.quiz

import androidx.compose.runtime.Composable
import androidx.lifecycle.SavedStateHandle
import de.tum.informatics.www1.artemis.native_app.core.data.service.network.ParticipationService
import de.tum.informatics.www1.artemis.native_app.core.model.Course
import de.tum.informatics.www1.artemis.native_app.core.test.BaseComposeTest
import de.tum.informatics.www1.artemis.native_app.core.test.coreTestModules
import de.tum.informatics.www1.artemis.native_app.core.test.testWebsocketModule
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.course_creation.createCourse
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.generateId
import de.tum.informatics.www1.artemis.native_app.feature.login.loginModule
import de.tum.informatics.www1.artemis.native_app.feature.login.test.getAdminAccessToken
import de.tum.informatics.www1.artemis.native_app.feature.login.test.performTestLogin
import de.tum.informatics.www1.artemis.native_app.feature.login.test.testLoginModule
import de.tum.informatics.www1.artemis.native_app.feature.quiz.participation.QuizParticipationViewModel
import de.tum.informatics.www1.artemis.native_app.feature.quiz.service.QuizExerciseService
import org.junit.Before
import org.junit.Rule
import org.koin.android.ext.koin.androidContext
import org.koin.test.KoinTestRule
import org.koin.test.get
import org.robolectric.shadows.ShadowLog

internal abstract class QuizBaseE2eTest(protected val quizType: QuizType.WorkableQuizType) : BaseComposeTest() {

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

    protected val quizExerciseService: QuizExerciseService get() = get()

    @Before
    fun setup() {
        ShadowLog.stream = System.out

        runBlockingWithTestTimeout(timeoutMultiplier = 2) {
            course = createCourse(getAdminAccessToken())
            courseId = course.id!!

            accessToken = performTestLogin()

            setupHook()
        }
    }

    open suspend fun setupHook() {}

    protected fun setupUi(exerciseId: Long, content: @Composable (QuizParticipationViewModel) -> Unit): QuizParticipationViewModel {
        val viewModel = QuizParticipationViewModel(
            courseId = courseId,
            exerciseId = exerciseId,
            quizType = quizType,
            savedStateHandle = SavedStateHandle(),
            quizExerciseService = get(),
            serverConfigurationService = get(),
            accountService = get(),
            quizParticipationService = get(),
            websocketProvider = get(),
            networkStatusProvider = get(),
            participationService = get(),
            serverTimeService = get()
        )

        composeTestRule.setContent {
            content(viewModel)
        }

        return viewModel
    }

    protected fun getBackgroundImageFilePath() = "/api/files/drag-and-drop/backgrounds/${generateId()}/dndbackground.png"

    protected fun getBackgroundImageBytes(): ByteArray = context.resources.openRawResource(R.raw.dndbackground).use { inputStream ->
        inputStream.readBytes()
    }
}