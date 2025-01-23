package de.tum.informatics.www1.artemis.native_app.core.ui.remote_images.impl

import android.content.Context
import coil.ImageLoader
import coil3.network.NetworkHeaders
import coil3.network.httpHeaders
import coil3.request.ImageRequest
import de.tum.informatics.www1.artemis.native_app.core.ui.remote_images.BaseImageProvider
import io.ktor.http.HttpHeaders

class BaseImageProviderImpl : BaseImageProvider {
    override fun createImageRequest(
        context: Context,
        imageUrl: String,
        authorizationToken: String,
    ): ImageRequest {
        val headers = NetworkHeaders.Builder()
            .set(HttpHeaders.Cookie, "jwt=$authorizationToken")
            .build()

        val builder = ImageRequest.Builder(context)
            .httpHeaders(headers)
            .data(imageUrl)

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
