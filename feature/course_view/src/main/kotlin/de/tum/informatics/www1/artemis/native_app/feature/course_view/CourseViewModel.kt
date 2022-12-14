package de.tum.informatics.www1.artemis.native_app.feature.course_view

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.tum.informatics.www1.artemis.native_app.core.model.Course
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.service.CourseService
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.Exercise
import de.tum.informatics.www1.artemis.native_app.core.websocket.ParticipationService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import java.time.temporal.WeekFields
import java.util.Locale


internal class CourseViewModel(
    private val courseId: Int,
    private val serverConfigurationService: ServerConfigurationService,
    private val accountService: AccountService,
    private val courseService: CourseService,
    private val participationService: ParticipationService
) : ViewModel() {

    private val requestReloadCourse = MutableSharedFlow<Unit>()

    val course: Flow<DataState<Course>> =
        combine(
            serverConfigurationService.serverUrl,
            accountService.authenticationData,
            requestReloadCourse.onStart { emit(Unit) }
        ) { serverUrl, authData, _ ->
            serverUrl to authData
        }
            .transformLatest { (serverUrl, authData) ->
                when (authData) {
                    is AccountService.AuthenticationData.LoggedIn -> {
                        emitAll(
                            courseService.getCourse(courseId, serverUrl, authData.authToken)
                        )
                    }

                    AccountService.AuthenticationData.NotLoggedIn -> {
                        emit(DataState.Suspended())
                    }
                }
            }
            .stateIn(viewModelScope, SharingStarted.Eagerly, DataState.Loading())

    /**
     * Holds a flow of the latest exercises. Updated by the websocket.
     */
    private val exerciseWithParticipationStatusFlow: Flow<DataState<List<Exercise>>> =
        course
            .map { courseDataState -> courseDataState.bind { it.exercises } }
            .transformLatest { exercisesDataState ->
                when (exercisesDataState) {
                    is DataState.Success -> {
                        val exercises = exercisesDataState.data
                        val exercisesById = exercises.associateBy { it.id }.toMutableMap()

                        val participationStatusMap =
                            exercises
                                .associateBy { exercise -> (exercise.id ?: 0) }
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

                                //Replace the exercise
                                val newExercise =
                                    associatedExercise.withUpdatedParticipation(participation)
                                exercisesById[associatedExerciseId] = newExercise

                                participationStatusMap[associatedExerciseId] = newExercise
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
                    .groupBy { exercise ->
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