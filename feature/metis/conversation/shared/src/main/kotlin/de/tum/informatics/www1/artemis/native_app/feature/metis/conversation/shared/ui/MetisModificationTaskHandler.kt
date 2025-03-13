package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.shared.ui

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import de.tum.informatics.www1.artemis.native_app.core.ui.AwaitDeferredCompletion
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.shared.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.shared.service.MetisModificationFailure
import kotlinx.coroutines.Deferred

internal const val TEST_TAG_METIS_MODIFICATION_FAILURE_DIALOG = "TEST_TAG_METIS_MODIFICATION_FAILURE_DIALOG"


@Composable
fun MetisModificationTaskHandler(
    metisModificationTask: Deferred<MetisModificationFailure?>?,
    onTaskCompletion: (MetisModificationFailure?) -> Unit = {},
    onSuccess: () -> Unit = {}
) {
    if (metisModificationTask == null) { return }

    var metisFailure: MetisModificationFailure? by remember {
        mutableStateOf(null)
    }

    AwaitDeferredCompletion(job = metisModificationTask) {
        metisFailure = it
        onTaskCompletion(it)
        if (it == null) {
            onSuccess()
        }
    }

    metisFailure?.let {
        MetisModificationFailureDialog(
            metisModificationFailure = it,
            onDismissRequest = { metisFailure = null }
        )
    }
}


@Composable
internal fun MetisModificationFailureDialog(
    metisModificationFailure: MetisModificationFailure,
    onDismissRequest: () -> Unit
) {
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
