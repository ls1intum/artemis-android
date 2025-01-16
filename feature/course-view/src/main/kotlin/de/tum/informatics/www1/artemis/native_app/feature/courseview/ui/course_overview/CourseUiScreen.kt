package de.tum.informatics.www1.artemis.native_app.feature.courseview.ui.course_overview

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.navDeepLink
import androidx.navigation.toRoute
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.model.Course
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.Exercise
import de.tum.informatics.www1.artemis.native_app.core.model.lecture.Lecture
import de.tum.informatics.www1.artemis.native_app.core.ui.common.BasicDataStateUi
import de.tum.informatics.www1.artemis.native_app.core.ui.common.EmptyDataStateUi
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.BoundExerciseActions
import de.tum.informatics.www1.artemis.native_app.core.ui.generateLinks
import de.tum.informatics.www1.artemis.native_app.core.ui.navigation.DefaultTransition
import de.tum.informatics.www1.artemis.native_app.core.ui.navigation.animatedComposable
import de.tum.informatics.www1.artemis.native_app.feature.courseview.GroupedByWeek
import de.tum.informatics.www1.artemis.native_app.feature.courseview.R
import de.tum.informatics.www1.artemis.native_app.feature.courseview.ui.CourseViewModel
import de.tum.informatics.www1.artemis.native_app.feature.courseview.ui.LectureListUi
import de.tum.informatics.www1.artemis.native_app.feature.courseview.ui.exercise_list.ExerciseListUi
import de.tum.informatics.www1.artemis.native_app.feature.metis.NavigateToUserConversation
import de.tum.informatics.www1.artemis.native_app.feature.metis.NothingOpened
import de.tum.informatics.www1.artemis.native_app.feature.metis.OpenedConversation
import de.tum.informatics.www1.artemis.native_app.feature.metis.OpenedThread
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.StandalonePostId
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.ConversationFacadeUi
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

internal const val TAB_EXERCISES = 0
internal const val TAB_LECTURES = 1
internal const val TAB_COMMUNICATION = 2

internal const val DEFAULT_CONVERSATION_ID = -1L
internal const val DEFAULT_POST_ID = -1L

@Serializable
private data class CourseUiScreen(
    val courseId: Long,
    val conversationId: Long = DEFAULT_CONVERSATION_ID,
    val postId: Long = DEFAULT_POST_ID,
    val username: String = ""
)

fun NavController.navigateToCourse(courseId: Long, builder: NavOptionsBuilder.() -> Unit) {
    navigate(CourseUiScreen(courseId), builder)
}

fun NavGraphBuilder.course(
    onNavigateToExercise: (exerciseId: Long) -> Unit,
    onNavigateToExerciseResultView: (exerciseId: Long) -> Unit,
    onNavigateToTextExerciseParticipation: (exerciseId: Long, participationId: Long) -> Unit,
    onParticipateInQuiz: (courseId: Long, exerciseId: Long, isPractice: Boolean) -> Unit,
    onViewQuizResults: (courseId: Long, exerciseId: Long) -> Unit,
    onNavigateToLecture: (courseId: Long, lectureId: Long) -> Unit,
    onNavigateBack: () -> Unit
) {
    val deepLinks = listOf(
        navDeepLink {
            uriPattern = "artemis://courses/{courseId}/{conversationId}/{postId}"
        }
    ) +
            generateLinks("courses/{courseId}") +
            generateLinks("courses/{courseId}/exercises") +
            generateLinks("courses/{courseId}/messages?conversationId={conversationId}") +
            generateLinks("courses/{courseId}/messages?username={username}")
    animatedComposable<CourseUiScreen>(
        deepLinks = deepLinks
    ) { backStackEntry ->
        val route: CourseUiScreen = backStackEntry.toRoute()
        val courseId = route.courseId

        val conversationId = route.conversationId
        val postId = route.postId
        val username = route.username

        CourseUiScreen(
            modifier = Modifier.fillMaxSize(),
            viewModel = koinViewModel { parametersOf(courseId) },
            courseId = courseId,
            conversationId = conversationId,
            postId = postId,
            username = username,
            onNavigateToExercise = onNavigateToExercise,
            onNavigateToTextExerciseParticipation = onNavigateToTextExerciseParticipation,
            onNavigateToExerciseResultView = onNavigateToExerciseResultView,
            onParticipateInQuiz = { exerciseId, isPractice ->
                onParticipateInQuiz(
                    courseId,
                    exerciseId,
                    isPractice
                )
            },
            onClickViewQuizResults = onViewQuizResults,
            onNavigateToLecture = { lectureId -> onNavigateToLecture(courseId, lectureId) },
            onNavigateBack = onNavigateBack
        )
    }
}

