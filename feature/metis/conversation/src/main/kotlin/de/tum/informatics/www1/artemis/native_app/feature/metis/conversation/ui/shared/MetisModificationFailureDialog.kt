package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.shared

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.MetisModificationFailure

internal const val TEST_TAG_METIS_MODIFICATION_FAILURE_DIALOG = "TEST_TAG_METIS_MODIFICATION_FAILURE_DIALOG"

@Composable
internal fun MetisModificationFailureDialog(
    metisModificationFailure: MetisModificationFailure?,
    onDismissRequest: () -> Unit
) {
    if (metisModificationFailure == null) return

    AlertDialog(
        modifier = Modifier.testTag(TEST_TAG_METIS_MODIFICATION_FAILURE_DIALOG),
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
