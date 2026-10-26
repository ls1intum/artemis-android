package de.tum.informatics.www1.artemis.native_app.core.ui.media

import okhttp3.OkHttpClient

/** Network interceptors run again for each redirect, unlike application interceptors. */
private class MediaRedirectScope(val initialOrigin: String?) {
    var crossedOrigin: Boolean = false
}

fun mediaHttpClient(serverUrl: String, authToken: String): OkHttpClient = OkHttpClient.Builder()
    .addInterceptor { chain ->
        val request = chain.request().newBuilder()
            .tag(MediaRedirectScope::class.java, MediaRedirectScope(normalizedMediaOrigin(chain.request().url.toString())))
            .build()
        chain.proceed(request)
    }
    .addNetworkInterceptor { chain ->
        val request = chain.request().newBuilder()
            .removeHeader("Cookie")
            .removeHeader("Authorization")
        val scope = chain.request().tag(MediaRedirectScope::class.java)
        val origin = normalizedMediaOrigin(chain.request().url.toString())
        if (scope != null && origin != scope.initialOrigin) scope.crossedOrigin = true
        if (authToken.isNotBlank() && scope?.crossedOrigin == false &&
            scope.initialOrigin == normalizedMediaOrigin(serverUrl) && sameMediaOrigin(serverUrl, chain.request().url.toString())) {
            request.header("Cookie", "jwt=$authToken")
        }
        chain.proceed(request.build())
    }
    .build()

/** Cancels both the pending request and an in-progress response read with the owning UI scope. */
suspend fun <T> withMediaResponse(client: OkHttpClient, request: okhttp3.Request, read: (okhttp3.Response) -> T): T =
    kotlinx.coroutines.suspendCancellableCoroutine { continuation ->
        val call = client.newCall(request)
        continuation.invokeOnCancellation { call.cancel() }
        call.enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, error: java.io.IOException) {
                if (continuation.isActive) continuation.resumeWith(Result.failure(error))
            }
            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                response.use {
                    try {
                        if (continuation.isActive) continuation.resumeWith(Result.success(read(it)))
                    } catch (error: Exception) {
                        if (continuation.isActive) continuation.resumeWith(Result.failure(error))
                    }
                }
            }
        })
    }
