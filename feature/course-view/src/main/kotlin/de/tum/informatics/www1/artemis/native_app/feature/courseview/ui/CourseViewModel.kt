package de.tum.informatics.www1.artemis.native_app.feature.courseview.ui

import androidx.lifecycle.viewModelScope
import de.tum.informatics.www1.artemis.native_app.core.common.flatMapLatest
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.retryOnInternet
import de.tum.informatics.www1.artemis.native_app.core.data.service.network.CourseExerciseService
import de.tum.informatics.www1.artemis.native_app.core.data.service.network.CourseService
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.datastore.authToken
import de.tum.informatics.www1.artemis.native_app.core.device.NetworkStatusProvider
import de.tum.informatics.www1.artemis.native_app.core.model.Course
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.Exercise
import de.tum.informatics.www1.artemis.native_app.core.model.lecture.Lecture
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.BaseExerciseListViewModel
import de.tum.informatics.www1.artemis.native_app.core.websocket.LiveParticipationService
import de.tum.informatics.www1.artemis.native_app.feature.courseview.GroupedByWeek
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toKotlinLocalDate
import kotlinx.datetime.toLocalDateTime
import java.time.temporal.WeekFields
import java.util.Locale
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

internal class CourseViewModel(
    private val courseId: Long,
    private val courseService: CourseService,
    private val liveParticipationService: LiveParticipationService,
    serverConfigurationService: ServerConfigurationService,
    accountService: AccountService,
    courseExerciseService: CourseExerciseService,
    networkStatusProvider: NetworkStatusProvider,
    coroutineContext: CoroutineContext = EmptyCoroutineContext
) : BaseExerciseListViewModel(serverConfigurationService, accountService, courseExerciseService) {

    private val requestReloadCourse = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    val course: StateFlow<DataState<Course>> =
        flatMapLatest(
            serverConfigurationService.serverUrl,
            accountService.authToken,
            requestReloadCourse.onStart { emit(Unit) }
        ) { serverUrl, authToken, _ ->
            retryOnInternet(networkStatusProvider.currentNetworkStatus) {
                courseService
                    .getCourse(courseId, serverUrl, authToken)
                    .bind { it.course }
            }
        }
            .flowOn(coroutineContext)
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
                                .associateBy { exercise -> exercise.id }
                                .toMutableMap()

                        emit(DataState.Success(participationStatusMap.values.toList()))

                        merge(
                            liveParticipationService
                                .personalSubmissionUpdater
                                .mapFilterToNewParticipationData(),
                            startedExerciseFlow
                        ).collect { newParticipationData ->
                            //Find the associated exercise, so that the submissions can be updated.
                            val participation = newParticipationData.newParticipation

                            val associatedExerciseId = newParticipationData.exerciseId
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

    val exercisesGroupedByWeek: StateFlow<DataState<List<GroupedByWeek<Exercise>>>> =
        exerciseWithParticipationStatusFlow.map { exercisesDataState ->
            exercisesDataState.bind { exercisesWithParticipationState ->
                exercisesWithParticipationState
                    .filter { it.visibleToStudents != false }
                    .groupByWeek(compareBy(Exercise::dueDate)) { dueDate }
            }
        }
            .flowOn(coroutineContext)
            .stateIn(viewModelScope, SharingStarted.Lazily, DataState.Loading())

    val lecturesGroupedByWeek: StateFlow<DataState<List<GroupedByWeek<Lecture>>>> =
        course.map { courseDataState ->
            courseDataState.bind { course ->
                course
                    .lectures
                    .groupByWeek(compareBy(Lecture::title)) { startDate }
            }
        }
            .flowOn(coroutineContext)
            .stateIn(viewModelScope, SharingStarted.Lazily, DataState.Loading())

    private fun <T> List<T>.groupByWeek(
        comparator: Comparator<T>,
        getSortDate: T.() -> Instant?
    ): List<GroupedByWeek<T>> =
        // Group the items based on their start of the week day (most likely monday)
        groupBy { item ->
            val sortDate = getSortDate(item) ?: return@groupBy null

            sortDate
                .toLocalDateTime(TimeZone.currentSystemDefault())
                .date
                .toJavaLocalDate()
                .with(WeekFields.of(Locale.getDefault()).firstDayOfWeek)
                .toKotlinLocalDate()
        }
            .map { (firstDayOfWeek, items) ->
                val sortedItems = items.sortedWith(comparator)

                if (firstDayOfWeek != null) {
                    val lastDayOfWeek = firstDayOfWeek.plus(6, DateTimeUnit.DAY)
                    GroupedByWeek.BoundToWeek(firstDayOfWeek, lastDayOfWeek, sortedItems)
                } else GroupedByWeek.Unbound(sortedItems)
            }
            .sortedBy { itemsBoundByWeek ->
                when (itemsBoundByWeek) {
                    is GroupedByWeek.BoundToWeek -> itemsBoundByWeek.firstDayOfWeek
                    is GroupedByWeek.Unbound -> LocalDate(9999, 1, 1)
                }
            }

    fun reloadCourse() {
        requestReloadCourse.tryEmit(Unit)
    }
}