@Composable
fun CourseUiScreen(
    modifier: Modifier,
    viewModel: CourseViewModel,
    courseId: Long,
    conversationId: Long,
    postId: Long,
    username: String,
    onNavigateToExercise: (exerciseId: Long) -> Unit,
    onNavigateToTextExerciseParticipation: (exerciseId: Long, participationId: Long) -> Unit,
    onNavigateToExerciseResultView: (exerciseId: Long) -> Unit,
    onParticipateInQuiz: (exerciseId: Long, isPractice: Boolean) -> Unit,
    onClickViewQuizResults: (courseId: Long, exerciseId: Long) -> Unit,
    onNavigateToLecture: (lectureId: Long) -> Unit,
    onNavigateBack: () -> Unit
) {
    val courseDataState by viewModel.course.collectAsState()
    val weeklyExercisesDataState by viewModel.exercisesGroupedByWeek.collectAsState()
    val weeklyLecturesDataState by viewModel.lecturesGroupedByWeek.collectAsState()

    CourseUiScreen(
        modifier = modifier,
        conversationId = conversationId,
        courseDataState = courseDataState,
        username = username,
        onNavigateBack = onNavigateBack,
        weeklyExercisesDataState = weeklyExercisesDataState,
        onNavigateToExercise = onNavigateToExercise,
        onNavigateToTextExerciseParticipation = onNavigateToTextExerciseParticipation,
        onParticipateInQuiz = onParticipateInQuiz,
        onNavigateToExerciseResultView = onNavigateToExerciseResultView,
        onClickViewQuizResults = onClickViewQuizResults,
        courseId = courseId,
        weeklyLecturesDataState = weeklyLecturesDataState,
        onNavigateToLecture = onNavigateToLecture,
        postId = postId,
        onReloadCourse = viewModel::reloadCourse,
        onClickStartTextExercise = { exerciseId: Long ->
            viewModel.startExercise(exerciseId) { participationId ->
                onNavigateToTextExerciseParticipation(
                    exerciseId,
                    participationId
                )
            }
        }
    )
}

