package de.tum.informatics.www1.artemis.native_app.feature.courseview.ui.course_overview

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.google.accompanist.placeholder.material.placeholder
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.model.Course
import de.tum.informatics.www1.artemis.native_app.core.ui.common.BasicDataStateUi
import de.tum.informatics.www1.artemis.native_app.core.ui.common.EmptyDataStateUi
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.BoundExerciseActions
import de.tum.informatics.www1.artemis.native_app.core.ui.generateLinks
import de.tum.informatics.www1.artemis.native_app.feature.courseview.R
import de.tum.informatics.www1.artemis.native_app.feature.courseview.ui.CourseViewModel
import de.tum.informatics.www1.artemis.native_app.feature.courseview.ui.LectureListUi
import de.tum.informatics.www1.artemis.native_app.feature.courseview.ui.exercise_list.ExerciseListUi
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.thread.StandalonePostId
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.NothingOpened
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.OpenedConversation
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.OpenedThread
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.SinglePageConversationBody
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

private const val TAB_EXERCISES = 0
private const val TAB_LECTURES = 1
private const val TAB_COMMUNICATION = 2

internal const val DEFAULT_CONVERSATION_ID = -1L
internal const val DEFAULT_POST_ID = -1L

fun NavController.navigateToCourse(courseId: Long, builder: NavOptionsBuilder.() -> Unit) {
    navigate("course/$courseId", builder)
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
            generateLinks("courses/{courseId}/messages?conversationId={conversationId}")
    composable(
        route = "course/{courseId}",
        arguments = listOf(
            navArgument("courseId") { type = NavType.LongType; nullable = false },
            navArgument("conversationId") {
                type = NavType.LongType; defaultValue = DEFAULT_CONVERSATION_ID
            },
            navArgument("postId") { type = NavType.LongType; defaultValue = DEFAULT_POST_ID }
        ),
        deepLinks = deepLinks
    ) { backStackEntry ->
        val courseId = backStackEntry.arguments?.getLong("courseId")
        checkNotNull(courseId)

        val conversationId =
            backStackEntry.arguments?.getLong("conversationId") ?: DEFAULT_CONVERSATION_ID
        val postId = backStackEntry.arguments?.getLong("postId") ?: DEFAULT_POST_ID

        CourseUiScreen(
            modifier = Modifier.fillMaxSize(),
            viewModel = koinViewModel { parametersOf(courseId) },
            courseId = courseId,
            conversationId = conversationId,
            postId = postId,
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
internal fun CourseUiScreen(
    modifier: Modifier,
    viewModel: CourseViewModel,
    courseId: Long,
    conversationId: Long,
    postId: Long,
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

    val topAppBarState = rememberTopAppBarState()

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        topAppBarState
    )

    var selectedTabIndex by rememberSaveable(conversationId) {
        val initialTab = when {
            conversationId != DEFAULT_CONVERSATION_ID -> TAB_COMMUNICATION
            else -> TAB_EXERCISES
        }

        mutableStateOf(initialTab)
    }

    Scaffold(
        modifier = modifier.then(Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)),
        topBar = {
            CourseTopAppBar(
                courseDataState = courseDataState,
                onNavigateBack = onNavigateBack,
                scrollBehavior = scrollBehavior,
                selectedTabIndex = selectedTabIndex,
                changeTab = { selectedTabIndex = it },
                onReloadCourse = viewModel::reloadCourse
            )
        }
    ) { padding ->
        BasicDataStateUi(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            dataState = courseDataState,
            loadingText = stringResource(id = R.string.course_ui_loading_course_loading),
            failureText = stringResource(id = R.string.course_ui_loading_course_failed),
            retryButtonText = stringResource(id = R.string.course_ui_loading_course_try_again),
            onClickRetry = { viewModel.reloadCourse() }
        ) { course ->
            AnimatedContent(
                targetState = selectedTabIndex,
                transitionSpec = {
                    if (targetState > initialState) {
                        slideInHorizontally { width -> width } togetherWith
                                slideOutHorizontally { width -> -width }
                    } else {
                        slideInHorizontally { width -> -width } togetherWith
                                slideOutHorizontally { width -> width }
                    }.using(
                        SizeTransform(clip = false)
                    )
                },
                label = "Switch Course Tab"
            ) { tabIndex ->
                when (tabIndex) {
                    TAB_EXERCISES -> {
                        EmptyDataStateUi(dataState = weeklyExercisesDataState) { weeklyExercises ->
                            ExerciseListUi(
                                modifier = Modifier.fillMaxSize(),
                                weeklyExercises = weeklyExercises,
                                onClickExercise = onNavigateToExercise,
                                actions = BoundExerciseActions(
                                    onClickStartTextExercise = { exerciseId ->
                                        viewModel.startExercise(exerciseId) { participationId ->
                                            onNavigateToTextExerciseParticipation(
                                                exerciseId,
                                                participationId
                                            )
                                        }
                                    },
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
                    }

                    TAB_LECTURES -> {
                        EmptyDataStateUi(dataState = weeklyLecturesDataState) { weeklyLectures ->
                            LectureListUi(
                                modifier = Modifier.fillMaxSize(),
                                lectures = weeklyLectures,
                                onClickLecture = { onNavigateToLecture(it.id ?: 0L) }
                            )
                        }
                    }

                    TAB_COMMUNICATION -> {
                        val metisModifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp)

                        if (course.courseInformationSharingConfiguration.supportsMessaging) {
                            val initialConfiguration = remember(conversationId, postId) {
                                when {
                                    conversationId != DEFAULT_CONVERSATION_ID && postId != DEFAULT_POST_ID -> OpenedConversation(
                                        conversationId,
                                        OpenedThread(
                                            conversationId,
                                            StandalonePostId.ServerSideId(postId)
                                        )
                                    )

                                    conversationId != DEFAULT_CONVERSATION_ID -> OpenedConversation(
                                        conversationId,
                                        null
                                    )

                                    else -> NothingOpened
                                }
                            }

                            SinglePageConversationBody(
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
                    }
                }
            }
        }
    }
}

@Composable
private fun CourseTopAppBar(
    selectedTabIndex: Int,
    courseDataState: DataState<Course>,
    scrollBehavior: TopAppBarScrollBehavior,
    changeTab: (Int) -> Unit,
    onReloadCourse: () -> Unit,
    onNavigateBack: () -> Unit
) {
    Column {
        TopAppBar(
            title = {
                Text(
                    modifier = Modifier.placeholder(visible = courseDataState !is DataState.Success),
                    text = courseDataState.bind { it.title }
                        .orElse("Placeholder course title"),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null)
                }
            },
            actions = {
                IconButton(onClick = onReloadCourse) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                }
            },
            scrollBehavior = scrollBehavior
        )
        TabRow(
            modifier = Modifier.fillMaxWidth(),
            selectedTabIndex = selectedTabIndex
        ) {
            @Suppress("LocalVariableName")
            val CourseTab = @Composable { index: Int, text: String, icon: ImageVector ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { changeTab(index) },
                    text = {
                        Text(
                            text = text,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    icon = { Icon(icon, contentDescription = null) }
                )
            }

            CourseTab(
                0,
                stringResource(id = R.string.course_ui_tab_exercises),
                Icons.Default.ListAlt
            )
            CourseTab(
                1,
                stringResource(id = R.string.course_ui_tab_lectures),
                Icons.Default.School
            )
            CourseTab(
                2,
                stringResource(id = R.string.course_ui_tab_communication),
                Icons.Default.Chat
            )
        }
    }
}
