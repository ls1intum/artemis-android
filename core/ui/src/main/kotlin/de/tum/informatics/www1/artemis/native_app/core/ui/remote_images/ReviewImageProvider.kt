package de.tum.informatics.www1.artemis.native_app.core.ui.remote_images

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.core.net.toUri
import coil3.compose.AsyncImagePainter
import coil3.compose.rememberAsyncImagePainter
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.request.ImageRequest
import coil3.request.ImageResult
import coil3.request.CachePolicy
import de.tum.informatics.www1.artemis.native_app.core.common.artemis_context.ArtemisContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import okhttp3.OkHttpClient
import java.net.URI

/** Private, cache-free readers for one captured review account. Never uses the app's global image cache. */
class ReviewImageProvider(
    private val context: Context,
    private val session: ArtemisContext.LoggedIn,
    private val onDenied: () -> Unit,
) : ArtemisImageProvider, AutoCloseable {
    private val failed = MutableStateFlow(false)
    val hasImageFailure: StateFlow<Boolean> = failed
    private val origin = URI(session.serverUrl)
    private fun sameOrigin(uri: URI): Boolean = uri.scheme == origin.scheme && uri.host == origin.host && effectivePort(uri) == effectivePort(origin)
    private fun effectivePort(uri: URI): Int = if (uri.port >= 0) uri.port else if (uri.scheme == "https") 443 else 80

    fun resolve(imagePath: String): String {
        val raw = URI(imagePath.replace(" ", "%20"))
        require(raw.scheme == null || raw.scheme in listOf("http", "https")) { "Unsupported review image" }
        val resolved = if (raw.isAbsolute || imagePath.startsWith("//")) origin.resolve(raw) else {
            val path = imagePath.removePrefix("/")
            URI(session.serverUrl.trimEnd('/') + "/").resolve(if (path.startsWith("api/") || path.startsWith("public/")) path else "api/core/files/$path")
        }
        require(resolved.scheme in listOf("http", "https") && resolved.userInfo == null)
        return resolved.toASCIIString()
    }

    private val client = OkHttpClient.Builder()
        .followRedirects(false).followSslRedirects(false)
        .cache(null)
        .addInterceptor { chain ->
            val original = chain.request()
            val protected = sameOrigin(original.url.toUri())
            val request = original.newBuilder().removeHeader("Cookie").removeHeader("Authorization")
                .apply { if (protected) header("Cookie", "jwt=${session.authToken}") }.build()
            try {
                chain.proceed(request).also { response ->
                    if (!response.isSuccessful) {
                        failed.value = true
                        if (protected && response.code in listOf(401, 403)) onDenied()
                    }
                }
            } catch (error: Exception) { failed.value = true; throw error }
        }.build()

    val markdownImageLoader: coil.ImageLoader = coil.ImageLoader.Builder(context)
        .okHttpClient(client)
        .memoryCache(null).diskCache(null)
        .eventListener(object : coil.EventListener {
            override fun onError(request: coil.request.ImageRequest, result: coil.request.ErrorResult) { failed.value = true }
        })
        .components {
            add(object : coil.map.Mapper<String, android.net.Uri> {
                override fun map(data: String, options: coil.request.Options): android.net.Uri = try { resolve(data).toUri() }
                catch (error: Exception) { failed.value = true; throw error }
            })
        }
        .build()

    private val imageLoader = coil3.ImageLoader.Builder(context)
        .memoryCache(null).diskCache(null)
        .components { add(OkHttpNetworkFetcherFactory(callFactory = { client })) }
        .build()

    private fun request(imagePath: String): ImageRequest = ImageRequest.Builder(context)
        .data(runCatching { resolve(imagePath) }.getOrElse { failed.value = true; "unsupported-review-image:" })
        .memoryCachePolicy(CachePolicy.DISABLED).diskCachePolicy(CachePolicy.DISABLED)
        .listener(onError = { _, _ -> failed.value = true })
        .build()

    override suspend fun loadArtemisImage(context: Context, imagePath: String): ImageResult = imageLoader.execute(request(imagePath))
    @Composable override fun rememberArtemisImageRequest(imagePath: String): ImageRequest = remember(imagePath) { request(imagePath) }
    @Composable override fun rememberArtemisImageRequestByUrl(imageUrl: String): ImageRequest = remember(imageUrl) { request(imageUrl) }
    @Composable override fun rememberArtemisAsyncImagePainter(imagePath: String): AsyncImagePainter = rememberAsyncImagePainter(rememberArtemisImageRequest(imagePath), imageLoader)
    @Composable override fun rememberArtemisAsyncImagePainterByUrl(imageUrl: String): AsyncImagePainter = rememberAsyncImagePainter(rememberArtemisImageRequestByUrl(imageUrl), imageLoader)
    @Composable override fun rememberArtemisImageLoader(): coil.ImageLoader = markdownImageLoader

    override fun close() {
        imageLoader.shutdown()
        markdownImageLoader.shutdown()
        client.dispatcher.cancelAll()
        client.connectionPool.evictAll()
    }
}
