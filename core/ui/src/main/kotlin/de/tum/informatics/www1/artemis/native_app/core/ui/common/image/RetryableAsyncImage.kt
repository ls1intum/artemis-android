package de.tum.informatics.www1.artemis.native_app.core.ui.common.image

import android.graphics.drawable.Drawable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import coil.imageLoader
import coil.request.ErrorResult
import coil.request.ImageRequest
import coil.request.SuccessResult
import de.tum.informatics.www1.artemis.native_app.core.data.DataState

@Composable
fun loadAsyncImageDrawable(request: ImageRequest): AsyncImageDrawableResult {
    val context = LocalContext.current
    var dataState: DataState<Drawable> by remember(request) { mutableStateOf(DataState.Loading()) }

    // We simply increase this counter to trigger a reload
    var reloadCounter by remember(request) { mutableStateOf(0) }

    LaunchedEffect(request, reloadCounter) {
        dataState = DataState.Loading()
        dataState = when (val result = context.imageLoader.execute(request)) {
            is ErrorResult -> {
                DataState.Failure(result.throwable)
            }

            is SuccessResult -> {
                DataState.Success(result.drawable)
            }
        }
    }

    val result by remember(dataState) {
        derivedStateOf {
            AsyncImageDrawableResult(dataState) {
                reloadCounter++
            }
        }
    }

    return result
}

data class AsyncImageDrawableResult(
    val dataState: DataState<Drawable>,
    val requestRetry: () -> Unit
)
