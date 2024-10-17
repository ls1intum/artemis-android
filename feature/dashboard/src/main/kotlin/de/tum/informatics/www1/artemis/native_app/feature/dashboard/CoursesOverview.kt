package de.tum.informatics.www1.artemis.native_app.feature.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable
import de.tum.informatics.www1.artemis.native_app.core.model.Course
import de.tum.informatics.www1.artemis.native_app.core.model.CourseWithScore
import de.tum.informatics.www1.artemis.native_app.core.model.Dashboard
import de.tum.informatics.www1.artemis.native_app.core.ui.alert.TextAlertDialog
import de.tum.informatics.www1.artemis.native_app.core.ui.common.BasicDataStateUi
import de.tum.informatics.www1.artemis.native_app.core.ui.common.course.CompactCourseHeaderViewMode
import de.tum.informatics.www1.artemis.native_app.core.ui.common.course.CompactCourseItemHeader
import de.tum.informatics.www1.artemis.native_app.core.ui.common.course.CourseExerciseAndLectureCount
import de.tum.informatics.www1.artemis.native_app.core.ui.common.course.CourseItemGrid
import de.tum.informatics.www1.artemis.native_app.core.ui.common.course.ExpandedCourseItemHeader
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.CoursePointsDecimalFormat
import de.tum.informatics.www1.artemis.native_app.feature.dashboard.service.BetaHintService
import kotlinx.coroutines.launch
import org.koin.androidx.compose.getViewModel
import org.koin.compose.koinInject
import java.text.DecimalFormat

const val DASHBOARD_DESTINATION = "dashboard"
internal const val TEST_TAG_COURSE_LIST = "TEST_TAG_COURSE_LIST"

internal fun testTagForCourse(courseId: Long) = "Course$courseId"

fun NavController.navigateToDashboard(builder: NavOptionsBuilder.() -> Unit) {
    navigate(DASHBOARD_DESTINATION, builder)
}

fun NavGraphBuilder.dashboard(
    onOpenSettings: () -> Unit,
    onClickRegisterForCourse: () -> Unit,
    onViewCourse: (courseId: Long) -> Unit
) {
    composable(DASHBOARD_DESTINATION) {
        CoursesOverview(
            modifier = Modifier.fillMaxSize(),
            viewModel = getViewModel(),
            onOpenSettings = onOpenSettings,
            onClickRegisterForCourse = onClickRegisterForCourse,
            onViewCourse = onViewCourse
        )
    }
}

/**
 * Displays the Course Overview screen.
 * Uses Scaffold to display a Material Design TopAppBar.
 */
