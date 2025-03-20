package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.R


@Composable
internal fun FormattingOptions(
    containerColor: Color,
    applyMarkdownStyle: (MarkdownStyle) -> Unit
) {
    var isListDropDownExpanded by remember { mutableStateOf(false) }
    var isCodeDropdownExpanded by remember { mutableStateOf(false) }

    val applyMarkdownStyleFormattingButton = @Composable { markdownStyle: MarkdownStyle, drawableId: Int ->
        FormattingButton(
            modifier = Modifier.background(containerColor),
            applyMarkdown = {
                applyMarkdownStyle(markdownStyle)
            },
            drawableId = drawableId
        )
    }

    Row(
        modifier = Modifier
            .clip(MaterialTheme.shapes.medium)
            .fillMaxWidth()
            .height(32.dp)
            .horizontalScroll(rememberScrollState())
            .background(containerColor),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.width(8.dp))

        applyMarkdownStyleFormattingButton(MarkdownStyle.Bold, R.drawable.bold)
        applyMarkdownStyleFormattingButton(MarkdownStyle.Italic, R.drawable.italic)
        applyMarkdownStyleFormattingButton(MarkdownStyle.Underline, R.drawable.underline)
        applyMarkdownStyleFormattingButton(MarkdownStyle.Strikethrough, R.drawable.strikethrough)

        // Code Button
        Box(modifier = Modifier.align(Alignment.Top)) {
            FormattingButton(
                modifier = Modifier.background(containerColor),
                applyMarkdown = {
                    isCodeDropdownExpanded = !isCodeDropdownExpanded
                },
                drawableId = R.drawable.code
            )

            CodeFormattingDropdownMenu(
                isCodeDropdownExpanded = isCodeDropdownExpanded,
                onDismissRequest = { isCodeDropdownExpanded = false },
                applyCodeMarkdown = applyMarkdownStyle
            )
        }

        // Blockquote Button
        applyMarkdownStyleFormattingButton(MarkdownStyle.Blockquote, R.drawable.quote)

        Box(modifier = Modifier.align(Alignment.Top)) {
            FormattingButton(
                modifier = Modifier.background(containerColor),
                applyMarkdown = {
                    isListDropDownExpanded = !isListDropDownExpanded
                },
                drawableId = R.drawable.list
            )

            ListFormattingDropdownMenu(
                isListDropDownExpanded = isListDropDownExpanded,
                onDismissRequest = { isListDropDownExpanded = false },
                applyListMarkdown = applyMarkdownStyle
            )
        }

        Spacer(modifier = Modifier.width(8.dp))
    }
}


@Composable
private fun FormattingButton(
    modifier: Modifier,
    applyMarkdown: () -> Unit,
    drawableId: Int,
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .clickable {
                applyMarkdown()
            }
            .padding(8.dp)
    ){
        Icon(
            painter = painterResource(id = drawableId),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            contentDescription = null,
        )
    }
}

@Composable
private fun CodeFormattingDropdownMenu(
    isCodeDropdownExpanded: Boolean,
    onDismissRequest: () -> Unit,
    applyCodeMarkdown: (MarkdownStyle) -> Unit,
) {
    DropdownMenu(
        expanded = isCodeDropdownExpanded,
        onDismissRequest = onDismissRequest,
        properties = PopupProperties(
            focusable = false
        )
    ) {
        DropdownMenuItem(
            leadingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.code),
                    contentDescription = null
                )
            },
            text = { Text(text = stringResource(R.string.reply_format_inline_code)) },
            onClick = {
                onDismissRequest()
                applyCodeMarkdown(MarkdownStyle.InlineCode)
            }
        )

        DropdownMenuItem(
            leadingIcon = {
                Text(
                    modifier = Modifier.padding(start = 4.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    text = "{ }"
                )
            },
            text = { Text(stringResource(R.string.reply_format_code_block)) },
            onClick = {
                onDismissRequest()
                applyCodeMarkdown(MarkdownStyle.CodeBlock)
            }
        )
    }
}

@Composable
private fun ListFormattingDropdownMenu(
    isListDropDownExpanded: Boolean,
    onDismissRequest: () -> Unit,
    applyListMarkdown: (MarkdownStyle) -> Unit,
) {
    DropdownMenu(
        expanded = isListDropDownExpanded,
        onDismissRequest = onDismissRequest,
        properties = PopupProperties(
            focusable = false
        )
    ) {
        // Unordered List item
        DropdownMenuItem(
            leadingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.list),
                    contentDescription = null
                )
            },
            text = { Text(text = stringResource(R.string.reply_format_unordered)) },
            onClick = {
                onDismissRequest()
                applyListMarkdown(MarkdownStyle.UnorderedList)
            }
        )
        // Ordered List item
        DropdownMenuItem(
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.FormatListNumbered,
                    contentDescription = null
                )
            },
            text = { Text(stringResource(R.string.reply_format_ordered)) },
            onClick = {
                onDismissRequest()
                applyListMarkdown(MarkdownStyle.OrderedList)
            }
        )
    }
}