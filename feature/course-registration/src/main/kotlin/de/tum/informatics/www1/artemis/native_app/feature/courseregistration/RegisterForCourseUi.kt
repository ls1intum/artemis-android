package de.tum.informatics.www1.artemis.native_app.feature.courseregistration

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.model.Course
import de.tum.informatics.www1.artemis.native_app.core.ui.AwaitDeferredCompletion
import de.tum.informatics.www1.artemis.native_app.core.ui.common.BasicDataStateUi
import de.tum.informatics.www1.artemis.native_app.core.ui.common.course.CompactCourseHeaderViewMode
import de.tum.informatics.www1.artemis.native_app.core.ui.common.course.CompactCourseItemHeader
import de.tum.informatics.www1.artemis.native_app.core.ui.common.course.ExpandedCourseItemHeader
import de.tum.informatics.www1.artemis.native_app.core.ui.common.course.computeCourseColumnCount
import de.tum.informatics.www1.artemis.native_app.core.ui.common.course.computeCourseItemModifier
import de.tum.informatics.www1.artemis.native_app.core.ui.getWindowSizeClass
import de.tum.informatics.www1.artemis.native_app.core.ui.markdown.MarkdownText
import kotlinx.coroutines.Deferred
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel

internal const val TEST_TAG_REGISTRABLE_COURSE_LIST = "registrable course list"

internal fun testTagForRegistrableCourse(courseId: Long) = "registrableCourse$courseId"

@Serializable
private data object CourseRegistrationScreen

fun NavController.navigateToCourseRegistration(builder: NavOptionsBuilder.() -> Unit) {
    navigate(CourseRegistrationScreen, builder)
}

fun NavGraphBuilder.courseRegistration(
    onNavigateUp: () -> Unit,
    onRegisteredInCourse: (courseId: Long) -> Unit
) {
    composable<CourseRegistrationScreen> {
        RegisterForCourseScreen(
            modifier = Modifier.fillMaxSize(),
            viewModel = koinViewModel(),
            onNavigateUp = onNavigateUp,
            onRegisteredInCourse = onRegisteredInCourse
        )
    }
}

@Composable
internal fun RegisterForCourseScreen(
    modifier: Modifier,
    viewModel: RegisterForCourseViewModel = koinViewModel(),
    onNavigateUp: () -> Unit,
    onRegisteredInCourse: (courseId: Long) -> Unit
) {
    val courses by viewModel.registrableCourses.collectAsState()

    var signUpCandidate: Course? by remember { mutableStateOf(null) }
    var displayRegistrationFailedDialog: Boolean by rememberSaveable { mutableStateOf(false) }

    val topAppBarState = rememberTopAppBarState()

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(
        topAppBarState
    )

    var courseRegistrationDeferred: Deferred<Long?>? by remember { mutableStateOf(null) }

    AwaitDeferredCompletion(courseRegistrationDeferred) { courseId ->
        if (courseId != null) {
            onRegisteredInCourse(courseId)
        } else {
            displayRegistrationFailedDialog = true
        }

        signUpCandidate = null
        courseRegistrationDeferred = null
    }

    Scaffold(
        modifier = modifier.then(Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)),
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.course_registration_title)) },
                scrollBehavior = scrollBehavior,
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        RegisterForCourseContent(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = padding.calculateTopPadding())
                .consumeWindowInsets(WindowInsets.systemBars)
                .padding(horizontal = 8.dp),
            courses = courses,
            reloadCourses = viewModel::reloadRegistrableCourses,
            onClickSignup = { course ->
                signUpCandidate = course
            }
        )

        signUpCandidate?.let { candidate ->
            AlertDialog(
                onDismissRequest = { signUpCandidate = null },
                confirmButton = {
                    Button(
                        onClick = {
                            courseRegistrationDeferred = viewModel.registerInCourse(candidate)
                        }
                    ) {
                        Text(text = stringResource(id = R.string.course_registration_sign_up_dialog_positive_button))
                    }
                },
                text = {
                    if (candidate.registrationConfirmationMessage.isNotBlank()) {
                        MarkdownText(markdown = candidate.registrationConfirmationMessage)
                    } else {
                        Text(text = stringResource(id = R.string.course_registration_sign_up_dialog_message))
                    }
                },
                title = if (candidate.registrationConfirmationMessage.isBlank()) {
                    { Text(text = candidate.title) }
                } else null
            )
        }

        if (displayRegistrationFailedDialog) {
            AlertDialog(
                onDismissRequest = { displayRegistrationFailedDialog = false },
                title = { Text(text = stringResource(id = R.string.course_registration_sign_up_failed_dialog_title)) },
                text = { Text(text = stringResource(id = R.string.course_registration_sign_up_failed_dialog_message)) },
                confirmButton = {
                    TextButton(onClick = {
                        displayRegistrationFailedDialog = false
                    }) {
                        Text(text = stringResource(id = R.string.course_registration_sign_up_failed_dialog_positive))
                    }
                }
            )
        }
    }
}

