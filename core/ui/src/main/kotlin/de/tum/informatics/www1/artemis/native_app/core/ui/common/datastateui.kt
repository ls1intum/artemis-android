package de.tum.informatics.www1.artemis.native_app.core.ui.common

import androidx.compose.foundation.layout.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
fun <T> BasicDataStateUi(
    modifier: Modifier,
    dataState: DataState<T>,
    loadingText: String,
    failureText: String,
    suspendedText: String,
    retryButtonText: String,
    onClickRetry: () -> Unit,
    successUi: @Composable BoxScope.(data: T) -> Unit
) {
    Box(modifier = modifier) {
        when (dataState) {
            is DataState.Failure, is DataState.Suspended -> {
                Column(
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = when (dataState) {
                            is DataState.Failure -> failureText
                            is DataState.Suspended -> suspendedText
                            else -> "" //Not reachable
                        },
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

                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                            .align(Alignment.CenterHorizontally)
                            .padding(top = 4.dp)
                    )
                }
            }
            is DataState.Success -> {
                successUi(dataState.data)
            }
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