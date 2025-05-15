package de.tum.informatics.www1.artemis.native_app.core.ui.common

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.SignalWifiOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.ui.R
import de.tum.informatics.www1.artemis.native_app.core.ui.Spacings


/**
 * Displays ui for each data state.
 * For loading, a progress bar with text on top of it
 * For suspended and failure, text with a try again button.
 */
@Composable
inline fun <T> BasicDataStateUi(
    modifier: Modifier,
    dataState: DataState<T>?,
    loadingText: String = stringResource(R.string.basic_data_state_ui_loading),
    failureText: String = stringResource(R.string.basic_data_state_ui_failure),
    retryButtonText: String = stringResource(R.string.basic_data_state_ui_retry),
    enablePullToRefresh: Boolean = true,
    noinline onClickRetry: () -> Unit,
    crossinline successUi: @Composable (BoxScope.(data: T) -> Unit)
) {
    val pullToRefreshState = rememberPullToRefreshState()

    if (enablePullToRefresh) {
        PullToRefreshBox(
            modifier = modifier,
            isRefreshing = dataState is DataState.Loading,
            onRefresh = onClickRetry,
            state = pullToRefreshState,
            indicator = {
                Indicator(
                    modifier = Modifier.align(Alignment.TopCenter),
                    isRefreshing = dataState is DataState.Loading,
                    color = MaterialTheme.colorScheme.primary,
                    state = pullToRefreshState
                )
            },
            contentAlignment = Alignment.Center
        ) {
            Content(
                dataState = dataState,
                loadingText = loadingText,
                failureText = failureText,
                retryButtonText = retryButtonText,
                onClickRetry = onClickRetry,
                successUi = successUi
            )
        }
    } else {
        Box(
            modifier = modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Content(
                dataState = dataState,
                loadingText = loadingText,
                failureText = failureText,
                retryButtonText = retryButtonText,
                enablePullToRefresh = false,
                onClickRetry = onClickRetry,
                successUi = successUi
            )
        }
    }
}

@Composable
inline fun <T> Content(
    dataState: DataState<T>?,
    loadingText: String,
    failureText: String,
    retryButtonText: String,
    enablePullToRefresh: Boolean = true,
    noinline onClickRetry: () -> Unit,
    crossinline successUi: @Composable (BoxScope.(data: T) -> Unit)
) {
    when (dataState) {
        is DataState.Failure -> {
            Column(
                modifier = Modifier
            ) {
                EmptyListHint(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacings.ScreenHorizontalSpacing)
                        .align(Alignment.CenterHorizontally),
                    hint = failureText,
                    imageVector = Icons.Outlined.SignalWifiOff
                )

                TextButton(
                    onClick = onClickRetry,
                    content = { Text(text = retryButtonText) },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }

        is DataState.Loading -> {
            Column(modifier = Modifier) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = loadingText,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge
                )

                if (!enablePullToRefresh) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                            .align(Alignment.CenterHorizontally)
                            .padding(top = 4.dp)
                    )
                }
            }
        }

        is DataState.Success -> {
            Box {
                successUi(dataState.data)
            }
        }

        null -> {}
    }
}

/**
 * Simply displays nothing when the data state is not loaded.
 */
@Composable
fun <T> EmptyDataStateUi(
    dataState: DataState<T>,
    otherwise: @Composable () -> Unit = {},
    content: @Composable (T) -> Unit
) {
    when (dataState) {
        is DataState.Success -> content(dataState.data)
        else -> otherwise()
    }
}

@Composable
fun <T>AnimatedDataStateUi(
    modifier: Modifier = Modifier,
    dataState: DataState<T>,
    loadingContent: @Composable () -> Unit = @Composable {
        CircularProgressIndicator()
    },
    failureContent: @Composable () -> Unit = @Composable {
        Text(text = "Failed to load data")
    },
    successContent: @Composable (T) -> Unit
) {
    AnimatedContent(
        targetState = dataState,
        modifier = modifier,
        label = "AnimatedDataStateUi"
    ) { state ->
        when (state) {
            is DataState.Loading -> loadingContent()
            is DataState.Failure -> failureContent()
            is DataState.Success -> successContent(state.data)
        }
    }

}