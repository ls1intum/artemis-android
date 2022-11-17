package de.tum.informatics.www1.artemis.native_app.core.ui.common.course

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import de.tum.informatics.www1.artemis.native_app.android.model.Course
import de.tum.informatics.www1.artemis.native_app.core.ui.common.AutoResizeText
import de.tum.informatics.www1.artemis.native_app.core.ui.common.FontSizeRange

@Composable
fun CourseItemHeader(
    modifier: Modifier,
    course: Course,
    serverUrl: String,
    authorizationToken: String,
    onClick: () -> Unit = {},
    content: @Composable ColumnScope.() -> Unit
) {
    val courseIconUrl = "$serverUrl${course.courseIconPath}"

    val context = LocalContext.current

    //Authorization needed
    val courseIconRequest = ImageRequest.Builder(context)
        .addHeader("Authorization", authorizationToken)
        .data(courseIconUrl)
        .build()
    
    Card(modifier = modifier, onClick = onClick) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(0.dp)
        ) {
            val headerHeight = 80.dp
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(headerHeight)
            ) {
                val painter = if (course.courseIconPath != null) {
                    rememberAsyncImagePainter(model = courseIconRequest)
                } else rememberVectorPainter(image = Icons.Default.QuestionMark)

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