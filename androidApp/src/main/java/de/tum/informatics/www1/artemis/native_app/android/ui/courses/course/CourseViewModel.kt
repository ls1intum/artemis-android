package de.tum.informatics.www1.artemis.native_app.android.ui.courses.course

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.tum.informatics.www1.artemis.native_app.android.content.Course
import de.tum.informatics.www1.artemis.native_app.android.content.exercise.Exercise
import de.tum.informatics.www1.artemis.native_app.android.service.AccountService
import de.tum.informatics.www1.artemis.native_app.android.service.CourseService
import de.tum.informatics.www1.artemis.native_app.android.service.NetworkStatusProvider
import de.tum.informatics.www1.artemis.native_app.android.service.ServerCommunicationProvider
import de.tum.informatics.www1.artemis.native_app.android.util.DataState
import de.tum.informatics.www1.artemis.native_app.android.util.retryOnInternet
import kotlinx.coroutines.flow.*
import kotlinx.datetime.*
import java.time.temporal.WeekFields
import java.util.Locale


class CourseViewModel(
    private val courseId: Int,
    serverCommunicationProvider: ServerCommunicationProvider,
    accountService: AccountService,
    private val networkStatusProvider: NetworkStatusProvider,
    private val courseService: CourseService
) : ViewModel() {

    val course: Flow<DataState<Course>> =
        combine(
            serverCommunicationProvider.serverUrl,
            accountService.authenticationData
        ) { serverUrl, authData ->
            serverUrl to authData
        }
            .transformLatest { (serverUrl, authData) ->
                when (authData) {
                    is AccountService.AuthenticationData.LoggedIn -> {
                        emitAll(
                            retryOnInternet(
                                networkStatusProvider.currentNetworkStatus,
                                minimumLoadingMillis = 4000
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

    val exercises: Flow<DataState<List<WeeklyExercises>>> = course.map { courseDataState ->
        courseDataState.bind { course ->
            course
                .exercises
                // Group the exercise based on their start of the week day (most likely monday)
                .groupBy { exercise ->
                    val releaseDate = exercise.releaseDate ?: return@groupBy null

                    releaseDate
                        .toLocalDateTime(TimeZone.currentSystemDefault())
                        .date
                        .toJavaLocalDate()
                        .with(WeekFields.of(Locale.getDefault()).firstDayOfWeek)
                        .toKotlinLocalDate()
                }
                .map { (firstDayOfWeek, exercises) ->
                    if (firstDayOfWeek != null) {
                        val lastDayOfWeek = firstDayOfWeek.plus(7, DateTimeUnit.DAY)
                        WeeklyExercises.BoundToWeek(firstDayOfWeek, lastDayOfWeek, exercises)
                    } else WeeklyExercises.Unbound(exercises)
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
}