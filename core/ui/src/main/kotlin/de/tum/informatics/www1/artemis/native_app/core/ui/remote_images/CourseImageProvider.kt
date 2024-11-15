package de.tum.informatics.www1.artemis.native_app.core.ui.remote_images

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import io.ktor.http.HttpHeaders
import io.ktor.http.URLBuilder
import io.ktor.http.appendPathSegments

val LocalCourseImageProvider = compositionLocalOf<CourseImageProvider> { DefaultCourseImageProvider }

interface CourseImageProvider {
    @Composable
    fun rememberCourseImagePainter(
        courseIconPath: String,
        serverUrl: String,
        authorizationToken: String
    ): Painter
}

private object DefaultCourseImageProvider : CourseImageProvider {
    private val imageProvider = DefaultImageProvider()

    @Composable
    override fun rememberCourseImagePainter(
        courseIconPath: String,
        serverUrl: String,
        authorizationToken: String
    ): Painter {
        val context = LocalContext.current
        val imageRequest = remember {
            imageProvider.createImageRequest(context, courseIconPath, serverUrl, authorizationToken)
        }
        return rememberAsyncImagePainter(model = imageRequest)
    }
}
