package de.tum.informatics.www1.artemis.native_app.core.ui.markdown

import android.content.Context
import android.text.method.LinkMovementMethod
import android.util.TypedValue
import android.view.View
import android.widget.TextView
import androidx.annotation.FontRes
import androidx.annotation.IdRes
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.onLongClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.res.ResourcesCompat
import coil.ImageLoader
import de.tum.informatics.www1.artemis.native_app.core.common.markdown.ArtemisMarkdownTransformer
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import io.noties.markwon.Markwon
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.ext.tables.TablePlugin
import io.noties.markwon.html.HtmlPlugin
import io.noties.markwon.image.coil.CoilImagesPlugin
import io.noties.markwon.linkify.LinkifyPlugin
import org.koin.androidx.compose.get
import org.koin.compose.koinInject

// Copy from: https://github.com/jeziellago/compose-markdown
/*
 *
 * MIT License
 *
 * Copyright (c) 2021 Jeziel Lago
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

@Composable
fun MarkdownText(
    markdown: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    @FontRes fontResource: Int? = null,
    style: TextStyle = LocalTextStyle.current,
    @IdRes viewId: Int? = null,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    imageLoader: ImageLoader? = null,
) {
    val defaultColor: Color = LocalContentColor.current
    val context: Context = LocalContext.current
    val localMarkwon = LocalMarkwon.current

    val markdownRender: Markwon = localMarkwon ?: remember(imageLoader) {
        createMarkdownRender(context, imageLoader)
    }

    val serverConfigurationService: ServerConfigurationService = koinInject()
    val serverUrl by serverConfigurationService.serverUrl.collectAsState(initial = "")

    val transformedMarkdown by remember(markdown, serverUrl) {
        derivedStateOf {
            val strippedServerUrl =
                if (serverUrl.endsWith("/")) serverUrl.substring(
                    0,
                    serverUrl.length - 1
                ) else serverUrl

            ArtemisMarkdownTransformer.transformMarkdown(markdown, strippedServerUrl)
        }
    }

    AndroidView(
        // Added semantics for ui testing.
        modifier = modifier.semantics {
            text = AnnotatedString(markdown)
            onClick?.let { this.onClick(action = { onClick(); true }) }
            onLongClick?.let { this.onLongClick(action = { onLongClick(); true }) }
        },
        factory = { ctx ->
            createTextView(
                context = ctx,
                color = color,
                defaultColor = defaultColor,
                fontSize = fontSize,
                fontResource = fontResource,
                maxLines = maxLines,
                style = style,
                textAlign = textAlign,
                viewId = viewId,
                onClick = onClick,
                onLongClick = onLongClick
            )
        },
        update = { textView ->
            markdownRender.setMarkdown(textView, transformedMarkdown)
            textView.movementMethod = LinkMovementMethod.getInstance()
        },
        onReset = {},
        onRelease = {}
    )
}

private fun createTextView(
    context: Context,
    color: Color = Color.Unspecified,
    defaultColor: Color,
    fontSize: TextUnit = TextUnit.Unspecified,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    @FontRes fontResource: Int? = null,
    style: TextStyle,
    @IdRes viewId: Int? = null,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null
): TextView {

    val textColor = color.takeOrElse { style.color.takeOrElse { defaultColor } }
    val mergedStyle = style.merge(
        TextStyle(
            color = textColor,
            fontSize = if (fontSize != TextUnit.Unspecified) fontSize else style.fontSize,
            textAlign = textAlign,
        )
    )
    return TextView(context).apply {
        onClick?.let { setOnClickListener { onClick() } }
        onLongClick?.let {
            setOnLongClickListener {
                onLongClick()
                true
            }
        }
        setTextColor(textColor.toArgb())
        setMaxLines(maxLines)
        setTextSize(TypedValue.COMPLEX_UNIT_DIP, mergedStyle.fontSize.value)
        highlightColor = android.graphics.Color.TRANSPARENT

        viewId?.let { id = viewId }
        textAlign?.let { align ->
            textAlignment = when (align) {
                TextAlign.Left, TextAlign.Start -> View.TEXT_ALIGNMENT_TEXT_START
                TextAlign.Right, TextAlign.End -> View.TEXT_ALIGNMENT_TEXT_END
                TextAlign.Center -> View.TEXT_ALIGNMENT_CENTER
                else -> View.TEXT_ALIGNMENT_TEXT_START
            }
        }

        fontResource?.let { font ->
            typeface = ResourcesCompat.getFont(context, font)
        }
    }
}

fun createMarkdownRender(context: Context, imageLoader: ImageLoader?): Markwon {
    return Markwon.builder(context)
        .usePlugin(HtmlPlugin.create())
        .usePlugin(StrikethroughPlugin.create())
        .usePlugin(TablePlugin.create(context))
        .usePlugin(LinkifyPlugin.create())
        .apply {
            if (imageLoader != null) {
                usePlugin(CoilImagesPlugin.create(context, imageLoader))
            }
        }
        .build()
}