@Composable
internal fun CoursesOverview(
    modifier: Modifier,
    viewModel: CourseOverviewViewModel,
    onOpenSettings: () -> Unit,
    onClickRegisterForCourse: () -> Unit,
    onViewCourse: (courseId: Long) -> Unit,
    isBeta: Boolean = BuildConfig.isBeta,
    betaHintService: BetaHintService = koinInject()
) {
    val coursesDataState by viewModel.dashboard.collectAsState()

    //The course composable needs the serverUrl to build the correct url to fetch the course icon from.
    val serverUrl by viewModel.serverUrl.collectAsState()
    //The server wants an authorization token to send the course icon.
    val authToken by viewModel.authToken.collectAsState()

    val topAppBarState = rememberTopAppBarState()

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(
        topAppBarState
    )

    val shouldDisplayBetaDialog by betaHintService.shouldShowBetaHint.collectAsState(initial = false)
    var displayBetaDialog by rememberSaveable { mutableStateOf(false) }

    // Trigger the dialog if service sets value to true
    LaunchedEffect(shouldDisplayBetaDialog) {
        if (shouldDisplayBetaDialog) displayBetaDialog = true
    }

    Scaffold(
        modifier = modifier.then(Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)),
        topBar = {
            TopAppBar(
                title = {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            modifier = Modifier.weight(1f, fill = false),
                            text = stringResource(id = R.string.course_overview_title),
                            maxLines = 1
                        )

                        if (isBeta) {
                            Text(
                                modifier = Modifier
                                    .border(
                                        1.dp,
                                        color = MaterialTheme.colorScheme.outline,
                                        shape = RoundedCornerShape(percent = 50)
                                    )
                                    .padding(horizontal = 8.dp),
                                text = stringResource(id = R.string.dashboard_title_beta),
                                color = MaterialTheme.colorScheme.outline,
                                maxLines = 1
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::requestReloadDashboard) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null
                        )
                    }

                    IconButton(onClick = onClickRegisterForCourse) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = stringResource(id = R.string.course_overview_action_register)
                        )
                    }

                    IconButton(onClick = onOpenSettings) {
                        Icon(imageVector = Icons.Default.Settings, contentDescription = null)
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { padding ->
        BasicDataStateUi(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            dataState = coursesDataState,
            loadingText = stringResource(id = R.string.courses_loading_loading),
            failureText = stringResource(id = R.string.courses_loading_failure),
            retryButtonText = stringResource(id = R.string.courses_loading_try_again),
            onClickRetry = viewModel::requestReloadDashboard
        ) { dashboard: Dashboard ->
            if (dashboard.courses.isEmpty()) {
                DashboardEmpty(
                    modifier = Modifier.fillMaxSize(),
                    onClickSignup = onClickRegisterForCourse
                )
            } else {
                CourseList(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp)
                        .testTag(TEST_TAG_COURSE_LIST),
                    courses = dashboard.courses,
                    serverUrl = serverUrl,
                    authorizationToken = authToken,
                    onClickOnCourse = { course -> onViewCourse(course.id ?: 0L) }
                )
            }
        }
    }

    if (displayBetaDialog) {
        val scope = rememberCoroutineScope()

        BetaHintDialog { dismissPermanently ->
            if (dismissPermanently) {
                scope.launch {
                    betaHintService.dismissBetaHintPermanently()

                    displayBetaDialog = false
                }
            } else {
                displayBetaDialog = false
            }
        }
    }
}

@Composable
private fun BetaHintDialog(
    dismiss: (dismissPermanently: Boolean) -> Unit
) {
    var isDismissPersistentlyChecked by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { dismiss(false) },
        title = { Text(text = stringResource(id = R.string.dashboard_dialog_beta_title)) },
        text = {
            Column(
                modifier = Modifier,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = stringResource(id = R.string.dashboard_dialog_beta_message))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            role = Role.Checkbox,
                            onClick = { isDismissPersistentlyChecked = !isDismissPersistentlyChecked }
                        ),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        modifier = Modifier,
                        checked = isDismissPersistentlyChecked,
                        onCheckedChange = { isDismissPersistentlyChecked = it }
                    )

                    Text(text = stringResource(id = R.string.dashboard_dialog_beta_do_not_show_again))
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { dismiss(isDismissPersistentlyChecked) }
            ) {
                Text(text = stringResource(id = R.string.dashboard_dialog_beta_positive))
            }
        }
    )
}

/**
 * Displays a lazy list of all the courses supplied.
 */
@Composable
private fun CourseList(
    modifier: Modifier,
    courses: List<CourseWithScore>,
    serverUrl: String,
    authorizationToken: String,
    onClickOnCourse: (Course) -> Unit
) {
    CourseItemGrid(
        modifier = modifier,
        courses = courses,
    ) { dashboardCourse, courseItemModifier, isCompact ->
        CourseItem(
            modifier = courseItemModifier.testTag(testTagForCourse(dashboardCourse.course.id!!)),
            courseWithScore = dashboardCourse,
            serverUrl = serverUrl,
            authorizationToken = authorizationToken,
            onClick = { onClickOnCourse(dashboardCourse.course) },
            isCompact = isCompact
        )
    }
}

/**
 * Displays course icon, title and description in a Material Design Card.
 */
