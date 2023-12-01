package de.tum.informatics.www1.artemis.native_app.core.ui

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

val LocalCourseImageProvider = compositionLocalOf<CourseImageProvider> { CourseImageProviderImpl }

interface CourseImageProvider {
    @Composable
    fun rememberCourseImagePainter(
        courseIconPath: String,
        serverUrl: String,
        authorizationToken: String
    ): Painter
}

private object CourseImageProviderImpl : CourseImageProvider {
    @Composable
    override fun rememberCourseImagePainter(
        courseIconPath: String,
        serverUrl: String,
        authorizationToken: String
    ): Painter = rememberAsyncImagePainter(
        model = getCourseIconRequest(
            serverUrl = serverUrl,
            courseIconPath = courseIconPath,
            authorizationToken = authorizationToken
        )
    )

    @Composable
    private fun getCourseIconRequest(
        serverUrl: String,
        courseIconPath: String,
        authorizationToken: String
    ): ImageRequest {
        val courseIconUrl = remember(serverUrl, courseIconPath) {
            URLBuilder(serverUrl)
                .appendPathSegments(courseIconPath)
                .buildString()
        }

        val context = LocalContext.current

        //Authorization needed
        return remember {
            ImageRequest.Builder(context)
                .addHeader(HttpHeaders.Cookie, "jwt=$authorizationToken")
                .data(courseIconUrl)
                .build()
        }
    }
}