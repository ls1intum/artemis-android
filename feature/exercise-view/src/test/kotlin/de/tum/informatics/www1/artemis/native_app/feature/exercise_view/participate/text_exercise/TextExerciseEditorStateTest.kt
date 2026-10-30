package de.tum.informatics.www1.artemis.native_app.feature.exercise_view.participate.text_exercise

import de.tum.informatics.www1.artemis.native_app.core.common.artemis_context.ArtemisContext
import de.tum.informatics.www1.artemis.native_app.core.common.artemis_context.ArtemisContextImpl
import de.tum.informatics.www1.artemis.native_app.core.common.artemis_context.ArtemisContextProvider
import de.tum.informatics.www1.artemis.native_app.core.common.test.UnitTest
import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.TextExercise
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.participation.Participation
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.participation.StudentParticipation
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.Result
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.TextSubmission
import de.tum.informatics.www1.artemis.native_app.feature.exerciseview.participate.textexercise.TextExerciseDraft
import de.tum.informatics.www1.artemis.native_app.feature.exerciseview.participate.textexercise.TextExerciseDraftKey
import de.tum.informatics.www1.artemis.native_app.feature.exerciseview.participate.textexercise.TextExerciseDraftStore
import de.tum.informatics.www1.artemis.native_app.feature.exerciseview.participate.textexercise.TextExerciseParticipationViewModel
import de.tum.informatics.www1.artemis.native_app.feature.exerciseview.participate.textexercise.isTextSubmissionOpen
import de.tum.informatics.www1.artemis.native_app.feature.exerciseview.service.TextEditorService
import de.tum.informatics.www1.artemis.native_app.feature.exerciseview.service.TextSubmissionService
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import de.tum.informatics.www1.artemis.native_app.feature.exerciseview.participate.textexercise.TextExerciseEditingCoordinator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.experimental.categories.Category
import java.io.IOException
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
@Category(UnitTest::class)
class TextExerciseEditorStateTest {
    private val dispatcher = StandardTestDispatcher()
    private val session = ArtemisContextImpl.LoggedIn("https://one.example", "old-token", "student")
    private val context = object : ArtemisContextProvider {
        override val stateFlow = MutableStateFlow<ArtemisContext>(session)
        override fun setCourseId(courseId: Long) = Unit
        override fun clearCourseId() = Unit
    }
    private val drafts = mutableMapOf<TextExerciseDraftKey, TextExerciseDraft>()
    private var diskFailure = false
    private var readFailure = false
    private val store = object : TextExerciseDraftStore {
        override val coordinator = TextExerciseEditingCoordinator(CoroutineScope(SupervisorJob() + dispatcher))
        override suspend fun load(key: TextExerciseDraftKey, owner: String): TextExerciseDraft? {
            coordinator.claim(key, owner)
            if (readFailure) throw IOException("unreadable draft")
            return drafts[key]
        }
        override fun save(key: TextExerciseDraftKey, owner: String, draft: TextExerciseDraft): Deferred<Boolean> {
            if (!coordinator.owns(key, owner)) return CompletableDeferred(false)
            if (diskFailure) return CompletableDeferred<Boolean>().also { it.completeExceptionally(IOException("full disk")) }
            drafts[key] = draft
            return CompletableDeferred(true)
        }
    }
    private var server: TextSubmission? = TextSubmission(id = 3, text = "Original", submitted = true)
    private var get: (suspend () -> NetworkResponse<Participation>)? = null
    private var put: (suspend (TextSubmission) -> NetworkResponse<TextSubmission>)? = null
    private val sent = mutableListOf<TextSubmission>()
    private val credentials = mutableListOf<ArtemisContext.LoggedIn?>()
    private val editor = object : TextEditorService {
        override val onArtemisContextChanged = flowOf(session)
        override suspend fun getParticipation(participationId: Long, session: ArtemisContext.LoggedIn?): NetworkResponse<Participation> {
            credentials += session
            return get?.invoke() ?: NetworkResponse.Response(participation())
        }
    }
    private val submission = object : TextSubmissionService {
        override val onArtemisContextChanged = flowOf(session)
        override suspend fun update(textSubmission: TextSubmission, exerciseId: Long, session: ArtemisContext.LoggedIn?): NetworkResponse<TextSubmission> {
            credentials += session
            sent += textSubmission
            return put?.invoke(textSubmission) ?: NetworkResponse.Response(
                TextSubmission(id = 4, text = textSubmission.text, submitted = true).also { server = it }
            )
        }
    }

