package de.tum.informatics.www1.artemis.native_app.feature.exercise_view

import android.annotation.SuppressLint
import android.content.Context
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.tum.informatics.www1.artemis.native_app.core.common.flatMapLatest
import de.tum.informatics.www1.artemis.native_app.core.data.*
import de.tum.informatics.www1.artemis.native_app.core.data.service.BuildLogService
import de.tum.informatics.www1.artemis.native_app.core.data.service.CourseExerciseService
import de.tum.informatics.www1.artemis.native_app.core.data.service.ExerciseService
import de.tum.informatics.www1.artemis.native_app.core.data.service.ResultService
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.datastore.authToken
import de.tum.informatics.www1.artemis.native_app.core.device.NetworkStatusProvider
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.Exercise
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.ProgrammingExercise
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.feedback.Feedback
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.participation.Participation
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.BuildLogEntry
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.ProgrammingSubmission
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.Result
import de.tum.informatics.www1.artemis.native_app.core.ui.authTokenStateFlow
import de.tum.informatics.www1.artemis.native_app.core.ui.serverUrlStateFlow
import de.tum.informatics.www1.artemis.native_app.core.websocket.LiveParticipationService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant

// it does not actually leak, as context will be the application context, which cannot be leaked.
@SuppressLint("StaticFieldLeak")
internal class ExerciseViewModel(
    private val exerciseId: Long,
    private val serverConfigurationService: ServerConfigurationService,
    private val accountService: AccountService,
    private val exerciseService: ExerciseService,
    private val liveParticipationService: LiveParticipationService,
    private val resultService: ResultService,
    private val buildLogService: BuildLogService,
    private val courseExerciseService: CourseExerciseService,
    private val networkStatusProvider: NetworkStatusProvider,
    private val context: Context
) : ViewModel() {

    private val requestReloadExercise = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    /**
     * Flow that holds (serverUrl, authData) and emits every time the exercise should be reloaded.
     */
    private val baseConfigurationFlow: Flow<Pair<String, String>> =
        combine(
            serverConfigurationService.serverUrl,
            accountService.authToken,
            requestReloadExercise.onStart { emit(Unit) }
        ) { serverUrl, authData, _ ->
            serverUrl to authData
        }
            .shareIn(viewModelScope, SharingStarted.Lazily, replay = 1)

    private val fetchedExercise: Flow<DataState<Exercise>> =
        baseConfigurationFlow
            .flatMapLatest { (serverUrl, authToken) ->
                retryOnInternet(networkStatusProvider.currentNetworkStatus) {
                    exerciseService.getExerciseDetails(
                        exerciseId,
                        serverUrl,
                        authToken
                    )
                }
            }
            .stateIn(viewModelScope, SharingStarted.Eagerly, DataState.Loading())

    /**
     * Emitted to when startExercise is successful
     */
    private val _gradedParticipation = MutableSharedFlow<Participation>()

    /**
     * Emits the exercise updated with the latest participations and results
     */
    val exerciseDataState: StateFlow<DataState<Exercise>> =
        fetchedExercise
            .transformLatest { exercise ->
                when (exercise) {
                    is DataState.Success -> {
                        var currentExercise = exercise.data

                        emit(exercise)

                        val newParticipationsFlow =
                            merge(
                                liveParticipationService
                                    .personalSubmissionUpdater.filter { result -> result.participation?.exercise?.id == exerciseId }
                                    .mapNotNull { it.participation },
                                _gradedParticipation
                            )

                        newParticipationsFlow.collect { participation ->
                            currentExercise =
                                currentExercise.withUpdatedParticipation(participation)

                            emit(DataState.Success(currentExercise))
                        }
                    }

                    else -> emit(exercise)
                }
            }
            .stateIn(viewModelScope, SharingStarted.Eagerly)

    val latestIndividualDueDate: StateFlow<DataState<Instant?>> =
        baseConfigurationFlow
            .flatMapLatest { (serverUrl, authToken) ->
                retryOnInternet(networkStatusProvider.currentNetworkStatus) {
                    exerciseService.getLatestDueDate(
                        exerciseId,
                        serverUrl,
                        authToken
                    )
                }
            }
            .stateIn(viewModelScope, SharingStarted.Lazily)

    /**
     * The latest result for the exercise.
     */
    val latestResultDataState: StateFlow<DataState<Result?>> =
        exerciseDataState.map { exerciseData ->
            exerciseData.bind { exercise ->
                val participation =
                    exercise.studentParticipations.orEmpty().firstOrNull()

                participation?.results.orEmpty().sortedByDescending { it.completionDate }
                    .firstOrNull()
            }
        }
            .stateIn(viewModelScope, SharingStarted.Lazily)

    val feedbackItems: StateFlow<DataState<List<FeedbackItem>>> =
        flatMapLatest(
            exerciseDataState,
            latestResultDataState,
            serverConfigurationService.serverUrl,
            accountService.authToken
        ) { exerciseDataState, latestResultDataState, serverUrl, authToken ->
            when (val joinedDataState = exerciseDataState join latestResultDataState) {
                is DataState.Success -> {
                    val (exercise, latestResult) = joinedDataState.data
                    if (latestResult != null) {
                        val feedbacks = latestResult.feedbacks
                        if (feedbacks == null) {
                            // Load feedback

                            val participation =
                                exercise.studentParticipations.orEmpty().firstOrNull()

                            retryOnInternet(networkStatusProvider.currentNetworkStatus) {
                                resultService.getFeedbackDetailsForResult(
                                    latestResult.participation?.id ?: participation?.id ?: 0,
                                    resultId = latestResult.id ?: 0,
                                    serverUrl = serverUrl,
                                    authToken = authToken
                                )
                            }
                                .map { feedbackDataState ->
                                    feedbackDataState.bind { feedback ->
                                        createFeedbackItems(exercise, feedback)
                                    }
                                }
                        } else {
                            flowOf(
                                DataState.Success(
                                    createFeedbackItems(
                                        exercise,
                                        feedbacks
                                    )
                                )

                            )
                        }
                    } else emptyFlow()

                }
                else -> {
                    flowOf(joinedDataState.bind { emptyList<FeedbackItem>() })
                }
            }
        }
            .stateIn(viewModelScope, SharingStarted.Lazily)

    /**
     * Fetch the build logs only if the exercise is loaded and the exercise is a programming exercise.
     * Furthermore, the loaded exercise must already have a submission.
     */
    val buildLogs: StateFlow<DataState<List<BuildLogEntry>>> =
        exerciseDataState
            .map { exerciseDataState -> exerciseDataState.bind<Exercise?> { it }.orElse(null) }
            .filterNotNull()
            .filterIsInstance<ProgrammingExercise>()
            .flatMapLatest { exercise ->
                val participationId = exercise.studentParticipations.orEmpty().firstOrNull()?.id
                    ?: return@flatMapLatest emptyFlow()

                latestResultDataState
                    .filter {
                        if (it !is DataState.Success) return@filter false
                        val submission = it.data?.submission
                        submission is ProgrammingSubmission && submission.buildFailed == true
                    }
                    .transformLatest { latestResult ->
                        val resultId = latestResult.bind { it?.id }.orElse(null)

                        //This is reexecuted when exercise is reloaded anyway. And exercise is reloaded
                        //When server url or auth data changes.
                        val serverUrl = serverConfigurationService.serverUrl.first()
                        val authToken = accountService.authToken.first()

                        emitAll(
                            buildLogService
                                .loadBuildLogs(
                                    participationId,
                                    resultId,
                                    serverUrl,
                                    authToken
                                )
                        )
                    }
            }
            .stateIn(viewModelScope, SharingStarted.Lazily)

    val serverUrl: StateFlow<String> = serverUrlStateFlow(serverConfigurationService)
    val authToken: StateFlow<String> = authTokenStateFlow(accountService)

    fun requestReloadExercise() {
        requestReloadExercise.tryEmit(Unit)
    }

    private fun createFeedbackItems(
        exercise: Exercise,
        feedbackList: List<Feedback>
    ): List<FeedbackItem> {
        val showTestDetails =
            exercise is ProgrammingExercise && exercise.showTestNamesToStudents == true

        return if (exercise is ProgrammingExercise) {
            feedbackList.map { createProgrammingExerciseFeedbackItem(it, showTestDetails) }
        } else {
            feedbackList.map { feedback ->
                FeedbackItem(
                    type = FeedbackItemType.Feedback,
                    category = R.string.result_view_feedback_category_regular,
                    title = feedback.text,
                    text = feedback.detailText,
                    positive = feedback.positive,
                    credits = feedback.credits,
                    actualCredits = null
                )
            }
        }
    }

    private fun createProgrammingExerciseFeedbackItem(
        feedback: Feedback,
        showTestDetails: Boolean
    ): FeedbackItem {
        return when {
            feedback.isSubmissionPolicy -> {
                createProgrammingExerciseSubmissionPolicyFeedbackItem(feedback)
            }

            feedback.isStaticCodeAnalysis -> {
                createProgrammingExerciseScaFeedbackItem(feedback, showTestDetails)
            }

            feedback.type == Feedback.FeedbackCreationType.AUTOMATIC -> {
                createProgrammingExerciseAutomaticFeedbackItem(feedback, showTestDetails)
            }

            (feedback.type == Feedback.FeedbackCreationType.MANUAL
                    || feedback.type == Feedback.FeedbackCreationType.MANUAL_UNREFERENCED)
                    && feedback.gradingInstruction != null -> {
                createProgrammingExerciseGradingInstructionFeedbackItem(feedback, showTestDetails)
            }

            else -> {
                createProgrammingExerciseTutorFeedbackItem(feedback, showTestDetails)
            }
        }
    }

    private fun createProgrammingExerciseSubmissionPolicyFeedbackItem(
        feedback: Feedback
    ): FeedbackItem {
        return FeedbackItem(
            type = FeedbackItemType.Policy,
            category = R.string.result_view_feedback_category_submission_policy,
            title = feedback.submissionPolicyTitle,
            text = feedback.detailText,
            positive = false,
            credits = feedback.credits,
            actualCredits = null
        )
    }

    private fun createProgrammingExerciseScaFeedbackItem(
        feedback: Feedback,
        showTestDetails: Boolean
    ): FeedbackItem {
        val issue = feedback.staticCodeAnalysisIssue

        val penalty = issue.penalty
        return FeedbackItem(
            type = FeedbackItemType.Issue,
            category = R.string.result_view_feedback_category_submission_code_issue,
            title = "",
            text = if (showTestDetails) "${issue.rule}: ${issue.message}" else issue.message,
            positive = false,
            credits = if (penalty != null) -penalty else feedback.credits,
            actualCredits = feedback.credits
        )
    }

    /**
     * Creates a feedback item from a feedback generated from an automatic test case result.
     */
    private fun createProgrammingExerciseAutomaticFeedbackItem(
        feedback: Feedback,
        showTestDetails: Boolean
    ): FeedbackItem {
        val title =
            if (showTestDetails) {
                val positive = feedback.positive
                context.getString(
                    if (positive == null) {
                        R.string.result_view_feedback_test_title_no_info
                    } else {
                        if (positive) {
                            R.string.result_view_feedback_test_title_passed
                        } else {
                            R.string.result_view_feedback_test_title_failed
                        }
                    }, feedback.text
                )
            } else ""

        return FeedbackItem(
            type = FeedbackItemType.Test,
            category = if (showTestDetails) R.string.result_view_feedback_category_submission_test
            else R.string.result_view_feedback_category_regular,
            title = title,
            text = feedback.detailText,
            positive = feedback.positive,
            credits = feedback.credits,
            actualCredits = null
        )
    }

    /**
     * Creates a feedback item for a manual feedback where the tutor used a grading instruction.
     */
    private fun createProgrammingExerciseGradingInstructionFeedbackItem(
        feedback: Feedback,
        showTestDetails: Boolean
    ): FeedbackItem {
        return FeedbackItem(
            type = FeedbackItemType.Feedback,
            category = if (showTestDetails) R.string.result_view_feedback_category_submission_tutor
            else R.string.result_view_feedback_category_regular,
            title = feedback.text,
            text = feedback.gradingInstruction?.feedback.orEmpty() + (feedback.detailText ?: ""),
            positive = feedback.positive,
            credits = feedback.credits,
            actualCredits = null
        )
    }

    /**
     * Creates a feedback item for a regular tutor feedback not using a grading instruction.
     */
    private fun createProgrammingExerciseTutorFeedbackItem(
        feedback: Feedback,
        showTestDetails: Boolean
    ): FeedbackItem {
        return FeedbackItem(
            type = FeedbackItemType.Feedback,
            category = if (showTestDetails) R.string.result_view_feedback_category_submission_tutor
            else R.string.result_view_feedback_category_regular,
            title = feedback.text,
            text = feedback.detailText,
            positive = feedback.positive,
            credits = feedback.credits,
            actualCredits = null
        )
    }

    fun startExercise(onStartedSuccessfully: (participationId: Long) -> Unit) {
        viewModelScope.launch {
            val serverUrl = serverConfigurationService.serverUrl.first()
            val authToken = accountService.authToken.first()

            val response = courseExerciseService.startExercise(
                exerciseId,
                serverUrl,
                authToken
            )

            when (response) {
                is NetworkResponse.Response -> {
                    val participation = response.data

                    _gradedParticipation.emit(participation)
                    onStartedSuccessfully(participation.id ?: return@launch)
                }
                is NetworkResponse.Failure -> {}
            }
        }
    }

    enum class FeedbackItemType {
        Issue,
        Test,
        Feedback,
        Policy,
        Subsequent,
    }

    data class FeedbackItem(
        val type: FeedbackItemType,
        @StringRes val category: Int,
        val title: String?, // this is typically feedback.text
        val text: String?, // this is typically feedback.detailText
        val positive: Boolean?,
        val credits: Float?,
        val actualCredits: Float?
    ) {
        val creditsOrZero: Double = credits?.toDouble() ?: 0.0
        val actualCreditsOrZero: Double = actualCredits?.toDouble() ?: 0.0
    }
}