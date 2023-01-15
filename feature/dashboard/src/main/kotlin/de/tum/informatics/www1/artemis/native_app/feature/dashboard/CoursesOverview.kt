package de.tum.informatics.www1.artemis.native_app.feature.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import de.tum.informatics.www1.artemis.native_app.core.model.Course
import de.tum.informatics.www1.artemis.native_app.core.model.Dashboard
import de.tum.informatics.www1.artemis.native_app.core.ui.common.BasicDataStateUi
import de.tum.informatics.www1.artemis.native_app.core.ui.common.course.CompactCourseItemHeader
import de.tum.informatics.www1.artemis.native_app.core.ui.common.course.CourseItemGrid
import de.tum.informatics.www1.artemis.native_app.core.ui.common.course.ExpandedCourseItemHeader
import org.koin.androidx.compose.getViewModel
import java.text.DecimalFormat
import java.text.NumberFormat

const val DASHBOARD_DESTINATION = "dashboard"

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
    onViewCourse: (courseId: Long) -> Unit
) {
    val coursesDataState by viewModel.dashboard.collectAsState()

    //The course composable needs the serverUrl to build the correct url to fetch the course icon from.
    val serverUrl by viewModel.serverUrl.collectAsState()
    //The server wants an authorization token to send the course icon.
    val authorizationBearer by viewModel.authorizationBearerToken.collectAsState()

    val topAppBarState = rememberTopAppBarState()

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(
        topAppBarState
    )

    Scaffold(
        modifier = modifier.then(Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)),
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.course_overview_title)) },
                actions = {
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
            val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = false)

            SwipeRefresh(state = swipeRefreshState, onRefresh = viewModel::requestReloadDashboard) {
                if (dashboard.courses.isEmpty()) {
                    DashboardEmpty(
                        modifier = Modifier.fillMaxSize(),
                        onClickSignup = onClickRegisterForCourse
                    )
                } else {
                    CourseList(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp),
                        courses = dashboard.courses,
                        serverUrl = serverUrl,
                        authorizationToken = authorizationBearer,
                        onClickOnCourse = { course -> onViewCourse(course.id) }
                    )
                }
            }
        }
    }
}

/**
 * Displays a lazy list of all the courses supplied.
 */
@Composable
private fun CourseList(
    modifier: Modifier,
    courses: List<Course>,
    serverUrl: String,
    authorizationToken: String,
    onClickOnCourse: (Course) -> Unit
) {
    CourseItemGrid(
        modifier = modifier,
        courses = courses,
    ) { course, courseItemModifier, isCompact ->
        CourseItem(
            modifier = courseItemModifier,
            course = course,
            serverUrl = serverUrl,
            authorizationToken = authorizationToken,
            onClick = { onClickOnCourse(course) },
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
    course: Course,
    serverUrl: String,
    authorizationToken: String,
    onClick: () -> Unit
) {
    val content: @Composable ColumnScope.() -> Unit = @Composable {
        val courseProgress = course.progress.coerceAtMost(1f)
        if (isCompact) {
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
                    progress = courseProgress,
                    trackColor = MaterialTheme.colorScheme.onPrimary
                )

                CourseProgressText(
                    modifier = Modifier,
                    course = course
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    progress = courseProgress
                )

                CourseProgressText(
                    modifier = Modifier,
                    course = course
                )
            }
        }
    }

    if (isCompact) {
        CompactCourseItemHeader(
            modifier = modifier,
            course = course,
            serverUrl = serverUrl,
            authorizationToken = authorizationToken,
            onClick = onClick,
            content = content
        )
    } else {
        ExpandedCourseItemHeader(
            modifier = modifier,
            course = course,
            serverUrl = serverUrl,
            authorizationToken = authorizationToken,
            onClick = onClick,
            content = content
        )
    }
}

@Composable
private fun CourseProgressText(modifier: Modifier, course: Course) {
    val scoreNumberFormat = remember {
        DecimalFormat("0").apply {
            maximumFractionDigits = 1
        }
    }

    val percentNumberFormat = remember {
        NumberFormat.getPercentInstance().apply {
            maximumFractionDigits = 0
        }
    }

    Text(
        modifier = modifier,
        text = stringResource(
            id = R.string.course_overview_course_progress,
            scoreNumberFormat.format(course.currentScore),
            scoreNumberFormat.format(course.maxPointsPossible),
            percentNumberFormat.format(course.progress)
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