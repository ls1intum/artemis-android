package de.tum.informatics.www1.artemis.native_app.feature.login.custom_instance_selection

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DoneOutline
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.service.ServerDataService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.ui.markdown.MarkdownText
import de.tum.informatics.www1.artemis.native_app.feature.account.R
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
    serverDataService: ServerDataService,
    private val serverConfigurationService: ServerConfigurationService
) : ViewModel() {
    private val _serverUrl = MutableStateFlow("")
    val serverUrl: StateFlow<String> = _serverUrl

    val isServerReachable: StateFlow<CanReachState> = _serverUrl.transformLatest { serverUrl ->
        emit(CanReachState.UNKNOWN)

        if (serverUrl.isBlank()) return@transformLatest

        delay(1.seconds)

        val response = serverDataService.getServerProfileInfo(serverUrl)

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
            val actualUrl = if (!serverUrl.value.endsWith("/")) {
                serverUrl.value + "/"
            } else {
                serverUrl.value
            }

            serverConfigurationService.updateServerUrl(actualUrl)
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
        modifier = modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        MarkdownText(
            modifier = Modifier.fillMaxWidth(),
            markdown = stringResource(id = R.string.account_select_custom_instance_selection_instruction)
        )

        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = enteredUrl,
            onValueChange = viewModel::updateServerUrl,
            textStyle = MaterialTheme.typography.bodyLarge,
            label = { Text(text = stringResource(id = R.string.account_select_custom_instance_selection_text_field_label)) }
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = when (reachStatus) {
                    CanReachState.UNKNOWN -> Icons.Default.QuestionMark
                    CanReachState.CONNECTED -> Icons.Default.DoneOutline
                    CanReachState.FAILED -> Icons.Default.ErrorOutline
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
    }
}

enum class CanReachState {
    UNKNOWN,
    CONNECTED,
    FAILED
}