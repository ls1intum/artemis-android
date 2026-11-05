package de.tum.informatics.www1.artemis.native_app.feature.exerciseview.participate.textexercise

import androidx.lifecycle.viewModelScope
import de.tum.informatics.www1.artemis.native_app.core.common.artemis_context.ArtemisContext
import de.tum.informatics.www1.artemis.native_app.core.common.artemis_context.ArtemisContextProvider
import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.participation.Participation
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.TextSubmission
import de.tum.informatics.www1.artemis.native_app.core.ui.ReloadableViewModel
import de.tum.informatics.www1.artemis.native_app.feature.exerciseview.service.TextEditorService
import de.tum.informatics.www1.artemis.native_app.feature.exerciseview.service.TextSubmissionService
import java.util.UUID
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.time.Clock
import kotlin.time.Instant

internal const val TEXT_EXERCISE_MAX_LENGTH = 30_000

internal data class TextExerciseEditorState(
    val text: String = "",
    val draftReady: Boolean = false,
    val loading: Boolean = true,
    val loadFailed: Boolean = false,
    val saving: Boolean = false,
    val localSaveFailed: Boolean = false,
    val submitting: Boolean = false,
    val submittingElsewhere: Boolean = false,
    val submitFailed: Boolean = false,
    val sessionExpired: Boolean = false,
    val recovered: Boolean = false,
    val closed: Boolean = false,
    val participation: Participation? = null,
    val server: TextExerciseServerVersion? = null,
    val conflict: TextExerciseServerVersion? = null
) {
    val isSubmitted: Boolean get() = server?.submitted == true && text == server.text
    val canSubmit: Boolean get() = draftReady && !loading && participation != null && !loadFailed &&
        !submitting && !submittingElsewhere && !sessionExpired && !closed && !localSaveFailed && conflict == null &&
        text.isNotBlank() && text.length <= TEXT_EXERCISE_MAX_LENGTH && !isSubmitted
}

