package de.tum.informatics.www1.artemis.native_app.feature.dashboard.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.feature.dashboard.R

private const val SURVEY_URL = "https://example.com/survey"

@Composable
fun SurveyDialog(
    modifier: Modifier = Modifier,
    dismiss: (dismissPermanently: Boolean) -> Unit
) {
    val uriHandler = LocalUriHandler.current
    var isDismissPersistentlyChecked by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { dismiss(false) },
        title = { Text(text = "Help needed!") },
        text = {
            Column(
                modifier = Modifier,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = "Do you have 5-10 minutes to take a quick survey and help us improve the app?")

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
            Button(
                onClick = {
                    uriHandler.openUri(SURVEY_URL)

                    dismiss(true)
                }
            ) {
                Text(text = "Participate")
            }
        },
        dismissButton = {
            TextButton(
                onClick = { dismiss(isDismissPersistentlyChecked) }
            ) {
                Text(text = "Not now")
            }
        }
    )
}



@Preview
@Composable
fun SurveyDialogPreview() {
    SurveyDialog(dismiss = {})
}