package de.tum.informatics.www1.artemis.native_app.core.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private fun getSectionTestTag(title: String) = "ArtemisSection $title"

@Composable
fun ArtemisSection(
    modifier: Modifier,
    title: String,
    description: String? = null,
    testTag: String? = null,
    spacing: Dp = 8.dp,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = modifier
                .padding(16.dp)
                .testTag(testTag ?: getSectionTestTag(title)),
            verticalArrangement = Arrangement.spacedBy(spacing)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            description?.let {
                Text(
                    modifier = Modifier.padding(bottom = 8.dp),
                    text = it,
                    style = MaterialTheme.typography.labelMedium
                )
            }
            content()
        }
    }
}