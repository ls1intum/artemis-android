package de.tum.informatics.www1.artemis.native_app.feature.exerciseview.participate.textexercise

import androidx.lifecycle.viewModelScope
import de.tum.informatics.www1.artemis.native_app.core.common.transformLatest
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.filterSuccess
import de.tum.informatics.www1.artemis.native_app.core.data.retryNetworkCall
import de.tum.informatics.www1.artemis.native_app.core.data.service.performAutoReloadingNetworkCall
import de.tum.informatics.www1.artemis.native_app.core.data.stateIn
import de.tum.informatics.www1.artemis.native_app.core.device.NetworkStatusProvider
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.participation.Participation
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.participation.StudentParticipation
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.Result
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.SubmissionType
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.TextSubmission
import de.tum.informatics.www1.artemis.native_app.core.ui.ReloadableViewModel
import de.tum.informatics.www1.artemis.native_app.feature.exerciseview.service.TextEditorService
import de.tum.informatics.www1.artemis.native_app.feature.exerciseview.service.TextSubmissionService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.datetime.Clock
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.time.Duration.Companion.seconds

internal class TextExerciseParticipationViewModel(
    private val exerciseId: Long,
    private val participationId: Long,
    private val textSubmissionService: TextSubmissionService,
    textEditorService: TextEditorService,
    networkStatusProvider: NetworkStatusProvider,
    coroutineContext: CoroutineContext = EmptyCoroutineContext
) : ReloadableViewModel() {

    companion object {
        internal val SyncDelay = 2.seconds
    }

    private val initialParticipationDataState: StateFlow<DataState<Participation>> =
        textEditorService.performAutoReloadingNetworkCall(networkStatusProvider) {
            getParticipation(participationId)
        }
            .stateIn(viewModelScope + coroutineContext, SharingStarted.Eagerly)

    val initialParticipation: StateFlow<Participation?> = initialParticipationDataState
        .filterSuccess()
        .flowOn(coroutineContext)
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val initialSubmission: Flow<TextSubmission> = initialParticipation
        .filterNotNull()
        .mapNotNull {
            it.submissions.orEmpty().firstOrNull() as? TextSubmission
        }
        .flowOn(coroutineContext)

    /**
     * The latest submission the server knows about
     */
    private val serverText: Flow<String> = initialSubmission.map { it.text.orEmpty() }

    private val _text = MutableStateFlow<String?>(null)
    val text: Flow<String> = _text.map { it.orEmpty() }

    val syncState: Flow<SyncState> = transformLatest(
        serverText,
        _text,
        initialSubmission,
        requestReload.onStart { emit(Unit) }
    ) { serverText, text, initialSubmission, _ ->
        if (serverText != text && text != null) {
            emit(SyncState.SyncPending)

            // Wait 2 seconds before syncing with the server
            delay(SyncDelay)
            emit(SyncState.Syncing)

            when (val response = submitChanges(initialSubmission, text)) {
                is NetworkResponse.Response -> {
                    emit(SyncState.Synced(response.data))
                }

                is NetworkResponse.Failure -> {
                    emit(SyncState.SyncFailed)
                }
            }
        }
    }
        .onStart { emit(SyncState.Synced(initialSubmission.first())) }
        .flowOn(coroutineContext)
        .shareIn(viewModelScope, SharingStarted.Eagerly, replay = 1)

    val latestSubmission: StateFlow<TextSubmission?> =
        flow { emit(initialSubmission.first()) }
            .onCompletion {
                emitAll(
                    syncState
                        .mapNotNull { syncState ->
                            when (syncState) {
                                is SyncState.Synced -> syncState.submission
                                else -> null
                            }
                        }
                )
            }
            .flowOn(coroutineContext)
            .stateIn(viewModelScope, SharingStarted.Lazily, null)

    val latestResult: StateFlow<Result?> = latestSubmission
        .filterNotNull()
        .map { it.results.orEmpty().lastOrNull() }
        .flowOn(coroutineContext)
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    init {
        viewModelScope.launch(coroutineContext) {
            updateText(serverText.first())
        }
    }

    fun updateText(newText: String) {
        _text.value = newText
    }

    /**
     * Try to submit changes.
     * @return true on success
     */
    private suspend fun submitChanges(
        initialSubmission: TextSubmission,
        text: String
    ): NetworkResponse<TextSubmission> {
        return retryNetworkCall {
            textSubmissionService.update(
                textSubmission = TextSubmission(
                    id = initialSubmission.id,
                    submissionDate = Clock.System.now(),
                    participation = StudentParticipation.StudentParticipationImpl(id = participationId),
                    submitted = true,
                    text = text,
                    submissionType = SubmissionType.MANUAL
                ),
                exerciseId = exerciseId
            )
        }
    }
}