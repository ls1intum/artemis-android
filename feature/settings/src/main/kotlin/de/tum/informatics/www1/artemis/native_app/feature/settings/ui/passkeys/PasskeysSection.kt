package de.tum.informatics.www1.artemis.native_app.feature.settings.ui.passkeys

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Key
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.ui.common.ArtemisSection
import de.tum.informatics.www1.artemis.native_app.core.ui.common.EmptyDataStateUi
import de.tum.informatics.www1.artemis.native_app.core.ui.date.DateFormats
import de.tum.informatics.www1.artemis.native_app.core.ui.date.format
import de.tum.informatics.www1.artemis.native_app.feature.settings.R
import de.tum.informatics.www1.artemis.native_app.feature.settings.service.dto.PasskeyDTO
import de.tum.informatics.www1.artemis.native_app.feature.settings.ui.ButtonEntry

@Composable
fun PasskeysSection(
    modifier: Modifier = Modifier,
    passkeysDataState: DataState<List<PasskeyDTO>>,
    onCreatePasskey: () -> Unit,
    onDeletePasskey: (PasskeyDTO) -> Unit,
) {
    ArtemisSection(
        modifier = modifier,
        title = stringResource(R.string.passkey_settings_section_title),
        description = stringResource(R.string.passkey_settings_section_desc)
    ) {
        EmptyDataStateUi(
            dataState = passkeysDataState,
        ) { passkeys ->
            for (passkey in passkeys) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = passkey.label,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = stringResource(
                                R.string.passkey_settings_created_at_label,
                                passkey.created.format(DateFormats.OnlyDate.format)
                            ),
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                    
                    IconButton(
                        onClick = { onDeletePasskey(passkey) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(R.string.passkey_settings_delete_key),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            // TODO: enable once passkey creation error is resolved
            ButtonEntry(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.passkey_settings_add_key),
                leadingIcon = Icons.Default.Key,
                isFocused = true,
                textColor = MaterialTheme.colorScheme.primary,
                onClick = onCreatePasskey,
            )
        }
    }
}