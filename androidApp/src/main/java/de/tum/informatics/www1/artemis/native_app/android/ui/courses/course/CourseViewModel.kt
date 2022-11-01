package de.tum.informatics.www1.artemis.native_app.android.ui.courses.course

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.tum.informatics.www1.artemis.native_app.android.content.Course
import de.tum.informatics.www1.artemis.native_app.android.content.exercise.Exercise
import de.tum.informatics.www1.artemis.native_app.android.service.AccountService
import de.tum.informatics.www1.artemis.native_app.android.service.CourseService
import de.tum.informatics.www1.artemis.native_app.android.service.NetworkStatusProvider
import de.tum.informatics.www1.artemis.native_app.android.service.ServerCommunicationProvider
import de.tum.informatics.www1.artemis.native_app.android.service.exercises.ExerciseService
import de.tum.informatics.www1.artemis.native_app.android.service.exercises.ParticipationService
import de.tum.informatics.www1.artemis.native_app.android.util.DataState
import de.tum.informatics.www1.artemis.native_app.android.util.retryOnInternet
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import java.time.temporal.WeekFields
import java.util.Locale


class CourseViewModel(
    private val courseId: Int,
    private val serverCommunicationProvider: ServerCommunicationProvider,
    private val accountService: AccountService,
    private val networkStatusProvider: NetworkStatusProvider,
    private val courseService: CourseService,
    private val exerciseService: ExerciseService,
    private val participationService: ParticipationService
) : ViewModel() {

    private val requestReloadCourse = MutableSharedFlow<Unit>()

    val course: Flow<DataState<Course>> =
        combine(
            serverCommunicationProvider.serverUrl,
            accountService.authenticationData,
            requestReloadCourse.onStart { emit(Unit) }
        ) { serverUrl, authData, _ ->
            serverUrl to authData
        }
            .transformLatest { (serverUrl, authData) ->
                when (authData) {
                    is AccountService.AuthenticationData.LoggedIn -> {
                        emitAll(
                            retryOnInternet(
                                networkStatusProvider.currentNetworkStatus
                            ) {
                                val r =
                                    courseService.getCourse(courseId, serverUrl, authData.authToken)
                                r
                            })
                    }
                    AccountService.AuthenticationData.NotLoggedIn -> {
                        emit(DataState.Suspended())
                    }
                }
            }
            .stateIn(viewModelScope, SharingStarted.Eagerly, DataState.Loading())

    /**
     * Holds a flow of the latest participation status for each exercise (associated by the exercise id)
     */
    private val exerciseWithParticipationStatusFlow: Flow<DataState<List<ExerciseWithParticipationStatus>>> =
        course
            .map { courseDataState -> courseDataState.bind { it.exercises } }
            .transformLatest { exercisesDataState ->
                when (exercisesDataState) {
                    is DataState.Success -> {
                        val exercises = exercisesDataState.data
                        val exercisesById = exercises.associateBy { it.id }.toMutableMap()

                        val participationStatusMap =
                            exercises
                                .associate {
                                    (it.id ?: 0) to ExerciseWithParticipationStatus(
                                        it,
                                        it.computeParticipationStatus(null)
                                    )
                                }
                                .toMutableMap()

                        emit(DataState.Success(participationStatusMap.values.toList()))

                        participationService
                            .personalSubmissionUpdater
                            .collect { latestSubmission ->
                                //Find the associated exercise, so that the submissions can be updated.
                                val participation = latestSubmission.participation

                                val associatedExerciseId =
                                    participation?.exercise?.id ?: return@collect
                                val associatedExercise =
                                    exercisesById[associatedExerciseId] ?: return@collect

                                val currentAssociatedExerciseParticipations =
                                    associatedExercise.studentParticipations

                                val updatedParticipations =
                                    //Replace the updated participation
                                    currentAssociatedExerciseParticipations?.map { oldParticipation ->
                                        if (oldParticipation.id == participation.id) {
                                            participation
                                        } else {
                                            oldParticipation
                                        }
                                    }
                                        ?: //The new participations are just the one we just received
                                        listOf(participation)

                                //Replace the exercise
                                val newExercise = associatedExercise.copyWithUpdatedParticipations(
                                    updatedParticipations
                                )
                                exercisesById[associatedExerciseId] = newExercise

                                participationStatusMap[associatedExerciseId] =
                                    ExerciseWithParticipationStatus(
                                        newExercise,
                                        newExercise.computeParticipationStatus(null)
                                    )
                                emit(DataState.Success(participationStatusMap.values.toList()))
                            }
                    }
                    else -> emit(exercisesDataState.bind { emptyList() })
                }
            }

    val exercisesGroupedByWeek: Flow<DataState<List<WeeklyExercises>>> =
        exerciseWithParticipationStatusFlow.map { exercisesDataState ->
            exercisesDataState.bind { exercisesWithParticipationState ->
                exercisesWithParticipationState
                    // Group the exercise based on their start of the week day (most likely monday)
                    .groupBy { exerciseWithParticipationState ->
                        val exercise = exerciseWithParticipationState.exercise
                        val releaseDate = exercise.dueDate ?: return@groupBy null

                        releaseDate
                            .toLocalDateTime(TimeZone.currentSystemDefault())
                            .date
                            .toJavaLocalDate()
                            .with(WeekFields.of(Locale.getDefault()).firstDayOfWeek)
                            .toKotlinLocalDate()
                    }
                    .map { (firstDayOfWeek, exercises) ->
                        if (firstDayOfWeek != null) {
                            val lastDayOfWeek = firstDayOfWeek.plus(6, DateTimeUnit.DAY)
                            WeeklyExercises.BoundToWeek(firstDayOfWeek, lastDayOfWeek, exercises)
                        } else WeeklyExercises.Unbound(exercises)
                    }
                    .sortedBy { weeklyExercise ->
                        when (weeklyExercise) {
                            is WeeklyExercises.BoundToWeek -> weeklyExercise.firstDayOfWeek
                            is WeeklyExercises.Unbound -> LocalDate(9999, 1, 1)
                        }
                    }
            }
        }
            .stateIn(viewModelScope, SharingStarted.Lazily, DataState.Loading())

//    /**
//     * The tabs that are displayed for this course.
//     */
//    val courseTabs: Flow<List<CourseTab>> = flowOf(
//        (if (course.exercises.isNotEmpty()) listOf(CourseTab.Exercises(course.exercises)) else emptyList())
//                + (if (course.lectures.isNotEmpty()) listOf(CourseTab.Lectures(course.lectures)) else emptyList())
//                + listOf(CourseTab.Statistics, CourseTab.Communication)
//    )

    fun reloadCourse() {
        viewModelScope.launch {
            requestReloadCourse.emit(Unit)
        }
    }

}