@Composable
private fun RegisterForCourseContent(
    modifier: Modifier,
    courses: DataState<List<RegisterForCourseViewModel.SemesterCourses>>,
    reloadCourses: () -> Unit,
    onClickSignup: (Course) -> Unit
) {
    BasicDataStateUi(
        modifier = modifier,
        dataState = courses,
        loadingText = stringResource(id = R.string.course_registration_loading_courses_loading),
        failureText = stringResource(id = R.string.course_registration_loading_courses_failed),
        retryButtonText = stringResource(id = R.string.course_registration_loading_courses_try_again),
        onClickRetry = reloadCourses
    ) { data ->
        val windowSizeClass = getWindowSizeClass()

        val columnCount = computeCourseColumnCount(windowSizeClass)
        val isCompact = windowSizeClass.widthSizeClass <= WindowWidthSizeClass.Compact
        val courseItemModifier = Modifier
            .computeCourseItemModifier(isCompact = isCompact)

        if(data.isEmpty()) {
            Column(
                modifier = Modifier.align(Alignment.Center)
            ) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(id = R.string.course_registration_no_courses),
                    textAlign = TextAlign.Center,
                    fontSize = 18.sp
                )
            }
        }

        LazyVerticalGrid(
            modifier = Modifier
                .fillMaxSize()
                .testTag(TEST_TAG_REGISTRABLE_COURSE_LIST),
            columns = GridCells.Fixed(columnCount),
            contentPadding = WindowInsets.systemBars.asPaddingValues(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            data.forEach { semesterCourses ->
                item(span = { GridItemSpan(columnCount) }) {
                    SemesterHeader(
                        modifier = Modifier.fillMaxWidth(),
                        semester = semesterCourses.semester
                    )
                }

                items(semesterCourses.courses, key = { it.id ?: 0L }) { course ->
                    RegistrableCourse(
                        modifier = courseItemModifier.testTag(testTagForRegistrableCourse(course.id ?: 0L)),
                        course = course,
                        onClickSignup = { onClickSignup(course) },
                        isCompact = isCompact
                    )
                }
            }
        }
    }
}

@Composable
private fun RegistrableCourse(
    modifier: Modifier,
    course: Course,
    isCompact: Boolean,
    onClickSignup: () -> Unit
) {
    val content: @Composable ColumnScope.() -> Unit = @Composable {
        HorizontalDivider()

        Row(
            modifier = Modifier
                .padding(top = 4.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                modifier = Modifier.padding(bottom = 4.dp, end = 8.dp),
                onClick = onClickSignup
            ) {
                Text(text = stringResource(id = R.string.course_registration_sign_up))
            }
        }
    }

    if (isCompact) {
        CompactCourseItemHeader(
            modifier = modifier,
            course = course,
            content = content,
            compactCourseHeaderViewMode = CompactCourseHeaderViewMode.DESCRIPTION
        )
    } else {
        ExpandedCourseItemHeader(
            modifier = modifier,
            course = course,
            content = {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .weight(1f),
                    text = course.description,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium
                )

                content()
            },
            rightHeaderContent = {}
        )
    }
}

@Composable
private fun SemesterHeader(modifier: Modifier, semester: String) {
    Box(modifier = modifier.then(Modifier.background(MaterialTheme.colorScheme.onPrimary))) {
        Text(
            text = semester,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .padding(horizontal = 16.dp + 8.dp, vertical = 2.dp)
        )
    }
}
