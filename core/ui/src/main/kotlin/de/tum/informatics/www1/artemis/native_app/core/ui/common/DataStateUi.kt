package de.tum.informatics.www1.artemis.native_app.core.ui.common

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.tum.informatics.www1.artemis.native_app.core.data.DataState

/**
 * Displays ui for each data state.
 * For loading, a progress bar with text on top of it
 * For suspended and failure, text with a try again button.
 */
@Composable
inline fun <T> BasicDataStateUi(
    modifier: Modifier,
    dataState: DataState<T>?,
    loadingText: String,
    failureText: String,
    retryButtonText: String,
    noinline onClickRetry: () -> Unit,
    crossinline successUi: @Composable (BoxScope.(data: T) -> Unit)
) {
    val pullToRefreshState = rememberPullToRefreshState()
    PullToRefreshBox(
        modifier = modifier,
        isRefreshing = dataState is DataState.Loading,
        onRefresh = {
            onClickRetry()
        },
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
        when (dataState) {
            is DataState.Failure -> {
                Column(
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = failureText,
                        textAlign = TextAlign.Center,
                        fontSize = 18.sp
                    )

                    TextButton(
                        onClick = onClickRetry,
                        content = { Text(text = retryButtonText) },
                        modifier = Modifier
                            .padding(top = 0.dp)
                            .align(Alignment.CenterHorizontally)
                    )
                }
            }

            is DataState.Loading -> {
                Column(modifier = Modifier.align(Alignment.Center)) {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = loadingText,
                        textAlign = TextAlign.Center,
                        fontSize = 20.sp,
                    )
                }
            }

            is DataState.Success -> {
                successUi(dataState.data)
            }

            null -> {}
        }
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