package de.tum.informatics.www1.artemis.native_app.feature.force_update.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import de.tum.informatics.www1.artemis.native_app.core.common.app_version.NormalizedAppVersion
import de.tum.informatics.www1.artemis.native_app.core.ui.Spacings
import de.tum.informatics.www1.artemis.native_app.feature.forceupdate.R

@Composable
fun UpdateScreen(
    onDownloadClick: () -> Unit,
    onSkipClick: () -> Unit,
    currentVersion: NormalizedAppVersion,
    minVersion: NormalizedAppVersion,
    recommendedVersion: NormalizedAppVersion
) {
    val isRecommended = recommendedVersion > minVersion && currentVersion > minVersion

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(Spacings.UpdateScreen.large),
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .fillMaxSize()
                    .padding(bottom = Spacings.UpdateScreen.large),
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

                Text(
                    text = if (isRecommended) {
                        stringResource(R.string.update_screen_recommended_download_message)
                    } else {
                        stringResource(R.string.update_screen_download_message)
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(Spacings.UpdateScreen.medium))

                Text(
                    text = if (isRecommended) {
                        stringResource(
                            R.string.update_screen_recommended_version_info,
                            currentVersion,
                            recommendedVersion
                        )
                    } else {
                        stringResource(
                            R.string.update_screen_version_info,
                            currentVersion,
                            minVersion
                        )
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.secondary
                )

            }

            // Download button
            Button(
                onClick = {
                    onDownloadClick()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(R.string.update_screen_downlaod_button))
            }

            if (isRecommended) {
                Spacer(modifier = Modifier.height(Spacings.UpdateScreen.medium))
                TextButton(
                    onClick = {
                        onSkipClick()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Skip")
                }
            }
        }
    }
}
