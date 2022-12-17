package de.tum.informatics.www1.artemis.native_app.feature.exercise_view

import android.annotation.SuppressLint
import android.content.Context
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.Exercise
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.filterSuccess
import de.tum.informatics.www1.artemis.native_app.core.data.isSuccess
import de.tum.informatics.www1.artemis.native_app.core.data.service.BuildLogService
import de.tum.informatics.www1.artemis.native_app.core.data.service.CourseExerciseService
import de.tum.informatics.www1.artemis.native_app.core.data.service.ExerciseService
import de.tum.informatics.www1.artemis.native_app.core.data.service.ResultService
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.ProgrammingExercise
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.feedback.Feedback
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.participation.Participation
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.participation.StudentParticipation
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.BuildLogEntry
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.ProgrammingSubmission
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.Result
import de.tum.informatics.www1.artemis.native_app.core.websocket.ParticipationService
import kotlinx.coroutines.coroutineScope
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
    private val participationService: ParticipationService,
    private val resultService: ResultService,
    private val buildLogService: BuildLogService,
    private val courseExerciseService: CourseExerciseService,
    private val context: Context
) : ViewModel() {

    private val requestReloadExercise = MutableSharedFlow<Unit>()

    /**
     * Flow that holds (serverUrl, authData) and emits every time the exercise should be reloaded.
     */
    private val baseConfigurationFlow: Flow<Pair<String, AccountService.AuthenticationData>> =
        combine(
            serverConfigurationService.serverUrl,
            accountService.authenticationData,
            requestReloadExercise.onStart { emit(Unit) }
        ) { serverUrl, authData, _ ->
            serverUrl to authData
        }
            .shareIn(viewModelScope, SharingStarted.Lazily, replay = 1)

    private val fetchedExercise: Flow<DataState<Exercise>> =
        baseConfigurationFlow
            .transformLatest { (serverUrl, authData) ->
                when (authData) {
                    is AccountService.AuthenticationData.LoggedIn -> {
                        emitAll(
                            exerciseService.getExerciseDetails(
                                exerciseId,
                                serverUrl,
                                authData.authToken
                            )
                        )
                    }

                    AccountService.AuthenticationData.NotLoggedIn -> {
                        emit(DataState.Suspended())
                    }
                }
            }
            .stateIn(viewModelScope, SharingStarted.Eagerly, DataState.Loading())

    /**
     * Emits the exercise updated with the latest participations and results
     */
    val exercise: StateFlow<DataState<Exercise>> =
        fetchedExercise
            .transformLatest { exercise ->
                when (exercise) {
                    is DataState.Success -> {
                        var currentExercise = exercise.data

                        emit(exercise)

                        val newParticipationsFlow =
                            merge(
                                participationService
                                    .personalSubmissionUpdater.filter { result -> result.participation?.exercise?.id == exerciseId }
                                    .mapNotNull { it.participation },
                                _gradedParticipation
                            )

                        newParticipationsFlow.collect { participation ->
                            currentExercise = currentExercise.withUpdatedParticipation(participation)

                            emit(DataState.Success(currentExercise))
                        }
                    }

                    else -> emit(exercise)
                }
            }
            .stateIn(viewModelScope, SharingStarted.Lazily, DataState.Loading())

    val latestIndividualDueDate: Flow<DataState<Instant?>> =
        baseConfigurationFlow
            .transformLatest { (serverUrl, authData) ->
                when (authData) {
                    is AccountService.AuthenticationData.LoggedIn -> {
                        emitAll(
                            exerciseService.getLatestDueDate(
                                exerciseId,
                                serverUrl,
                                authData.authToken
                            )
                        )
                    }

                    AccountService.AuthenticationData.NotLoggedIn -> {
                        emit(DataState.Suspended())
                    }
                }
            }
            .shareIn(viewModelScope, SharingStarted.Lazily, replay = 1)

    /**
     * The latest result for the exercise.
     */
    val latestResult: Flow<DataState<Result?>> =
        combine(
            exercise,
            serverConfigurationService.serverUrl,
            accountService.authenticationData
        ) { a, b, c -> Triple(a, b, c) }
            .transformLatest { (exerciseData, serverUrl, authData) ->
                when (exerciseData) {
                    is DataState.Success -> {
                        val participation =
                            exerciseData.data.studentParticipations.orEmpty().firstOrNull()
                        val participationId = participation?.id
                        val latestResult = participation?.results.orEmpty().firstOrNull()

                        if (latestResult == null || latestResult.feedbacks.orEmpty().isNotEmpty()) {
                            emit(DataState.Success(latestResult))
                        } else {
                            //Feedback is missing, fetch from server
                            val resultId = latestResult.id
                            if (participationId != null && resultId != null) {
                                when (authData) {
                                    is AccountService.AuthenticationData.LoggedIn -> {
                                        emitAll(
                                            resultService
                                                .getFeedbackDetailsForResult(
                                                    participationId,
                                                    resultId,
                                                    serverUrl,
                                                    authData.authToken
                                                )
                                                .map { feedbackListDataState ->
                                                    feedbackListDataState.bind<Result?> { loadedFeedbacksList ->
                                                        latestResult.copy(feedbacks = loadedFeedbacksList)
                                                    }
                                                }
                                        )
                                    }

                                    AccountService.AuthenticationData.NotLoggedIn -> {
                                        emit(DataState.Suspended())
                                    }
                                }

                            }
                        }
                    }

                    else -> {
                        emit(exerciseData.bind<Result?> { null })
                    }
                }
            }
            .shareIn(viewModelScope, SharingStarted.Lazily, replay = 1)

    val feedbackItems: Flow<DataState<List<FeedbackItem>>> =
        combine(
            exercise,
            latestResult
        ) { a, b -> a to b }
            .map { (exercise, latestResultDataState) ->
                if (exercise.isSuccess) {
                    latestResultDataState.bind {
                        createFeedbackItems(
                            exercise.orThrow(),
                            it?.feedbacks.orEmpty()
                        )
                    }
                } else exercise.bind { emptyList() }
            }

    /**
     * Fetch the build logs only if the exercise is loaded and the exercise is a programming exercise.
     * Furthermore, the loaded exercise must already have a submission.
     */
    val buildLogs: Flow<DataState<List<BuildLogEntry>>> =
        exercise
            .map { exerciseDataState -> exerciseDataState.bind<Exercise?> { it }.orElse(null) }
            .filterNotNull()
            .filterIsInstance<ProgrammingExercise>()
            .transformLatest { exercise ->
                val participationId = exercise.studentParticipations.orEmpty().firstOrNull()?.id
                    ?: return@transformLatest

                latestResult
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
                        val accountData = accountService.authenticationData.first()

                        when (accountData) {
                            is AccountService.AuthenticationData.LoggedIn -> {
                                emitAll(
                                    buildLogService
                                        .loadBuildLogs(
                                            participationId,
                                            resultId,
                                            serverUrl,
                                            accountData.authToken
                                        )
                                )
                            }

                            AccountService.AuthenticationData.NotLoggedIn -> {
                                emit(DataState.Suspended())
                            }
                        }

                    }
            }

    /**
     * Emitted to when startExercise is successful
     */
    private val _gradedParticipation = MutableSharedFlow<Participation>()
    val gradedParticipation: Flow<Participation?> = merge<Participation?>(
        _gradedParticipation,
        exercise
            .filterSuccess()
            .map { exercise ->
                exercise
                    .studentParticipations
                    .orEmpty()
                    .filterIsInstance<StudentParticipation>()
                    .firstOrNull()
            }
            .filterNotNull()
    )

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
            when (val authData = accountService.authenticationData.first()) {
                is AccountService.AuthenticationData.LoggedIn -> {
                    val response = courseExerciseService.startExercise(
                        exerciseId,
                        serverUrl,
                        authData.authToken
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

                AccountService.AuthenticationData.NotLoggedIn -> {}
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