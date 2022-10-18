package de.tum.informatics.www1.artemis.native_app.android.ui.courses_overview

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import de.tum.informatics.www1.artemis.native_app.android.R
import de.tum.informatics.www1.artemis.native_app.android.content.Course
import de.tum.informatics.www1.artemis.native_app.android.util.DataState
import de.tum.informatics.www1.artemis.native_app.android.util.compose.isScrollingUp
import java.text.DecimalFormat
import java.text.NumberFormat
import kotlin.math.roundToInt

/**
 * Displays the Course Overview screen.
 * Uses Scaffold to display a Material Design TopAppBar.
 */
@Composable
fun CoursesOverview(
    modifier: Modifier,
    viewModel: CourseOverviewViewModel,
    onLogout: () -> Unit,
    onClickRegisterForCourse: () -> Unit
) {
    val courses = viewModel.dashboard.collectAsState(initial = DataState.Loading()).value

    //The course composable needs the serverUrl to build the correct url to fetch the course icon from.
    val serverUrl by viewModel.serverUrl.collectAsState(initial = "")
    //The server wants an authorization token to send the course icon.
    val authorizationBearer by viewModel.authorizationBearerToken.collectAsState(initial = "")

    val coursesListState = rememberLazyListState()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(title = { Text(text = "Course Overview") }, actions = {
                IconButton(onClick = { viewModel.logout(onLogout) }) {
                    Icon(imageVector = Icons.Default.Logout, contentDescription = null)
                }
            })
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onClickRegisterForCourse,
                text = {
                    Text(text = stringResource(id = R.string.course_overview_fab_text))
                },
                icon = {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null)
                },
                expanded = coursesListState.isScrollingUp(),
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (courses) {
                is DataState.Loading -> CircularProgressIndicator(
                    modifier = Modifier.align(
                        Alignment.Center
                    )
                )
                is DataState.Success -> {
                    CourseList(
                        modifier = Modifier.fillMaxSize(),
                        courses = courses.data.courses,
                        serverUrl = serverUrl,
                        authorizationToken = authorizationBearer,
                        listState = coursesListState
                    )
                }
                else -> {

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
    listState: LazyListState
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(bottom = 90.dp),
        state = listState
    ) {
        items(courses + courses + courses + courses + courses) { course ->
            CourseItem(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                course = course,
                serverUrl = serverUrl,
                authorizationToken = authorizationToken
            )
        }
    }
}

/**
 * Displays course icon, title and description in a Material Design Card.
 */
@Composable
private fun CourseItem(
    modifier: Modifier,
    course: Course,
    serverUrl: String,
    authorizationToken: String
) {
    val courseIconUrl = "$serverUrl${course.courseIconPath}"

    val context = LocalContext.current

    //Authorization needed
    val courseIconRequest = ImageRequest.Builder(context)
        .addHeader("Authorization", authorizationToken)
        .data(courseIconUrl)
        .build()

    Card(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(0.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
            ) {
                Image(
                    modifier = Modifier
                        .size(80.dp),
                    painter = rememberAsyncImagePainter(model = courseIconRequest),
                    contentDescription = null
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(horizontal = 8.dp)
                ) {
                    Text(
                        text = course.title,
                        modifier = Modifier
                            .fillMaxWidth(),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )

                    Text(
                        text = course.description,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        color = MaterialTheme.colorScheme.secondary,
                        fontSize = 12.sp,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 14.sp,
                    )
                }
            }

            Divider()

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

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LinearProgressIndicator(
                    modifier = Modifier.weight(1f),
                    progress = course.progress,
                    trackColor = MaterialTheme.colorScheme.onPrimary
                )

                Text(
                    text = stringResource(
                        id = R.string.course_overview_course_progress,
                        scoreNumberFormat.format(course.currentScore),
                        scoreNumberFormat.format(course.maxPointsPossible),
                        percentNumberFormat.format(course.progress)
                    ),
                    fontSize = 14.sp
                )
            }
        }

    }
}