package de.tum.informatics.www1.artemis.native_app.feature.dashboard.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import de.tum.informatics.www1.artemis.native_app.core.model.Dashboard
import de.tum.informatics.www1.artemis.native_app.core.ui.LocalArtemisContextProvider
import de.tum.informatics.www1.artemis.native_app.core.ui.Spacings
import de.tum.informatics.www1.artemis.native_app.core.ui.common.BasicDataStateUi
import de.tum.informatics.www1.artemis.native_app.core.ui.common.BasicSearchTextField
import de.tum.informatics.www1.artemis.native_app.core.ui.common.NoSearchResults
import de.tum.informatics.www1.artemis.native_app.core.ui.compose.OnTrueLaunchedEffect
import de.tum.informatics.www1.artemis.native_app.core.ui.navigation.animatedComposable
import de.tum.informatics.www1.artemis.native_app.feature.dashboard.BuildConfig
import de.tum.informatics.www1.artemis.native_app.feature.dashboard.R
import de.tum.informatics.www1.artemis.native_app.feature.dashboard.service.BetaHintService
import de.tum.informatics.www1.artemis.native_app.feature.dashboard.service.SurveyHintService
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

internal const val TEST_TAG_COURSE_LIST = "TEST_TAG_COURSE_LIST"

internal fun testTagForCourse(courseId: Long) = "Course$courseId"

@Serializable
data object DashboardScreen

fun NavController.navigateToDashboard(builder: NavOptionsBuilder.() -> Unit) {
    navigate(DashboardScreen, builder)
}

fun NavGraphBuilder.dashboard(
    onOpenSettings: () -> Unit,
    onClickRegisterForCourse: () -> Unit,
    onViewCourse: (courseId: Long) -> Unit
) {
    animatedComposable<DashboardScreen> {
        val artemisContextProvider = LocalArtemisContextProvider.current
        LaunchedEffect(true) {
            artemisContextProvider.clearCourseId()
        }

        CoursesOverview(
            modifier = Modifier.fillMaxSize(),
            viewModel = koinViewModel(),
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
    betaHintService: BetaHintService = koinInject(),
    surveyHintService: SurveyHintService = koinInject()
) {
    val coursesDataState by viewModel.dashboard.collectAsState()
    val scope = rememberCoroutineScope()

    val shouldDisplayBetaDialog by betaHintService.shouldShowBetaHint.collectAsState(initial = false)
    var displayBetaDialog by rememberSaveable { mutableStateOf(false) }

    val query by viewModel.query.collectAsState()
    val sorting by viewModel.sorting.collectAsState()

    val courseListState = rememberLazyGridState()
    var lastIndex by remember { mutableIntStateOf(0) }
    var lastOffset by remember { mutableIntStateOf(0) }

    // Trigger the dialog if service sets value to true
    OnTrueLaunchedEffect(shouldDisplayBetaDialog) {
        displayBetaDialog = true
    }

    LaunchedEffect(sorting, query) {
        courseListState.scrollToItem(lastIndex, lastOffset)
    }

    Scaffold(
        modifier = modifier,
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
                    IconButton(onClick = onClickRegisterForCourse) {
                        Icon(
                            modifier = Modifier.size(22.dp),
                            tint = MaterialTheme.colorScheme.primary,
                            painter = painterResource(id = de.tum.informatics.www1.artemis.native_app.core.ui.R.drawable.enroll),
                            contentDescription = stringResource(id = R.string.course_overview_action_register)
                        )
                    }

                    IconButton(onClick = onOpenSettings) {
                        Icon(
                            modifier = Modifier.size(23.dp),
                            painter = painterResource(id = R.drawable.settings),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(top = padding.calculateTopPadding())
                .consumeWindowInsets(WindowInsets.systemBars.only(WindowInsetsSides.Top))
        ) {
            SurveyHint(
                modifier = Modifier
                    .padding(vertical = 8.dp, horizontal = Spacings.ScreenHorizontalSpacing)
                    .fillMaxWidth(),
                surveyHintService = surveyHintService
            )

            SearchAndOrderRow(
                modifier = Modifier.fillMaxWidth(),
                query = query,
                sorting = sorting,
                onUpdateQuery = viewModel::onUpdateQuery,
                onUpdateSorting = viewModel::onUpdateSorting,
                captureListPosition = {
                    lastIndex = courseListState.firstVisibleItemIndex
                    lastOffset = courseListState.firstVisibleItemScrollOffset
                }
            )

            BasicDataStateUi(
                modifier = Modifier.fillMaxSize(),
                dataState = coursesDataState,
                loadingText = stringResource(id = R.string.courses_loading_loading),
                failureText = stringResource(id = R.string.courses_loading_failure),
                retryButtonText = stringResource(id = R.string.courses_loading_try_again),
                onClickRetry = viewModel::requestReloadDashboard
            ) { dashboard: Dashboard ->
                if (dashboard.courses.isEmpty() && dashboard.recentCourses.isEmpty()) {
                    if (query.isNotBlank()) {
                        NoSearchResults(
                            modifier = Modifier.fillMaxSize(),
                            title = stringResource(id = R.string.course_overview_no_search_results),
                            details = stringResource(
                                id = R.string.course_overview_no_search_results_details,
                                query
                            )
                        )
                        return@BasicDataStateUi
                    }

                    DashboardEmpty(
                        modifier = Modifier.fillMaxSize(),
                        onClickSignup = onClickRegisterForCourse
                    )
                    return@BasicDataStateUi
                }

                CourseList(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = Spacings.ScreenHorizontalSpacing)
                        .testTag(TEST_TAG_COURSE_LIST),
                    courseListState = courseListState,
                    courses = dashboard.courses,
                    recentCourses = dashboard.recentCourses,
                    onClickOnCourse = { course ->
                        scope.launch{
                            viewModel.onCourseAccessed(course.id ?: 0L)
                        }
                        onViewCourse(course.id ?: 0L)
                    }
                )
            }
        }
    }

    if (displayBetaDialog) {
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
private fun SearchAndOrderRow(
    modifier: Modifier,
    query: String,
    sorting: CourseSorting,
    onUpdateQuery: (String) -> Unit,
    onUpdateSorting: (CourseSorting) -> Unit,
    captureListPosition: () -> Unit
) {
    Row(
        modifier = modifier
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BasicSearchTextField(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            hint = stringResource(id = R.string.course_overview_search_courses_hint),
            query = query,
            updateQuery = {
                if (query.isBlank()) captureListPosition()
                onUpdateQuery(it)
            },
        )

        IconButton(
            modifier = Modifier
                .size(24.dp),
            onClick = {
                val newSorting = if (sorting == CourseSorting.ALPHABETICAL_ASCENDING) {
                    CourseSorting.ALPHABETICAL_DESCENDING
                } else {
                    CourseSorting.ALPHABETICAL_ASCENDING
                }
                captureListPosition()
                onUpdateSorting(newSorting)
            }
        ) {
            Icon(
                modifier = Modifier.size(24.dp),
                painter = if (sorting == CourseSorting.ALPHABETICAL_ASCENDING) painterResource(id = R.drawable.alphabetical_sorting_descending) else painterResource(id = R.drawable.alphabetical_sorting_ascending),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    }
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