@Composable
internal fun CourseUiScreen(
    modifier: Modifier,
    courseId: Long,
    conversationId: Long,
    postId: Long,
    username: String,
    courseDataState: DataState<Course>,
    weeklyExercisesDataState: DataState<List<GroupedByWeek<Exercise>>>,
    weeklyLecturesDataState: DataState<List<GroupedByWeek<Lecture>>>,
    onNavigateToExercise: (exerciseId: Long) -> Unit,
    onNavigateToTextExerciseParticipation: (exerciseId: Long, participationId: Long) -> Unit,
    onParticipateInQuiz: (exerciseId: Long, isPractice: Boolean) -> Unit,
    onNavigateToExerciseResultView: (exerciseId: Long) -> Unit,
    onClickViewQuizResults: (courseId: Long, exerciseId: Long) -> Unit,
    onNavigateToLecture: (lectureId: Long) -> Unit,
    onClickStartTextExercise: (exerciseId: Long) -> Unit,
    onNavigateBack: () -> Unit,
    onReloadCourse: () -> Unit
) {
    var selectedTabIndex by rememberSaveable(conversationId) {
        val initialTab = when {
            conversationId != DEFAULT_CONVERSATION_ID || username.isNotBlank() -> TAB_COMMUNICATION
            else -> TAB_EXERCISES
        }

        mutableIntStateOf(initialTab)
    }

    CourseUiScreen(
        modifier = modifier,
        courseDataState = courseDataState,
        selectedTabIndex = selectedTabIndex,
        updateSelectedTabIndex = { selectedTabIndex = it },
        exerciseTabContent = {
            EmptyDataStateUi(dataState = weeklyExercisesDataState) { weeklyExercises ->
                ExerciseListUi(
                    modifier = Modifier.fillMaxSize(),
                    weeklyExercises = weeklyExercises,
                    onClickExercise = onNavigateToExercise,
                    actions = BoundExerciseActions(
                        onClickStartTextExercise = onClickStartTextExercise,
                        onClickOpenQuiz = { exerciseId ->
                            onParticipateInQuiz(exerciseId, false)
                        },
                        onClickPracticeQuiz = { exerciseId ->
                            onParticipateInQuiz(exerciseId, true)
                        },
                        onClickStartQuiz = { exerciseId ->
                            onParticipateInQuiz(exerciseId, false)
                        },
                        onClickOpenTextExercise = onNavigateToTextExerciseParticipation,
                        onClickViewResult = onNavigateToExerciseResultView,
                        onClickViewQuizResults = { exerciseId ->
                            onClickViewQuizResults(
                                courseId,
                                exerciseId
                            )
                        }
                    )
                )
            }
        },
        lectureTabContent = {
            EmptyDataStateUi(dataState = weeklyLecturesDataState) { weeklyLectures ->
                LectureListUi(
                    modifier = Modifier.fillMaxSize(),
                    lectures = weeklyLectures,
                    onClickLecture = { onNavigateToLecture(it.id ?: 0L) }
                )
            }
        },
        communicationTabContent = { course ->
            val metisModifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp)

            if (course.courseInformationSharingConfiguration.supportsMessaging) {
                val initialConfiguration = remember(conversationId, postId) {
                    when {
                        conversationId != DEFAULT_CONVERSATION_ID && postId != DEFAULT_POST_ID -> OpenedConversation(
                            _prevConfiguration = NothingOpened,
                            conversationId = conversationId,
                            openedThread = OpenedThread(
                                StandalonePostId.ServerSideId(postId)
                            )
                        )

                        conversationId != DEFAULT_CONVERSATION_ID -> OpenedConversation(
                            _prevConfiguration = NothingOpened,
                            conversationId = conversationId,
                            openedThread = null
                        )

                        username.isNotBlank() -> NavigateToUserConversation(
                            _prevConfiguration = NothingOpened,
                            username = username
                        )

                        else -> NothingOpened
                    }
                }

                ConversationFacadeUi(
                    modifier = metisModifier,
                    courseId = courseId,
                    initialConfiguration = initialConfiguration
                )
            } else {
                Box(modifier = metisModifier) {
                    Text(
                        modifier = Modifier.align(Alignment.Center),
                        text = stringResource(id = R.string.course_ui_communication_disabled),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        },
        onNavigateBack = onNavigateBack,
        onReloadCourse = onReloadCourse
    )
}

@Composable
internal fun CourseUiScreen(
    modifier: Modifier,
    courseDataState: DataState<Course>,
    selectedTabIndex: Int,
    updateSelectedTabIndex: (Int) -> Unit,
    exerciseTabContent: @Composable () -> Unit,
    lectureTabContent: @Composable () -> Unit,
    communicationTabContent: @Composable (Course) -> Unit,
    onNavigateBack: () -> Unit,
    onReloadCourse: () -> Unit
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            CourseTopAppBar(
                courseDataState = courseDataState,
                onNavigateBack = onNavigateBack,
                selectedTabIndex = selectedTabIndex,
                changeTab = updateSelectedTabIndex
            )
        }
    ) { padding ->
        BasicDataStateUi(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = padding.calculateTopPadding())
                .consumeWindowInsets(WindowInsets.systemBars.only(WindowInsetsSides.Top)),
            dataState = courseDataState,
            loadingText = stringResource(id = R.string.course_ui_loading_course_loading),
            failureText = stringResource(id = R.string.course_ui_loading_course_failed),
            retryButtonText = stringResource(id = R.string.course_ui_loading_course_try_again),
            onClickRetry = onReloadCourse
        ) { course ->
            AnimatedContent(
                targetState = selectedTabIndex,
                transitionSpec = {
                    if (targetState > initialState) {
                        DefaultTransition.navigateForward
                    } else {
                        DefaultTransition.navigateBack
                    }.using(
                        SizeTransform(clip = false)
                    )
                },
                label = "Switch Course Tab"
            ) { tabIndex ->
                when (tabIndex) {
                    TAB_EXERCISES -> exerciseTabContent()

                    TAB_LECTURES -> lectureTabContent()

                    TAB_COMMUNICATION -> communicationTabContent(course)
                }
            }
        }
    }
}
