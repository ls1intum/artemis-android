package de.tum.informatics.www1.artemis.native_app.android.ui.courses_overview

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import de.tum.informatics.www1.artemis.native_app.android.content.Course
import de.tum.informatics.www1.artemis.native_app.android.util.DataState
import de.tum.informatics.www1.artemis.native_app.android.util.NetworkResponse

/**
 * Displays the Course Overview screen.
 * Uses Scaffold to display a Material Design TopAppBar.
 */
@Composable
fun CoursesOverview(modifier: Modifier, viewModel: CourseOverviewViewModel, onLogout: () -> Unit) {
    val scope = rememberCoroutineScope()

    val courses = viewModel.dashboard.collectAsState(initial = DataState.Loading()).value

    //The course composable needs the serverUrl to build the correct url to fetch the course icon from.
    val serverUrl by viewModel.serverUrl.collectAsState(initial = "")
    //The server wants an authorization token to send the course icon.
    val authorizationBearer by viewModel.authorizationBearerToken.collectAsState(initial = "")

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(title = { Text(text = "Course Overview") }, actions = {
                IconButton(onClick = { viewModel.logout(onLogout) }) {
                    Icon(imageVector = Icons.Default.Logout, contentDescription = null)
                }
            })
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
                is DataState.Done -> {
                    when (val response = courses.response) {
                        is NetworkResponse.Response -> {
                            CourseList(
                                modifier = Modifier.fillMaxSize(),
                                courses = response.data.courses,
                                serverUrl = serverUrl,
                                authorizationToken = authorizationBearer
                            )
                        }
                        is NetworkResponse.Failure -> {
                            Text(
                                text = "Could not load dashboard. ${response.exception}",
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    }
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
    authorizationToken: String
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(courses) { course ->
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .height(IntrinsicSize.Min)
        ) {
            Image(
                modifier = Modifier
                    .fillMaxHeight()
                    .aspectRatio(1f)
                    .clip(CircleShape),
                painter = rememberAsyncImagePainter(model = courseIconRequest),
                contentDescription = null
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
            ) {
                Text(
                    text = course.title,
                    modifier = Modifier.fillMaxWidth(),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = course.description,
                    modifier = Modifier.fillMaxWidth(),
                    fontStyle = FontStyle.Italic,
                    maxLines = 1
                )
            }
        }
    }
}