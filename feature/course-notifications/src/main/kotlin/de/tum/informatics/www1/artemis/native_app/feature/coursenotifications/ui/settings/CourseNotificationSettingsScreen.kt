package de.tum.informatics.www1.artemis.native_app.feature.coursenotifications.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.ui.Scaling
import de.tum.informatics.www1.artemis.native_app.core.ui.common.BasicDataStateUi
import de.tum.informatics.www1.artemis.native_app.core.ui.common.InfoMessageCard
import de.tum.informatics.www1.artemis.native_app.feature.coursenotifications.R
import de.tum.informatics.www1.artemis.native_app.feature.coursenotifications.course_notification_model.CourseNotificationType
import de.tum.informatics.www1.artemis.native_app.feature.coursenotifications.course_notification_model.settingsTitle
import de.tum.informatics.www1.artemis.native_app.feature.coursenotifications.course_notification_model.NotificationChannel
import de.tum.informatics.www1.artemis.native_app.feature.coursenotifications.course_notification_model.NotificationSettingsPresetIdentifier

@Composable
internal fun CourseNotificationSettingsScreen(
    viewModel: CourseNotificationSettingsViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.combinedState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.notification_settings)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        BasicDataStateUi(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            dataState = state,
            loadingText = stringResource(R.string.loading_notification_settings),
            failureText = stringResource(R.string.failed_to_load_notification_settings),
            retryButtonText = stringResource(R.string.try_again),
            onClickRetry = viewModel::onRequestReload,
        ) { (info, settings) ->

            val current = viewModel.currentSettings.collectAsState()

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    PresetDropdown(
                        currentPreset = settings.selectedPreset,
                        onPresetSelected = { viewModel.selectPreset(it) }
                    )
                }

                item {
                    InfoMessageCard(infoText = stringResource(R.string.setting_disclaimer))
                }

                items(current.value) { (type, setting) ->
                    NotificationSettingToggle(
                        type = type,
                        enabled = setting[NotificationChannel.PUSH] ?: false,
                        onToggle = { viewModel.updateNotificationSetting(type, it) }
                    )
                }
            }
        }
    }
}


@Composable
private fun NotificationSettingToggle(
    type: CourseNotificationType,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    var localEnabled by remember { mutableStateOf(enabled) }

    LaunchedEffect(enabled) { localEnabled = enabled }

    Card(shape = MaterialTheme.shapes.medium) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = type.settingsTitle(),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            Switch(
                modifier = Modifier.scale(Scaling.SWITCH),
                checked = localEnabled,
                onCheckedChange = { new ->
                    localEnabled = new
                    onToggle(new)
                }
            )
        }
    }
}


@Composable
private fun PresetDropdown(
    currentPreset: Int,
    onPresetSelected: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val presets =
        NotificationSettingsPresetIdentifier.entries.filter { it != NotificationSettingsPresetIdentifier.UNKNOWN }
    val selectedPreset = presets.find { it.presetId == currentPreset }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Card(
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            ExposedDropdownMenuBox(
                modifier = Modifier.fillMaxWidth(),
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                TextField(
                    value = selectedPreset?.let { stringResource(it.titleResId) } ?: "",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                        .background(Color.Transparent),
                    colors = ExposedDropdownMenuDefaults.textFieldColors(
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent
                    )
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    presets.forEach { preset ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(text = stringResource(preset.titleResId))
                                    Text(
                                        text = stringResource(preset.descriptionResId),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            },
                            onClick = {
                                onPresetSelected(preset.presetId)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }

        selectedPreset?.descriptionResId?.let {
            Text(
                text = stringResource(id = it),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
