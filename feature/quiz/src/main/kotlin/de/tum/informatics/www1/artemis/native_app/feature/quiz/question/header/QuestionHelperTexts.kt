package de.tum.informatics.www1.artemis.native_app.feature.quiz.question

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Help
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.ui.markdown.MarkdownText

@Composable
internal fun HelpText(
    modifier: Modifier,
    help: String,
    style: TextStyle = LocalTextStyle.current
) {
    TextImpl(
        modifier, help, style, Icons.Filled.Help
    )
}

@Composable
internal fun ExplanationText(
    modifier: Modifier,
    explanation: String,
    style: TextStyle = LocalTextStyle.current
) {
    TextImpl(
        modifier, explanation, style, Icons.Filled.Error
    )
}

@Composable
private fun TextImpl(
    modifier: Modifier,
    text: String,
    style: TextStyle = LocalTextStyle.current,
    icon: ImageVector
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            modifier = Modifier.align(Alignment.Top),
            imageVector = icon,
            contentDescription = null
        )

        MarkdownText(markdown = text, style = style)
    }
}