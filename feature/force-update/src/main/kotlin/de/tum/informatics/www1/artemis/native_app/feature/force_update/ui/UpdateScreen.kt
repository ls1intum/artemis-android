package de.tum.informatics.www1.artemis.native_app.feature.force_update.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import de.tum.informatics.www1.artemis.native_app.core.ui.Spacings
import de.tum.informatics.www1.artemis.native_app.feature.forceupdate.R

@Composable
fun UpdateScreen(
    onDownloadClick: () -> Unit,
    currentVersion: String,
    minVersion: String
) {
    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(Spacings.UpdateScreen.large),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacings.UpdateScreen.large)
        ) {
            Image(
                painter = painterResource(R.drawable.ic_launcher_foreground),
                contentDescription = "Update Icon",
                modifier = Modifier
                    .size(Spacings.UpdateScreen.imageSize)
                    .align(Alignment.CenterHorizontally),
            )

            Text(
                text = stringResource(id = R.string.update_screen_new_update_available),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(Spacings.UpdateScreen.medium))

            Text(
                text = stringResource(R.string.update_screen_download_message),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(Spacings.UpdateScreen.medium))

            Text(
                text = stringResource(
                    R.string.update_screen_version_info,
                    currentVersion,
                    minVersion
                ),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.secondary
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onDownloadClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(R.string.update_screen_downlaod_button))
            }
        }
    }
}