    private fun participation() = StudentParticipation.StudentParticipationImpl(
        id = 2, exercise = TextExercise(id = 1), submissions = listOfNotNull(server)
    )
    private fun model() = TextExerciseParticipationViewModel(1, 2, submission, editor, context, store, dispatcher)
    private val key get() = TextExerciseDraftKey("https://one.example", "student", 1, 2)

    @Before fun setup() { Dispatchers.setMain(dispatcher) }
    @After fun teardown() { Dispatchers.resetMain() }

    @Test fun `empty participation is editable and creates a submission with no id`() = runTest(dispatcher) {
        server = null
        val model = model()
        runCurrent()
        assertTrue(model.state.value.draftReady)
        assertFalse(model.state.value.loading)
        model.updateText("First answer")
        model.submit()
        runCurrent()
        assertNull(sent.single().id)
        assertTrue(model.state.value.isSubmitted)
        assertEquals("First answer", drafts[key]?.text)
    }

    @Test fun `recreated editor restores the durable draft without submitting`() = runTest(dispatcher) {
        val first = model()
        runCurrent()
        first.updateText("Uns ubmitted **Markdown**\n\nSecond paragraph 🧪")
        runCurrent()
        val recovered = model()
        runCurrent()
        assertEquals("Uns ubmitted **Markdown**\n\nSecond paragraph 🧪", recovered.state.value.text)
        assertTrue(recovered.state.value.recovered)
        assertFalse(recovered.state.value.isSubmitted)
        assertTrue(sent.isEmpty())
    }

    @Test fun `older acknowledgement preserves newer edits and uses replacement id next time`() = runTest(dispatcher) {
        val pending = CompletableDeferred<NetworkResponse<TextSubmission>>()
        put = { pending.await() }
        val model = model()
        runCurrent()
        model.updateText("First revision")
        model.submit()
        model.submit()
        runCurrent()
        assertEquals(1, sent.size)
        model.updateText("Second revision")
        val ack = TextSubmission(id = 41, text = "First revision", submitted = true)
        server = ack
        pending.complete(NetworkResponse.Response(ack))
        runCurrent()
        assertEquals("Second revision", model.state.value.text)
        assertFalse(model.state.value.isSubmitted)
        assertEquals("Second revision", drafts[key]?.text)
        assertEquals(41, drafts[key]?.base?.id)
        put = null
        model.submit()
        runCurrent()
        assertEquals(listOf(3, 41), sent.map { it.id })
        assertEquals("Second revision", sent.last().text)
    }

    @Test fun `reopened editor cannot submit until an older editor request finishes`() = runTest(dispatcher) {
        val pending = CompletableDeferred<NetworkResponse<TextSubmission>>()
        put = { pending.await() }
        val older = model()
        runCurrent()
        older.updateText("In flight")
        older.submit()
        runCurrent()
        val newer = model()
        runCurrent()
        newer.updateText("Next answer")
        newer.submit()
        runCurrent()
        assertEquals(1, sent.size)
        assertFalse(newer.state.value.canSubmit)
        server = TextSubmission(id = 41, text = "In flight", submitted = true)
        pending.complete(NetworkResponse.Response(checkNotNull(server)))
        runCurrent()
        assertTrue(newer.state.value.canSubmit)
        newer.submit()
        runCurrent()
        assertEquals(1, sent.size)
        assertNotNull(newer.state.value.conflict)
    }

    @Test fun `older editor acknowledgement cannot overwrite a newer editor draft`() = runTest(dispatcher) {
        val pending = CompletableDeferred<NetworkResponse<TextSubmission>>()
        put = { pending.await() }
        val older = model()
        runCurrent()
        older.updateText("Older submitted text")
        older.submit()
        runCurrent()
        val newer = model()
        runCurrent()
        newer.updateText("Newer editor draft")
        runCurrent()
        pending.complete(NetworkResponse.Response(TextSubmission(id = 41, text = "Older submitted text", submitted = true)))
        runCurrent()
        assertEquals("Newer editor draft", drafts[key]?.text)
        assertEquals("Newer editor draft", newer.state.value.text)
    }

