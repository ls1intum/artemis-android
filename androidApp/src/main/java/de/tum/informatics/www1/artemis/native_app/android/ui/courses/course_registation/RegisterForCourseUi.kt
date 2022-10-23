package de.tum.informatics.www1.artemis.native_app.android.ui.courses.course_registation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.android.R
import de.tum.informatics.www1.artemis.native_app.android.content.Course
import de.tum.informatics.www1.artemis.native_app.android.service.AccountService
import de.tum.informatics.www1.artemis.native_app.android.service.ServerCommunicationProvider
import de.tum.informatics.www1.artemis.native_app.android.ui.common.BasicDataStateUi
import de.tum.informatics.www1.artemis.native_app.android.ui.courses.CourseItemHeader
import de.tum.informatics.www1.artemis.native_app.android.util.DataState
import de.tum.informatics.www1.artemis.native_app.android.util.withoutLastChar
import dev.jeziellago.compose.markdowntext.MarkdownText
import org.koin.androidx.compose.get
import org.koin.androidx.compose.getViewModel

@Composable
fun RegisterForCourseScreen(
    modifier: Modifier,
    viewModel: RegisterForCourseViewModel = getViewModel(),
    onNavigateUp: () -> Unit,
    onRegisteredInCourse: (courseId: Int) -> Unit
) {
    val courses by viewModel.registrableCourses.collectAsState(initial = DataState.Loading())

    val accountService: AccountService = get()
    val serverCommunicationProvider: ServerCommunicationProvider = get()

    var signUpCandidate: Course? by remember { mutableStateOf(null) }
    var displayRegistrationFailedDialog: Boolean by rememberSaveable { mutableStateOf(false) }

    val authData = accountService.authenticationData.collectAsState(initial = null).value
    val serverUrl = serverCommunicationProvider.serverUrl.collectAsState(initial = null).value

    //CourseHeader requires url without trailing /
    val properServerUrl = remember(serverUrl) { serverUrl?.withoutLastChar() }

    if (properServerUrl == null || authData == null) return

    val bearerToken = when (authData) {
        is AccountService.AuthenticationData.LoggedIn -> authData.asBearer
        AccountService.AuthenticationData.NotLoggedIn -> ""
    }

    val topAppBarState = rememberTopAppBarState()

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(
        topAppBarState
    )

    Scaffold(
        modifier = modifier.then(Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)),
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.course_registration_title)) },
                scrollBehavior = scrollBehavior,
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        RegisterForCourseContent(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            courses = courses,
            serverUrl = properServerUrl,
            bearerToken = bearerToken,
            reloadCourses = viewModel::reloadRegistrableCourses,
            onClickSignup = { course ->
                signUpCandidate = course
            }
        )

        val candidate = signUpCandidate
        if (candidate != null) {
            AlertDialog(
                onDismissRequest = { signUpCandidate = null },
                confirmButton = {
                    Button(onClick = {
                        viewModel.registerInCourse(
                            candidate,
                            onSuccess = {
                                signUpCandidate = null
                                onRegisteredInCourse(candidate.id)
                            },
                            onFailure = {
                                signUpCandidate = null
                                displayRegistrationFailedDialog = true
                            }
                        )
                    }) {
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
    serverUrl: String,
    bearerToken: String,
    reloadCourses: () -> Unit,
    onClickSignup: (Course) -> Unit
) {
    BasicDataStateUi(
        modifier = modifier,
        dataState = courses,
        loadingText = stringResource(id = R.string.course_registration_loading_courses_loading),
        failureText = stringResource(id = R.string.course_registration_loading_courses_failed),
        suspendedText = stringResource(id = R.string.course_registration_loading_courses_suspended),
        retryButtonText = stringResource(id = R.string.course_registration_loading_courses_try_again),
        onClickRetry = reloadCourses
    ) { data ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            data.forEach { semesterCourses ->
                stickyHeader {
                    SemesterHeader(
                        modifier = Modifier.fillMaxWidth(),
                        semester = semesterCourses.semester
                    )
                }

                items(semesterCourses.courses) { course ->
                    RegistrableCourse(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        course = course,
                        serverUrl = serverUrl,
                        bearerToken = bearerToken,
                        onClickSignup = { onClickSignup(course) }
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
    serverUrl: String,
    bearerToken: String,
    onClickSignup: () -> Unit
) {
    CourseItemHeader(
        modifier = modifier,
        course = course,
        serverUrl = serverUrl,
        authorizationToken = bearerToken
    ) {
        Divider()

        Row(
            modifier = Modifier
                .padding(top = 4.dp)
                .fillMaxWidth(), horizontalArrangement = Arrangement.End
        ) {
            Button(
                modifier = Modifier.padding(bottom = 4.dp, end = 8.dp),
                onClick = onClickSignup
            ) {
                Text(text = stringResource(id = R.string.course_registration_sign_up))
            }
        }
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