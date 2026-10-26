package de.tum.informatics.www1.artemis.native_app.core.ui.compose

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.ui.markdown.MarkdownText

@Composable
fun MediaDescription(description: String) {
    Surface(color = MaterialTheme.colorScheme.surface) {
        MarkdownText(
            description,
            Modifier.fillMaxWidth()
                .heightIn(max = 96.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
        )
    }
}