@Composable
fun CourseItem(
    modifier: Modifier,
    isCompact: Boolean,
    courseWithScore: CourseWithScore,
    serverUrl: String,
    authorizationToken: String,
    onClick: () -> Unit
) {
    val currentPoints = courseWithScore.totalScores.studentScores.absoluteScore
    val maxPoints = courseWithScore.totalScores.maxPoints

    val currentPointsFormatted = remember(currentPoints) {
        CoursePointsDecimalFormat.format(currentPoints)
    }
    val maxPointsFormatted = remember(maxPoints) {
        CoursePointsDecimalFormat.format(maxPoints)
    }

    val progress = if (maxPoints == 0f) 0f else currentPoints / maxPoints

    val progressPercentFormatted = remember(progress) {
        DecimalFormat.getPercentInstance().format(progress)
    }

    if (isCompact) {
        CompactCourseItemHeader(
            modifier = modifier,
            course = courseWithScore.course,
            serverUrl = serverUrl,
            authorizationToken = authorizationToken,
            onClick = onClick,
            compactCourseHeaderViewMode = CompactCourseHeaderViewMode.EXERCISE_AND_LECTURE_COUNT,
            content = {
                Divider()

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp, horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LinearProgressIndicator(
                        modifier = Modifier.weight(1f),
                        progress = progress,
                        trackColor = MaterialTheme.colorScheme.onPrimary
                    )

                    CourseProgressText(
                        modifier = Modifier,
                        currentPointsFormatted = currentPointsFormatted,
                        maxPointsFormatted = maxPointsFormatted,
                        progressPercentFormatted = progressPercentFormatted
                    )
                }
            }
        )
    } else {
        ExpandedCourseItemHeader(
            modifier = modifier,
            course = courseWithScore.course,
            serverUrl = serverUrl,
            authorizationToken = authorizationToken,
            onClick = onClick,
            content = {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f)
                        .align(Alignment.CenterHorizontally)
                ) {
                    CircularCourseProgress(
                        modifier = Modifier
                            .fillMaxSize(0.8f)
                            .align(Alignment.Center),
                        progress = progress,
                        currentPointsFormatted = currentPointsFormatted,
                        maxPointsFormatted = maxPointsFormatted,
                        progressPercentFormatted = progressPercentFormatted
                    )
                }

                CourseExerciseAndLectureCount(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(vertical = 8.dp),
                    exerciseCount = courseWithScore.course.exercises.size,
                    lectureCount = courseWithScore.course.lectures.size,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(textAlign = TextAlign.Center),
                    alignment = Alignment.CenterHorizontally
                )
            },
            rightHeaderContent = { }
        )
    }
}

@Composable
private fun CircularCourseProgress(
    modifier: Modifier,
    progress: Float,
    currentPointsFormatted: String,
    maxPointsFormatted: String,
    progressPercentFormatted: String
) {
    BoxWithConstraints(modifier = modifier) {
        val progressBarWidthDp = min(24.dp, maxWidth * 0.1f)
        val progressBarWidth = with(LocalDensity.current) { progressBarWidthDp.toPx() }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(progressBarWidthDp)
        ) {
            drawArc(
                color = Color.Green,
                startAngle = 180f,
                sweepAngle = 360f * progress,
                useCenter = false,
                style = Stroke(width = progressBarWidth)
            )

            drawArc(
                color = Color.Red,
                startAngle = 180f + 360f * progress,
                sweepAngle = 360f * (1f - progress),
                useCenter = false,
                style = Stroke(width = progressBarWidth)
            )
        }

        val (percentFontSize, ptsFontSize) = with(LocalDensity.current) {
            val availableSpace = maxHeight - progressBarWidthDp * 2
            (availableSpace * 0.2f).toSp() to (availableSpace * 0.1f).toSp()
        }

        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(
                    id = R.string.course_overview_course_progress_percentage,
                    progressPercentFormatted
                ),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                fontSize = percentFontSize,
                fontWeight = FontWeight.Normal
            )

            Text(
                text = stringResource(
                    id = R.string.course_overview_course_progress_pts,
                    currentPointsFormatted,
                    maxPointsFormatted
                ),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                fontSize = ptsFontSize,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun CourseProgressText(
    modifier: Modifier,
    currentPointsFormatted: String,
    maxPointsFormatted: String,
    progressPercentFormatted: String
) {
    Text(
        modifier = modifier,
        text = stringResource(
            id = R.string.course_overview_course_progress,
            currentPointsFormatted,
            maxPointsFormatted,
            progressPercentFormatted
        ),
        fontSize = 14.sp
    )
}

@Composable
private fun DashboardEmpty(modifier: Modifier, onClickSignup: () -> Unit) {
    Box(modifier = modifier) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(id = R.string.courses_empty_text),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )

            Button(onClick = onClickSignup) {
                Text(text = stringResource(id = R.string.courses_empty_register_now_button))
            }
        }
    }
}