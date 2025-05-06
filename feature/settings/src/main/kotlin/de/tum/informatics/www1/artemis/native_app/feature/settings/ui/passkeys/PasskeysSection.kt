package de.tum.informatics.www1.artemis.native_app.feature.settings.ui.passkeys

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.service.passkey.dto.PasskeyDTO
import de.tum.informatics.www1.artemis.native_app.core.ui.common.ArtemisSection
import de.tum.informatics.www1.artemis.native_app.core.ui.common.EmptyDataStateUi

@Composable
fun PasskeysSection(
    modifier: Modifier = Modifier,
    passkeysDataState: DataState<List<PasskeyDTO>>,
    onCreatePasskey: () -> Unit,
) {
    ArtemisSection(
        modifier = modifier,
        title = "Passkeys",
        description = "Passkeys are a safer and easier alternative to passwords."
    ) {
        EmptyDataStateUi(
            dataState = passkeysDataState,
        ) { passkeys ->
            for (passkey in passkeys) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = passkey.label,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = passkey.created,
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }

            // TODO: enable once passkey creation error is resolved
//            ButtonEntry(
//                modifier = Modifier.fillMaxWidth(),
//                text = "Add new key",
//                leadingIcon = Icons.Default.Key,
//                isFocused = true,
//                textColor = MaterialTheme.colorScheme.primary,
//                onClick = onCreatePasskey,
//            )
        }
    }
}