package de.tum.informatics.www1.artemis.native_app.feature.course_view

import androidx.compose.animation.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.*
import androidx.navigation.compose.composable
import com.google.accompanist.placeholder.material.placeholder
import de.tum.informatics.www1.artemis.native_app.core.communication.ui.SmartphoneMetisUi
import de.tum.informatics.www1.artemis.native_app.core.ui.common.BasicDataStateUi
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.datastore.model.metis.MetisContext
import de.tum.informatics.www1.artemis.native_app.core.ui.common.EmptyDataStateUi
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

fun NavController.navigateToCourse(courseId: Long, builder: NavOptionsBuilder.() -> Unit) {
    navigate("course/$courseId", builder)
}

fun NavGraphBuilder.course(
    navController: NavController,
    onNavigateToExercise: (exerciseId: Long) -> Unit,
    onNavigateToLecture: (courseId: Long, lectureId: Long) -> Unit,
    onNavigateBack: () -> Unit
) {
    composable("course/{courseId}", arguments = listOf(
        navArgument("courseId") { type = NavType.LongType; nullable = false }
    )) { backStackEntry ->
        val courseId =
            backStackEntry.arguments?.getLong("courseId")
        checkNotNull(courseId)

        CourseUiScreen(
            modifier = Modifier.fillMaxSize(),
            viewModel = koinViewModel { parametersOf(courseId) },
            onNavigateBack = onNavigateBack,
            onNavigateToExercise = onNavigateToExercise,
            courseId = courseId,
            navController = navController,
            onNavigateToLecture = { lectureId -> onNavigateToLecture(courseId, lectureId) }
        )
    }
}

@Composable
internal fun CourseUiScreen(
    modifier: Modifier,
    viewModel: CourseViewModel,
    courseId: Long,
    navController: NavController,
    onNavigateToExercise: (exerciseId: Long) -> Unit,
    onNavigateToLecture: (lectureId: Long) -> Unit,
    onNavigateBack: () -> Unit
) {
    val courseDataState by viewModel.course.collectAsState(initial = DataState.Loading())
    val weeklyExercisesDataState by viewModel.exercisesGroupedByWeek.collectAsState(initial = DataState.Loading())
    val weeklyLecturesDataState by viewModel.lecturesGroupedByWeek.collectAsState(initial = DataState.Loading())

    val topAppBarState = rememberTopAppBarState()

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(
        topAppBarState
    )

    var selectedTabIndex by rememberSaveable { mutableStateOf(0) }

    Scaffold(
        modifier = modifier.then(Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)),
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            modifier = Modifier.placeholder(visible = courseDataState !is DataState.Success),
                            text = courseDataState.bind { it.title }
                                .orElse("Placeholder course title")
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null)
                        }
                    },
                    actions = {
                        IconButton(onClick = viewModel::reloadCourse) {
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
                            onClick = { selectedTabIndex = index },
                            text = { Text(text = text) },
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
//                    CourseTab(
//                        3,
////                        stringResource(id = R.string.course_ui_tab_other),
//                        "Coming soon",
//                        Icons.Default.MoreHoriz
//                    )

                }
            }
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
                        slideInHorizontally { width -> width } with
                                slideOutHorizontally { width -> -width }
                    } else {
                        slideInHorizontally { width -> -width } with
                                slideOutHorizontally { width -> width }
                    }.using(
                        SizeTransform(clip = false)
                    )
                }
            ) { tabIndex ->
                when (tabIndex) {
                    0 -> {
                        EmptyDataStateUi(dataState = weeklyExercisesDataState) { weeklyExercises ->
                            ExerciseListUi(
                                modifier = Modifier.fillMaxSize(),
                                weeklyExercises = weeklyExercises,
                                onClickExercise = onNavigateToExercise
                            )
                        }
                    }

                    1 -> {
                        EmptyDataStateUi(dataState = weeklyLecturesDataState) { weeklyLectures ->
                            LectureListUi(
                                modifier = Modifier.fillMaxSize(),
                                lectures = weeklyLectures,
                                onClickLecture = { onNavigateToLecture(it.id) }
                            )
                        }
                    }

                    2 -> {
                        val metisModifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp)

                        if (course.postsEnabled) {
                            val metisContext = remember {
                                MetisContext.Course(courseId = courseId)
                            }
                            SmartphoneMetisUi(
                                modifier = metisModifier,
                                metisContext = metisContext,
                                navController = navController
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
