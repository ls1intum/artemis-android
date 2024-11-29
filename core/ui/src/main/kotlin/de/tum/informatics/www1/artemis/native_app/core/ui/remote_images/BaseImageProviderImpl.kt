package de.tum.informatics.www1.artemis.native_app.core.ui.remote_images

import android.content.Context
import coil3.ImageLoader
import coil3.network.NetworkHeaders
import coil3.network.httpHeaders
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.request.ImageRequest
import io.ktor.http.HttpHeaders
import io.ktor.http.URLBuilder
import io.ktor.http.appendPathSegments

class BaseImageProviderImpl : BaseImageProvider {
    override fun createImageRequest(
        context: Context,
        imagePath: String,
        serverUrl: String,
        authorizationToken: String,
        memoryCacheKey: String?
    ): ImageRequest {
        val imageUrl = URLBuilder(serverUrl).appendPathSegments(imagePath).buildString()

        val headers = NetworkHeaders.Builder()
            .set(HttpHeaders.Cookie, "jwt=$authorizationToken")
            .build()

        val builder = ImageRequest.Builder(context)
            .httpHeaders(headers)
            .data(imageUrl)
            // The following line is needed to for the AsyncImagePainter to work correctly, see:
            // https://stackoverflow.com/a/74705550/13366254
            // TODO is this needed: .size(coil.size.OriginalSize)

        memoryCacheKey?.let {
            builder.memoryCacheKey(it)
        }
        return builder.build()
    }

    override fun createImageLoader(
        context: Context,
        authorizationToken: String
    ): ImageLoader {
        val okHttpClient = okhttp3.OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader(HttpHeaders.Cookie, "jwt=$authorizationToken")
                    .build()
                chain.proceed(request)
            }
            .build()

        return ImageLoader.Builder(context)
            .components {
                add(OkHttpNetworkFetcherFactory(
                    callFactory = okHttpClient
                ))
            }
            .build()
    }
}
