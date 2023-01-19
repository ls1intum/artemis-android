package de.tum.informatics.www1.artemis.native_app.core.ui.common.course

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import de.tum.informatics.www1.artemis.native_app.core.model.Course
import de.tum.informatics.www1.artemis.native_app.core.ui.common.AutoResizeText
import de.tum.informatics.www1.artemis.native_app.core.ui.common.FontSizeRange
import de.tum.informatics.www1.artemis.native_app.core.ui.getWindowSizeClass

private val headerHeight = 80.dp

@Composable
fun CourseItemGrid(
    modifier: Modifier,
    courses: List<Course>,
    courseItem: @Composable LazyGridItemScope.(Course, Modifier, isCompact: Boolean) -> Unit
) {
    val windowSizeClass = getWindowSizeClass()
    val columnCount = computeCourseColumnCount(windowSizeClass)

    val isCompact = windowSizeClass.widthSizeClass <= WindowWidthSizeClass.Compact
    val courseItemModifier = computeCourseItemModifier(isCompact)

    LazyVerticalGrid(
        modifier = modifier,
        columns = GridCells.Fixed(columnCount),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        items(courses, key = Course::id) { course ->
            courseItem(course, courseItemModifier, isCompact)
        }
    }
}

@Composable
fun computeCourseItemModifier(isCompact: Boolean): Modifier {
    return if (isCompact) {
        Modifier
            .fillMaxWidth()
    } else {
        Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
    }
}

/**
 * Displays the cource icon on left, with the title and the description in a column on the right to it.
 */
@Composable
fun CompactCourseItemHeader(
    modifier: Modifier,
    course: Course,
    serverUrl: String,
    authorizationToken: String,
    onClick: () -> Unit = {},
    content: @Composable ColumnScope.() -> Unit
) {
    val painter = getCourseIconPainter(course, serverUrl, authorizationToken)

    Card(modifier = modifier, onClick = onClick) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(headerHeight)
            ) {
                Image(
                    modifier = Modifier
                        .size(headerHeight),
                    painter = painter,
                    contentDescription = null
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(horizontal = 8.dp)
                ) {
                    AutoResizeText(
                        text = course.title,
                        modifier = Modifier
                            .fillMaxWidth(),
                        fontSizeRange = FontSizeRange(min = 14.sp, max = 22.sp),
                        fontWeight = FontWeight.Bold,
                        maxLines = 2
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
                        maxLines = 3
                    )
                }
            }

            content()
        }
    }
}

@Composable
private fun getCourseIconPainter(
    course: Course,
    serverUrl: String,
    authorizationToken: String
): Painter {
    val painter = if (course.courseIconPath != null) {
        rememberAsyncImagePainter(
            model = getCourseIconRequest(
                serverUrl = serverUrl,
                course = course,
                authorizationToken = authorizationToken
            )
        )
    } else rememberVectorPainter(image = Icons.Default.QuestionMark)
    return painter
}

@Composable
private fun getCourseIconRequest(
    serverUrl: String,
    course: Course,
    authorizationToken: String
): ImageRequest {
    val courseIconUrl = "$serverUrl${course.courseIconPath}"

    val context = LocalContext.current

    //Authorization needed
    return remember {
        ImageRequest.Builder(context)
            .addHeader("Authorization", authorizationToken)
            .data(courseIconUrl)
            .build()
    }
}

@Composable
fun ExpandedCourseItemHeader(
    modifier: Modifier,
    course: Course,
    serverUrl: String,
    authorizationToken: String,
    onClick: () -> Unit = {},
    content: @Composable ColumnScope.() -> Unit
) {
    val courseIconPainter = getCourseIconPainter(course, serverUrl, authorizationToken)

    val courseColor: Color? = remember {
        try {
            course.color?.toColorInt()?.let { Color(it) }
        } catch (e: IllegalArgumentException) {
            null
        }
    }

    Card(modifier = modifier, onClick = onClick) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .let {
                    if (courseColor != null) {
                        it.background(courseColor)
                    } else it
                }
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val courseIconModifier = Modifier
                .fillMaxWidth(0.2f)
                .aspectRatio(1f)
            if (course.courseIconPath != null) {
                Image(
                    modifier = courseIconModifier.clip(CircleShape),
                    painter = courseIconPainter,
                    contentDescription = null
                )
            } else {
                Box(modifier = courseIconModifier)
            }

            AutoResizeText(
                modifier = Modifier.weight(1f),
                text = course.title,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                maxLines = 1,
                fontSizeRange = FontSizeRange(10.sp, max = 18.sp)
            )

            Box(modifier = courseIconModifier)
        }

        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .weight(1f),
            text = course.description,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodySmall
        )

        content()
    }
}

fun computeCourseColumnCount(windowSizeClass: WindowSizeClass): Int = when {
    windowSizeClass.widthSizeClass >= WindowWidthSizeClass.Expanded -> 4
    windowSizeClass.widthSizeClass >= WindowWidthSizeClass.Medium -> 2
    else -> 1
}
