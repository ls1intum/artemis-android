package de.tum.informatics.www1.artemis.native_app.feature.exercise_view.participate.text_exercise

import de.tum.informatics.www1.artemis.native_app.core.common.artemis_context.ArtemisContext
import de.tum.informatics.www1.artemis.native_app.core.common.artemis_context.ArtemisContextProvider
import de.tum.informatics.www1.artemis.native_app.core.common.artemis_context.ArtemisContextImpl
import de.tum.informatics.www1.artemis.native_app.core.common.test.UnitTest
import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.device.NetworkStatusProvider
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.participation.StudentParticipation
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.TextSubmission
import de.tum.informatics.www1.artemis.native_app.feature.exerciseview.participate.textexercise.TextExerciseParticipationViewModel
import de.tum.informatics.www1.artemis.native_app.feature.exerciseview.service.TextEditorService
import de.tum.informatics.www1.artemis.native_app.feature.exerciseview.service.TextSubmissionService
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import de.tum.informatics.www1.artemis.native_app.feature.exerciseview.participate.textexercise.TextExerciseEditingCoordinator
import de.tum.informatics.www1.artemis.native_app.feature.exerciseview.participate.textexercise.TextExerciseDraft
import de.tum.informatics.www1.artemis.native_app.feature.exerciseview.participate.textexercise.TextExerciseDraftKey
import de.tum.informatics.www1.artemis.native_app.feature.exerciseview.participate.textexercise.TextExerciseDraftStore
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.experimental.categories.Category
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
@Category(UnitTest::class)
class TextExerciseDraftRegressionTest {
    private val dispatcher = StandardTestDispatcher()
    private val session = ArtemisContextImpl.LoggedIn("https://test.example", "token", "student")
    private var getParticipation: suspend () -> NetworkResponse<de.tum.informatics.www1.artemis.native_app.core.model.exercise.participation.Participation> = { NetworkResponse.Response(participation) }
    private val editor = object : TextEditorService {
        override val onArtemisContextChanged = flowOf(session)
        override suspend fun getParticipation(participationId: Long, session: ArtemisContext.LoggedIn?) = getParticipation()
    }
    private var submissions = 0
    private val submission = object : TextSubmissionService {
        override val onArtemisContextChanged = flowOf(session)
        override suspend fun update(textSubmission: TextSubmission, exerciseId: Long, session: ArtemisContext.LoggedIn?): NetworkResponse<TextSubmission> {
            submissions++
            return NetworkResponse.Response(TextSubmission(id = 3, text = textSubmission.text))
        }
    }
    private val provider = object : ArtemisContextProvider {
        override val stateFlow = MutableStateFlow<ArtemisContext>(session)
        override fun setCourseId(courseId: Long) = Unit
        override fun clearCourseId() = Unit
    }
    private val store = object : TextExerciseDraftStore {
        override val coordinator = TextExerciseEditingCoordinator(CoroutineScope(SupervisorJob() + dispatcher))
        private val drafts = mutableMapOf<TextExerciseDraftKey, TextExerciseDraft>()
        override suspend fun load(key: TextExerciseDraftKey, owner: String): TextExerciseDraft? {
            coordinator.claim(key, owner)
            return drafts[key]
        }
        override fun save(key: TextExerciseDraftKey, owner: String, draft: TextExerciseDraft): Deferred<Boolean> {
            if (!coordinator.owns(key, owner)) return CompletableDeferred(false)
            drafts[key] = draft
            return CompletableDeferred(true)
        }
    }
    private val participation = StudentParticipation.StudentParticipationImpl(
        id = 2, submissions = listOf(TextSubmission(id = 3, text = "Server answer"))
    )

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)

    }

    @After
    fun teardown() { Dispatchers.resetMain() }

    private fun viewModel() = TextExerciseParticipationViewModel(1, 2, submission, editor, provider, store, dispatcher)

    @Test
    fun `typing only saves locally and never submits automatically`() = runTest(dispatcher) {
        val model = viewModel()
        runCurrent()
        model.updateText("Draft")
        advanceTimeBy(10_000)
        runCurrent()
        assertEquals("Draft", model.text.first())
        assertEquals(0, submissions)
    }

    @Test
    fun `delayed initial server response cannot overwrite typing`() = runTest(dispatcher) {
        val pending = CompletableDeferred<Unit>()
        getParticipation = {
            pending.await()
            NetworkResponse.Response(participation)
        }
        val model = viewModel()
        runCurrent()
        model.updateText("Typed while loading")
        pending.complete(Unit)
        runCurrent()
        assertEquals("Typed while loading", model.text.first())
    }
}
