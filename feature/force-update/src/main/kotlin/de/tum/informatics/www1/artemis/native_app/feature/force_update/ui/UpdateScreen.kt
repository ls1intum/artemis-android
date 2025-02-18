package de.tum.informatics.www1.artemis.native_app.feature.force_update.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import de.tum.informatics.www1.artemis.native_app.feature.force_update.UpdateScreenConstants
import de.tum.informatics.www1.artemis.native_app.feature.forceupdate.R

@Composable
fun UpdateScreen(
    onDownloadClick: () -> Unit,
    currentVersion: String,
    minVersion: String
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background)
            .imePadding()
            .padding(bottom = UpdateScreenConstants.SpacingMedium),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(UpdateScreenConstants.SpacingLarge),
            modifier = Modifier
                .fillMaxWidth()
                .padding(UpdateScreenConstants.ScreenPadding)
        ) {
            Image(
                painter = painterResource(R.drawable.ic_launcher_foreground),
                contentDescription = "Update Icon",
                modifier = Modifier
                    .size(UpdateScreenConstants.ImageSize)
                    .align(Alignment.CenterHorizontally),
            )

            Text(
                text = stringResource(id = R.string.update_screen_new_update_available),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(UpdateScreenConstants.SpacingMedium))

            Text(
                text = stringResource(R.string.update_screen_download_message),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(UpdateScreenConstants.SpacingMedium))

            Text(
                text = stringResource(
                    R.string.update_screen_version_info,
                    currentVersion,
                    minVersion
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.weight(0.1f))

            Button(
                onClick = onDownloadClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(UpdateScreenConstants.ButtonCornerRadius)
            ) {
                Text(
                    text = stringResource(R.string.update_screen_downlaod_button)
                )
            }
        }
    }
}
