package de.tum.informatics.www1.artemis.native_app.core.ui.common.image

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.imageLoader
import coil.request.ErrorResult
import coil.request.ImageRequest
import coil.request.SuccessResult
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

internal class RetryableAsyncImageViewModel(context: Context, private val request: ImageRequest) :
    ViewModel() {

    private val retryFlow = MutableSharedFlow<Unit>()

    val drawableDataState: StateFlow<DataState<Drawable>> = retryFlow
        .onStart { emit(Unit) }
        .transformLatest {
            emit(DataState.Loading())

            when (val result = context.imageLoader.execute(request)) {
                is ErrorResult -> {
                    emit(DataState.Failure(result.throwable))
                }

                is SuccessResult -> {
                    emit(DataState.Success(result.drawable))
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, DataState.Loading())

    fun requestRetry() {
        viewModelScope.launch {
            retryFlow.emit(Unit)
        }
    }
}

@Composable
fun loadAsyncImageDrawable(request: ImageRequest): AsyncImageDrawableResult {
    val viewModel: RetryableAsyncImageViewModel = koinViewModel { parametersOf(request) }

    val drawable by viewModel.drawableDataState.collectAsState()
    return AsyncImageDrawableResult(drawable, viewModel::requestRetry)
}

data class AsyncImageDrawableResult(val dataState: DataState<Drawable>, val requestRetry: () -> Unit)
