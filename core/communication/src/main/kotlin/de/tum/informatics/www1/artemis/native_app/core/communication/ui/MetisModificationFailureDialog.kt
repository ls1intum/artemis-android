package de.tum.informatics.www1.artemis.native_app.core.communication.ui

import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import de.tum.informatics.www1.artemis.native_app.core.communication.MetisModificationFailure
import de.tum.informatics.www1.artemis.native_app.core.communication.R

@Composable
internal fun MetisModificationFailureDialog(
    metisModificationFailure: MetisModificationFailure?,
    onDismissRequest: () -> Unit
) {
    if (metisModificationFailure == null) return
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(text = stringResource(id = R.string.metis_modification_failure_dialog_title)) },
        text = {
            Text(text = stringResource(id = metisModificationFailure.messageRes))
        },
        confirmButton = {
            TextButton(onClick = onDismissRequest) {
                Text(
                    text = stringResource(id = R.string.metis_modification_failure_dialog_confirm_button)
                )
            }
        }
    )
}

