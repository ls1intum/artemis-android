package de.tum.informatics.www1.artemis.native_app.core.ui.remote_images

import android.content.Context
import coil.ImageLoader
import coil.request.ImageRequest
import io.ktor.http.HttpHeaders
import io.ktor.http.URLBuilder
import io.ktor.http.appendPathSegments

open class DefaultImageProvider : BaseImageProvider {
    override fun createImageRequest(
        context: Context,
        imagePath: String,
        serverUrl: String,
        authorizationToken: String,
        memoryCacheKey: String?
    ): ImageRequest {
        val imageUrl = URLBuilder(serverUrl).appendPathSegments(imagePath).buildString()

        val builder = ImageRequest.Builder(context)
            .addHeader(HttpHeaders.Cookie, "jwt=$authorizationToken")
            .data(imageUrl)

        memoryCacheKey?.let {
            builder.memoryCacheKey(it)
        }
        return builder.build()
    }


    override fun createImageLoader(
        context: Context,
        authorizationToken: String
    ): ImageLoader {
        return ImageLoader.Builder(context)
            .okHttpClient {
                okhttp3.OkHttpClient.Builder()
                    .addInterceptor { chain ->
                        val request = chain.request().newBuilder()
                            .addHeader(HttpHeaders.Cookie, "jwt=$authorizationToken")
                            .build()
                        chain.proceed(request)
                    }
                    .build()
            }
            .build()
    }
}