    @Test fun `reload after a lost acknowledgement clears the obsolete failure status`() = runTest(dispatcher) {
        put = {
            server = TextSubmission(id = 51, text = it.text, submitted = true)
            NetworkResponse.Failure(IOException("lost acknowledgement"))
        }
        val model = model()
        runCurrent()
        model.updateText("Accepted answer")
        model.submit()
        runCurrent()
        model.onRequestReload()
        runCurrent()
        assertTrue(model.state.value.isSubmitted)
        assertFalse(model.state.value.submitFailed)
    }

    @Test fun `preflight detects another device answer and requires explicit resolution`() = runTest(dispatcher) {
        val model = model()
        runCurrent()
        model.updateText("My local answer")
        server = TextSubmission(id = 4, text = "Team member answer", submitted = true)
        model.submit()
        runCurrent()
        assertEquals("My local answer", model.state.value.text)
        assertNotNull(model.state.value.conflict)
        assertFalse(model.state.value.canSubmit)
        assertTrue(sent.isEmpty())
        model.keepLocalDraft()
        model.submit()
        runCurrent()
        assertEquals("My local answer", sent.single().text)
        assertEquals(4, sent.single().id)
    }

    @Test fun `recovery detects changed server answer without overwriting either answer`() = runTest(dispatcher) {
        val first = model()
        runCurrent()
        first.updateText("My saved answer")
        runCurrent()
        server = TextSubmission(id = 4, text = "Other device", submitted = true)
        val recovered = model()
        runCurrent()
        assertEquals("My saved answer", recovered.state.value.text)
        assertEquals("Other device", recovered.state.value.conflict?.text)
        recovered.useServerVersion()
        runCurrent()
        assertEquals("Other device", drafts[key]?.text)
        assertTrue(recovered.state.value.isSubmitted)
        assertTrue(sent.isEmpty())
    }

    @Test fun `failed submit keeps draft and never retries automatically`() = runTest(dispatcher) {
        put = { NetworkResponse.Failure(IOException("offline")) }
        val model = model()
        runCurrent()
        model.updateText("Offline answer")
        model.submit()
        runCurrent()
        assertEquals(1, sent.size)
        assertTrue(model.state.value.submitFailed)
        assertEquals("Offline answer", drafts[key]?.text)
        assertFalse(model.state.value.isSubmitted)
    }

    @Test fun `lost acknowledgement reconciles accepted answer on explicit retry`() = runTest(dispatcher) {
        put = {
            server = TextSubmission(id = 51, text = it.text, submitted = true)
            NetworkResponse.Failure(IOException("lost acknowledgement"))
        }
        val model = model()
        runCurrent()
        model.updateText("Accepted answer")
        model.submit()
        runCurrent()
        model.submit()
        runCurrent()
        assertEquals(1, sent.size)
        assertTrue(model.state.value.isSubmitted)
        assertEquals(51, model.state.value.server?.id)
    }

    @Test fun `changed session during preflight cannot submit or accept stale results`() = runTest(dispatcher) {
        val model = model()
        runCurrent()
        model.updateText("Private draft")
        val pending = CompletableDeferred<NetworkResponse<Participation>>()
        get = { pending.await() }
        model.submit()
        runCurrent()
        context.stateFlow.value = ArtemisContextImpl.LoggedIn("https://two.example", "new-token", "other")
        pending.complete(NetworkResponse.Response(participation()))
        runCurrent()
        assertTrue(model.state.value.sessionExpired)
        assertTrue(sent.isEmpty())
        assertEquals("Private draft", drafts[key]?.text)
        assertTrue(credentials.all { it == session })
    }

    @Test fun `account switch back does not resume an expired editing session`() = runTest(dispatcher) {
        val model = model()
        runCurrent()
        model.updateText("Private draft")
        context.stateFlow.value = ArtemisContextImpl.Empty
        runCurrent()
        context.stateFlow.value = session
        runCurrent()
        model.submit()
        runCurrent()
        assertTrue(model.state.value.sessionExpired)
        assertTrue(sent.isEmpty())
    }

