package de.tum.informatics.www1.artemis.native_app.feature.courseview.ui

import androidx.lifecycle.viewModelScope
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.service.artemis_context.performAutoReloadingNetworkCall
import de.tum.informatics.www1.artemis.native_app.core.data.service.network.CourseExerciseService
import de.tum.informatics.www1.artemis.native_app.core.data.service.network.CourseService
import de.tum.informatics.www1.artemis.native_app.core.device.NetworkStatusProvider
import de.tum.informatics.www1.artemis.native_app.core.model.Course
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.Exercise
import de.tum.informatics.www1.artemis.native_app.core.model.lecture.Lecture
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.BaseExerciseListViewModel
import de.tum.informatics.www1.artemis.native_app.core.websocket.LiveParticipationService
import de.tum.informatics.www1.artemis.native_app.feature.courseview.TimeFrame
import de.tum.informatics.www1.artemis.native_app.feature.courseview.TimeFrameUtils.groupByTimeFrame
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

class CourseViewModel(
    private val courseId: Long,
    courseService: CourseService,
    private val liveParticipationService: LiveParticipationService,
    courseExerciseService: CourseExerciseService,
    networkStatusProvider: NetworkStatusProvider,
    coroutineContext: CoroutineContext = EmptyCoroutineContext
) : BaseExerciseListViewModel(courseExerciseService) {

    private val _exerciseQuery = MutableStateFlow("")
    val exerciseQuery: StateFlow<String> = _exerciseQuery

    private val _lectureQuery = MutableStateFlow("")
    val lectureQuery: StateFlow<String> = _lectureQuery

    val course: StateFlow<DataState<Course>> = courseService
        .performAutoReloadingNetworkCall(
            networkStatusProvider = networkStatusProvider,
            manualReloadFlow = requestReload
        ) {
            getCourse(courseId)
                .bind { it.course }
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

    internal val exercisesTimeFrame: StateFlow<DataState<List<TimeFrame<Exercise>>>> =
        combine(exerciseWithParticipationStatusFlow, exerciseQuery) { exercisesDataState, query ->
            exercisesDataState.bind { exercises ->
                exercises
                    .filter { exercise -> query.isBlank() || exercise.title?.contains(query, ignoreCase = true) ?: false }
                    .groupByTimeFrame(
                        getStartDate = { it.releaseDate },
                        getEndDate = { it.dueDate },
                        isDueSoon = { ex ->
                            // Example: "due soon" if due - now < 3 days
                            val now = kotlinx.datetime.Clock.System.now()
                            val due = ex.dueDate ?: return@groupByTimeFrame false
                            due > now && (due - now).inWholeDays <= 3
                        }
                    )
            }
        }
            .flowOn(coroutineContext)
            .stateIn(viewModelScope, SharingStarted.Lazily, DataState.Loading())

    internal val lecturesTimeFrame: StateFlow<DataState<List<TimeFrame<Lecture>>>> =
        combine(course, _lectureQuery) { courseDataState, query ->
            courseDataState.bind { course ->
                course
                    .lectures
                    .filter { lecture -> query.isBlank() || lecture.title.contains(query, ignoreCase = true) }
                    .groupByTimeFrame(
                        getStartDate = { it.startDate },
                        getEndDate = { it.endDate },
                        isDueSoon = { false }
                    )
            }
        }
            .flowOn(coroutineContext)
            .stateIn(viewModelScope, SharingStarted.Lazily, DataState.Loading())

    fun onUpdateExerciseQuery(query: String) {
        _exerciseQuery.value = query
    }

    fun onUpdateLectureQuery(query: String) {
        _lectureQuery.value = query
    }
}
