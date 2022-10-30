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
    private val exerciseService: ExerciseService
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

    val exercisesGroupedByWeek: Flow<DataState<List<WeeklyExercises>>> =
        course.map { courseDataState ->
            courseDataState.bind { course ->
                course
                    .exercises
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

    /**
     * @return a flow that fetches the details for the exercise with the provided it.
     * Automatically retries and refreshes on changes.
     */
    fun getLoadExerciseDetailsFlow(exerciseId: Int): Flow<DataState<Exercise>> {
        return combine(
            serverCommunicationProvider.serverUrl,
            accountService.authenticationData,
            requestReloadCourse.onStart { emit(Unit) }) { a, b, _ -> a to b }
            .transformLatest { (serverUrl, authData) ->
                when (authData) {
                    is AccountService.AuthenticationData.LoggedIn -> {
                        emitAll(
                            retryOnInternet(
                                connectivity = networkStatusProvider.currentNetworkStatus
                            ) {
                                exerciseService
                                    .getExerciseDetails(exerciseId, serverUrl, authData.authToken)
                            }
                        )
                    }
                    AccountService.AuthenticationData.NotLoggedIn -> emit(DataState.Suspended())
                }
            }
    }

    fun reloadCourse() {
        viewModelScope.launch {
            requestReloadCourse.emit(Unit)
        }
    }
}