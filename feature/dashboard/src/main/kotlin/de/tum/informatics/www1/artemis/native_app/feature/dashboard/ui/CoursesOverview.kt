package de.tum.informatics.www1.artemis.native_app.feature.dashboard.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import de.tum.informatics.www1.artemis.native_app.core.model.Dashboard
import de.tum.informatics.www1.artemis.native_app.core.ui.common.BasicDataStateUi
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
        Column(
            modifier = Modifier
                .padding(top = padding.calculateTopPadding())
                .consumeWindowInsets(WindowInsets.systemBars)
        ) {
            SurveyHint(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth(),
                surveyHintService = surveyHintService
            )

            BasicDataStateUi(
                modifier = Modifier.fillMaxSize(),
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
                        onClickOnCourse = { course -> onViewCourse(course.id ?: 0L) }
                    )
                }
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