package de.tum.informatics.www1.artemis.native_app.feature.login.custom_instance_selection

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerProfileInfoService
import de.tum.informatics.www1.artemis.native_app.core.ui.Spacings
import de.tum.informatics.www1.artemis.native_app.core.ui.markdown.MarkdownText
import de.tum.informatics.www1.artemis.native_app.feature.login.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import kotlin.time.Duration.Companion.seconds

internal class CustomInstanceSelectionViewModel(
    serverProfileInfoService: ServerProfileInfoService,
    private val serverConfigurationService: ServerConfigurationService
) : ViewModel() {
    private val _serverUrl = MutableStateFlow("")
    val serverUrl: StateFlow<String> = _serverUrl

    val isServerReachable: StateFlow<CanReachState> = _serverUrl.transformLatest { serverUrl ->
        emit(CanReachState.UNKNOWN)

        if (serverUrl.isBlank()) return@transformLatest

        delay(1.seconds)

        val response = serverProfileInfoService.getServerProfileInfo(serverUrl)

        emit(
            when (response) {
                is NetworkResponse.Failure -> CanReachState.FAILED
                is NetworkResponse.Response -> CanReachState.CONNECTED
            }
        )
    }
        .stateIn(viewModelScope, SharingStarted.Eagerly, CanReachState.UNKNOWN)

    fun updateServerUrl(newServerUrl: String) {
        _serverUrl.value = newServerUrl
    }

    fun setCustomInstance(onDone: () -> Unit) {
        viewModelScope.launch {
            serverConfigurationService.updateServerUrl(serverUrl.value)
            onDone()
        }
    }
}

@Composable
internal fun CustomInstanceSelectionScreen(
    modifier: Modifier,
    onSuccessfullySetCustomInstance: () -> Unit
) {
    val viewModel: CustomInstanceSelectionViewModel = koinViewModel()

    val enteredUrl: String by viewModel.serverUrl.collectAsState()

    val reachStatus: CanReachState = viewModel.isServerReachable.collectAsState().value

    Column(
        modifier = modifier
            .padding(horizontal = Spacings.ScreenHorizontalSpacing)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f))

        MarkdownText(
            modifier = Modifier.fillMaxWidth(),
            markdown = stringResource(id = R.string.account_select_custom_instance_selection_instruction),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.labelLarge
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = enteredUrl,
                onValueChange = viewModel::updateServerUrl,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                label = { Text(text = stringResource(id = R.string.account_select_custom_instance_selection_text_field_label)) }
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = when (reachStatus) {
                        CanReachState.UNKNOWN -> Icons.Default.QuestionMark
                        CanReachState.CONNECTED -> Icons.Default.Done
                        CanReachState.FAILED -> Icons.Default.ErrorOutline
                    },
                    tint = when (reachStatus) {
                        CanReachState.UNKNOWN -> MaterialTheme.colorScheme.onSurface
                        CanReachState.CONNECTED -> MaterialTheme.colorScheme.primary
                        CanReachState.FAILED -> MaterialTheme.colorScheme.error
                    },
                    contentDescription = null
                )

                Text(
                    text = stringResource(
                        id = when (reachStatus) {
                            CanReachState.UNKNOWN -> R.string.account_select_custom_instance_selection_reach_unknown
                            CanReachState.CONNECTED -> R.string.account_select_custom_instance_selection_reach_success
                            CanReachState.FAILED -> R.string.account_select_custom_instance_selection_reach_failure
                        }
                    )
                )
            }
        }

        Button(
            modifier = Modifier.align(Alignment.End),
            enabled = reachStatus == CanReachState.CONNECTED,
            onClick = {
                viewModel.setCustomInstance {
                    onSuccessfullySetCustomInstance()
                }
            }
        ) {
            Text(
                text = stringResource(id = R.string.account_select_custom_instance_selection_set_instance_button)
            )
        }

        Spacer(modifier = Modifier.weight(2f))
    }
}

enum class CanReachState {
    UNKNOWN,
    CONNECTED,
    FAILED
}