    @Test fun `disk error is visible and retry persists the retained answer`() = runTest(dispatcher) {
        val model = model()
        runCurrent()
        diskFailure = true
        model.updateText("Keep in memory")
        runCurrent()
        assertTrue(model.state.value.localSaveFailed)
        assertFalse(model.state.value.canSubmit)
        assertEquals("Keep in memory", model.state.value.text)
        diskFailure = false
        model.retryLocalSave()
        runCurrent()
        assertFalse(model.state.value.localSaveFailed)
        assertEquals("Keep in memory", drafts[key]?.text)
    }

    @Test fun `initial server content never claims a local save after a disk failure`() = runTest(dispatcher) {
        diskFailure = true
        val model = model()
        runCurrent()
        assertTrue(model.state.value.localSaveFailed)
        assertEquals("Original", model.state.value.text)
    }

    @Test fun `recovered draft without a baseline requires review of a different server answer`() = runTest(dispatcher) {
        drafts[key] = TextExerciseDraft("Recovered before the server could be checked")
        val model = model()
        runCurrent()
        assertEquals("Recovered before the server could be checked", model.state.value.text)
        assertEquals("Original", model.state.value.conflict?.text)
        assertTrue(sent.isEmpty())
    }

    @Test fun `unreadable draft is never overwritten by typing until recovery succeeds`() = runTest(dispatcher) {
        drafts[key] = TextExerciseDraft("Unseen saved work")
        readFailure = true
        val model = model()
        runCurrent()
        assertFalse(model.state.value.draftReady)
        model.updateText("Must not replace the unread draft")
        runCurrent()
        assertEquals("Unseen saved work", drafts[key]?.text)
        readFailure = false
        model.retryLocalSave()
        runCurrent()
        assertEquals("Unseen saved work", model.state.value.text)
    }

    @Test fun `assessed answer remains eligible for an explicit new submission`() = runTest(dispatcher) {
        server = TextSubmission(id = 3, text = "Assessed", submitted = true, results = listOf(Result(id = 7)))
        val model = model()
        runCurrent()
        model.updateText("Revised after assessment")
        runCurrent()
        assertTrue(model.state.value.canSubmit)
        model.submit()
        runCurrent()
        assertEquals("Revised after assessment", sent.single().text)
    }

    @Test fun `UTF16 limit accepts 15000 emoji but preserves and rejects 15001`() = runTest(dispatcher) {
        val model = model()
        runCurrent()
        model.updateText("🧪".repeat(15_000))
        runCurrent()
        assertTrue(model.state.value.canSubmit)
        model.updateText("🧪".repeat(15_001))
        runCurrent()
        assertEquals(30_002, model.state.value.text.length)
        assertFalse(model.state.value.canSubmit)
        model.submit()
        assertTrue(sent.isEmpty())
        assertEquals(30_002, drafts[key]?.text?.length)
    }

    @Test fun `individual deadline and late initialization mirror server eligibility`() {
        val now = Instant.parse("2026-10-26T12:00:00Z")
        val past = Instant.parse("2026-10-25T12:00:00Z")
        val future = Instant.parse("2026-10-27T12:00:00Z")
        fun participation(initialized: Instant?, individual: Instant? = null) = StudentParticipation.StudentParticipationImpl(
            initializationDate = initialized, individualDueDate = individual, exercise = TextExercise(dueDate = past)
        )
        assertFalse(isTextSubmissionOpen(participation(past - kotlin.time.Duration.parse("1h")), now))
        assertTrue(isTextSubmissionOpen(participation(past - kotlin.time.Duration.parse("1h"), future), now))
        assertTrue(isTextSubmissionOpen(participation(now), now))
        assertFalse(isTextSubmissionOpen(participation(null), now))
        assertFalse(isTextSubmissionOpen(StudentParticipation.StudentParticipationImpl(
            initializationDate = past - kotlin.time.Duration.parse("1h"), individualDueDate = past, exercise = TextExercise(dueDate = null)
        ), now))
    }
}
