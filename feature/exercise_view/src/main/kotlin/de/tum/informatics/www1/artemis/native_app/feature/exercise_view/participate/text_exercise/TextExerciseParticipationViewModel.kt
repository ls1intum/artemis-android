package de.tum.informatics.www1.artemis.native_app.feature.exercise_view.participate.text_exercise

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.tum.informatics.www1.artemis.native_app.core.common.transformLatest
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.filterSuccess
import de.tum.informatics.www1.artemis.native_app.core.data.retryNetworkCall
import de.tum.informatics.www1.artemis.native_app.core.data.retryOnInternet
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.datastore.authToken
import de.tum.informatics.www1.artemis.native_app.core.device.NetworkStatusProvider
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.participation.Participation
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.participation.StudentParticipation
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.Result
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.SubmissionType
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.TextSubmission
import de.tum.informatics.www1.artemis.native_app.feature.exercise_view.service.TextEditorService
import de.tum.informatics.www1.artemis.native_app.feature.exercise_view.service.TextSubmissionService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlin.time.Duration.Companion.seconds

internal class TextExerciseParticipationViewModel(
    private val exerciseId: Long,
    private val participationId: Long,
    private val textSubmissionService: TextSubmissionService,
    private val serverConfigurationService: ServerConfigurationService,
    private val accountService: AccountService,
    private val textEditorService: TextEditorService,
    private val networkStatusProvider: NetworkStatusProvider
) : ViewModel() {

    private val initialParticipationDataState: StateFlow<DataState<Participation>> =
        transformLatest(
            serverConfigurationService.serverUrl,
            accountService.authToken
        ) { serverUrl, authToken ->
            emitAll(
                retryOnInternet(networkStatusProvider.currentNetworkStatus) {
                    textEditorService.getParticipation(participationId, serverUrl, authToken)
                }
            )
        }
            .stateIn(viewModelScope, SharingStarted.Eagerly, DataState.Loading())

    val initialParticipation: Flow<Participation> = initialParticipationDataState
        .filterSuccess()
        .shareIn(viewModelScope, SharingStarted.Eagerly, replay = 1)

    private val initialSubmission: Flow<TextSubmission> = initialParticipation.mapNotNull {
        it.submissions.orEmpty().firstOrNull() as? TextSubmission
    }

    /**
     * The latest submission the server knows about
     */
    private val serverText: Flow<String> = initialSubmission.map { it.text.orEmpty() }

    private val _text = MutableStateFlow<String?>(null)
    val text: Flow<String> = _text.map { it.orEmpty() }

    private val retrySync = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val syncState: Flow<SyncState> = retrySync
        .onStart { emit(Unit) }
        .flatMapLatest {
            transformLatest(
                serverText,
                _text,
                initialSubmission
            ) { serverText, text, initialSubmission ->
                if (serverText != text && text != null) {
                    emit(SyncState.SyncPending)
                    // Wait 2 seconds before syncing with the server
                    delay(2.seconds)
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
        }
        .onStart { emit(SyncState.Synced(initialSubmission.first())) }
        .shareIn(viewModelScope, SharingStarted.Eagerly, replay = 1)

    val latestSubmission: Flow<TextSubmission> =
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
            .shareIn(viewModelScope, SharingStarted.Eagerly, replay = 1)

    val latestResult: Flow<Result?> = latestSubmission.map { it.results.orEmpty().lastOrNull() }

    init {
        viewModelScope.launch {
            updateText(serverText.first())
        }
    }

    fun updateText(newText: String) {
        _text.value = newText
    }

    fun retrySync() {
        retrySync.tryEmit(Unit)
    }

    /**
     * Try to submit changes.
     * @return true on success
     */
    private suspend fun submitChanges(
        initialSubmission: TextSubmission,
        text: String
    ): NetworkResponse<TextSubmission> {
        val serverUrl = serverConfigurationService.serverUrl.first()
        val authToken = when (val authData = accountService.authenticationData.first()) {
            is AccountService.AuthenticationData.LoggedIn -> authData.authToken
            AccountService.AuthenticationData.NotLoggedIn -> return NetworkResponse.Failure(
                RuntimeException()
            )
        }

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
                exerciseId = exerciseId,
                serverUrl = serverUrl,
                authToken = authToken,
            )
        }
    }
}