/** The server's save endpoint submits. Only [submit] may write to it. */
internal class TextExerciseParticipationViewModel(
    private val exerciseId: Long,
    private val participationId: Long,
    private val textSubmissionService: TextSubmissionService,
    private val textEditorService: TextEditorService,
    private val contextProvider: ArtemisContextProvider,
    private val draftStore: TextExerciseDraftStore,
    private val coroutineContext: CoroutineContext = EmptyCoroutineContext
) : ReloadableViewModel() {
    private val session = (contextProvider.stateFlow.value as? ArtemisContext.LoggedIn)?.let {
        // Copy the credentials: requests must never follow mutable account/server selection.
        de.tum.informatics.www1.artemis.native_app.core.common.artemis_context.ArtemisContextImpl.LoggedIn(
            it.serverUrl, it.authToken, it.loginName
        )
    }
    private val key = session?.let { TextExerciseDraftKey(it.serverUrl, it.loginName, exerciseId, participationId) }
    private val _state = MutableStateFlow(TextExerciseEditorState())
    val state: StateFlow<TextExerciseEditorState> = _state.asStateFlow()
    val text = state.map { it.text }
    private val owner = UUID.randomUUID().toString()
    private var ownershipClaimed = false
    private var base: TextExerciseServerVersion? = null
    private var edited = false
    private var saveVersion = 0L
    private var deadlineJob: Job? = null
    private var loadJob: Job? = null

    init {
        viewModelScope.launch(coroutineContext) {
            contextProvider.stateFlow.collect {
                if (!sameSession(it)) _state.value = _state.value.copy(sessionExpired = true, loading = false, submitting = false)
            }
        }
        viewModelScope.launch(coroutineContext) {
            draftStore.coordinator.submissions.collect { submissions ->
                _state.value = _state.value.copy(submittingElsewhere = key in submissions && !_state.value.submitting)
            }
        }
        load()
    }

    private fun sameSession(context: ArtemisContext): Boolean = session != null && context is ArtemisContext.LoggedIn &&
        context.serverUrl == session.serverUrl && context.loginName == session.loginName && context.authToken == session.authToken

    private fun sessionCurrent(): Boolean {
        val current = !_state.value.sessionExpired && sameSession(contextProvider.stateFlow.value) &&
            (!ownershipClaimed || key?.let { draftStore.coordinator.owns(it, owner) } == true)
        if (!current) _state.value = _state.value.copy(sessionExpired = true, loading = false, submitting = false)
        return current
    }

    override fun onRequestReload() = load()

    private fun load() {
        if (loadJob?.isActive == true || !sessionCurrent()) return
        _state.value = _state.value.copy(loading = true, loadFailed = false)
        loadJob = viewModelScope.launch(coroutineContext) {
            if (!_state.value.draftReady) {
                try {
                    val draft = draftStore.load(checkNotNull(key), owner)
                    ownershipClaimed = true
                    if (!sessionCurrent()) return@launch
                    base = draft?.base
                    // updateText can also be called while the initial disk read is pending.
                    if (!edited) _state.value = _state.value.copy(text = draft?.text.orEmpty(), recovered = draft != null)
                    _state.value = _state.value.copy(draftReady = true, localSaveFailed = false)
                } catch (exception: CancellationException) {
                    throw exception
                } catch (_: Exception) {
                    if (sessionCurrent()) _state.value = _state.value.copy(loading = false, localSaveFailed = true)
                    return@launch
                }
            }
            when (val response = textEditorService.getParticipation(participationId, checkNotNull(session))) {
                is NetworkResponse.Response -> {
                    if (!sessionCurrent()) return@launch
                    reconcile(response.data)
                }
                is NetworkResponse.Failure -> if (sessionCurrent()) {
                    _state.value = _state.value.copy(loading = false, loadFailed = true)
                }
            }
        }
    }

    private fun reconcile(participation: Participation) {
        val latest = latestSubmission(participation).version()
        val current = _state.value
        val hadDraft = current.recovered || edited
        val conflict = current.text != latest.text && (base?.let { it != latest } ?: (hadDraft && latest.text.isNotEmpty()))
        if (!hadDraft) _state.value = current.copy(text = latest.text)
        if (!conflict) base = latest
        _state.value = _state.value.copy(
            participation = participation, server = latest, conflict = latest.takeIf { conflict },
            loading = false, loadFailed = false, submitFailed = false
        )
        updateDeadline(participation)
        persist()
    }

    fun updateText(newText: String) {
        if (!sessionCurrent() || !_state.value.draftReady) return
        edited = true
        _state.value = _state.value.copy(text = newText, submitFailed = false, recovered = false)
        persist()
    }

    fun retryLocalSave() {
        if (!_state.value.draftReady) load() else persist()
    }

    private fun persist() {
        if (!sessionCurrent() || key == null) return
        val version = ++saveVersion
        val write = draftStore.save(key, owner, TextExerciseDraft(_state.value.text, base))
        _state.value = _state.value.copy(saving = true, localSaveFailed = false)
        viewModelScope.launch(coroutineContext) {
            try {
                val saved = write.await()
                if (!saved) { sessionCurrent(); return@launch }
                if (sessionCurrent() && version == saveVersion) _state.value = _state.value.copy(saving = false)
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: Exception) {
                if (sessionCurrent() && version == saveVersion) _state.value = _state.value.copy(saving = false, localSaveFailed = true)
            }
        }
    }

    /** User explicitly accepts using the local draft as the basis of the next submission. */
    fun keepLocalDraft() {
        val latest = _state.value.conflict ?: return
        if (!sessionCurrent()) return
        base = latest
        _state.value = _state.value.copy(conflict = null)
        persist()
    }

    fun useServerVersion() {
        val latest = _state.value.conflict ?: return
        if (!sessionCurrent()) return
        base = latest
        _state.value = _state.value.copy(text = latest.text, conflict = null, recovered = false)
        persist()
    }

    fun submit() {
        val current = _state.value
        if (!sessionCurrent() || !current.canSubmit) return
        val sentText = current.text
        val expected = base
        _state.value = current.copy(submitting = true, submitFailed = false)
        val started = draftStore.coordinator.launchSubmission(checkNotNull(key), owner) {
            // A fresh GET catches changes by another device/team member, including a previous lost ACK.
            val response = textEditorService.getParticipation(participationId, checkNotNull(session))
            if (!sessionCurrent()) return@launchSubmission
            if (response !is NetworkResponse.Response) {
                _state.value = _state.value.copy(submitting = false, submitFailed = true)
                return@launchSubmission
            }
            val participation = response.data
            val latest = latestSubmission(participation).version()
            _state.value = _state.value.copy(participation = participation, server = latest)
            updateDeadline(participation)
            if (latest.text == sentText && latest.submitted) {
                acknowledge(latest)
                return@launchSubmission
            }
            if (latest != expected) {
                _state.value = _state.value.copy(submitting = false, conflict = latest)
                return@launchSubmission
            }
            if (_state.value.closed || !sessionCurrent()) {
                _state.value = _state.value.copy(submitting = false)
                return@launchSubmission
            }
            // No automatic retry: a failure can mean that a submission was accepted but the ACK was lost.
            when (val saved = textSubmissionService.update(TextSubmission(id = latest.id, text = sentText, submitted = true), exerciseId, session)) {
                is NetworkResponse.Response -> if (sessionCurrent()) acknowledge(saved.data.version(sentText))
                is NetworkResponse.Failure -> if (sessionCurrent()) _state.value = _state.value.copy(submitting = false, submitFailed = true)
            }
        }
        if (!started) _state.value = _state.value.copy(submitting = false, submittingElsewhere = true)
    }

    private fun acknowledge(version: TextExerciseServerVersion) {
        base = version
        // Do not replace text: it may have changed while this request was in flight.
        _state.value = _state.value.copy(server = version, submitting = false, submitFailed = false, recovered = false)
        persist()
    }

    private fun updateDeadline(participation: Participation) {
        deadlineJob?.cancel()
        _state.value = _state.value.copy(closed = !isTextSubmissionOpen(participation, Clock.System.now()))
        val due = participation.individualDueDate ?: participation.exercise?.dueDate ?: return
        val delayUntilDue = due - Clock.System.now()
        if (delayUntilDue.isPositive()) deadlineJob = viewModelScope.launch(coroutineContext) {
            delay(delayUntilDue)
            if (sessionCurrent()) _state.value = _state.value.copy(closed = !isTextSubmissionOpen(participation, Clock.System.now()))
        }
    }

    private fun latestSubmission(participation: Participation): TextSubmission? = participation.submissions.orEmpty()
        .filterIsInstance<TextSubmission>()
        .maxWithOrNull(compareBy<TextSubmission> { it.submissionDate }.thenBy { it.id })

    private fun TextSubmission?.version(fallbackText: String = "") = TextExerciseServerVersion(
        id = this?.id, text = this?.text ?: fallbackText, submitted = this?.submitted == true
    )
}

/** Mirrors course text submissions: a late initialization remains eligible; assessed answers can be resubmitted. */
internal fun isTextSubmissionOpen(participation: Participation, now: Instant): Boolean {
    val due = participation.individualDueDate ?: participation.exercise?.dueDate ?: return true
    return now < due || participation.initializationDate?.let { it >= due } == true
}
