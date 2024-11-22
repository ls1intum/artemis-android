package de.tum.informatics.www1.artemis.native_app.feature.dashboard.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.feature.dashboard.R

@Composable
fun BetaHintDialog(
    dismiss: (dismissPermanently: Boolean) -> Unit
) {
    var isDismissPersistentlyChecked by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { dismiss(false) },
        title = { Text(text = stringResource(id = R.string.dashboard_dialog_beta_title)) },
        text = {
            Column(
                modifier = Modifier,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = stringResource(id = R.string.dashboard_dialog_beta_message))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            role = Role.Checkbox,
                            onClick = { isDismissPersistentlyChecked = !isDismissPersistentlyChecked }
                        ),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        modifier = Modifier,
                        checked = isDismissPersistentlyChecked,
                        onCheckedChange = { isDismissPersistentlyChecked = it }
                    )

                    Text(text = stringResource(id = R.string.dashboard_dialog_beta_do_not_show_again))
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { dismiss(isDismissPersistentlyChecked) }
            ) {
                Text(text = stringResource(id = R.string.dashboard_dialog_beta_positive))
            }
        }
    )
}