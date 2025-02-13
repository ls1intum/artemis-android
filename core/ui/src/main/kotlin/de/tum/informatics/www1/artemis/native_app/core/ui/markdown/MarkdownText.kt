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
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import coil.request.Disposable
import coil.request.ImageRequest
import coil.size.Scale
import de.tum.informatics.www1.artemis.native_app.core.common.R
import de.tum.informatics.www1.artemis.native_app.core.common.markdown.ArtemisMarkdownTransformer
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.LinkResolver
import io.noties.markwon.Markwon
import io.noties.markwon.MarkwonConfiguration
import io.noties.markwon.MarkwonVisitor
import io.noties.markwon.core.MarkwonTheme
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.ext.tables.TablePlugin
import io.noties.markwon.html.HtmlPlugin
import io.noties.markwon.image.AsyncDrawable
import io.noties.markwon.image.coil.CoilImagesPlugin
import io.noties.markwon.linkify.LinkifyPlugin
import org.commonmark.node.SoftLineBreak

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

val LocalMarkdownTransformer =
    compositionLocalOf<ArtemisMarkdownTransformer> { ArtemisMarkdownTransformer.Default }

private const val DEFAULT_IMAGE_HEIGHT = 800
private const val LINK_TYPE_HINT_ICON_HEIGHT = 52

@Composable
fun MarkdownText(
    markdown: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    textAlign: TextAlign = TextAlign.Left,
    maxLines: Int = Int.MAX_VALUE,
    @FontRes fontResource: Int? = null,
    style: TextStyle = LocalTextStyle.current,
    @IdRes viewId: Int? = null,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    imageLoader: ImageLoader? = null,
    linkResolver: LinkResolver? = null
) {
    val defaultColor: Color = LocalContentColor.current
    val context: Context = LocalContext.current
    val localMarkwon = LocalMarkwon.current

    val imageWidth = context.resources.displayMetrics.widthPixels
    val markdownRender: Markwon = localMarkwon ?: remember(imageLoader, linkResolver) {
        createMarkdownRender(context, imageLoader, linkResolver, imageWidth)
    }

    val markdownTransformer = LocalMarkdownTransformer.current

    val transformedMarkdown by remember(markdown, markdownTransformer) {
        derivedStateOf {
            markdownTransformer.transformMarkdown(markdown)
        }
    }

    val previousTransformedMarkdown = remember { mutableStateOf<String?>(null) }

    AndroidView(
        // Added semantics for ui testing.
        modifier = modifier
            .semantics {
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
            // Only update if the transformed markdown has changed
            if (transformedMarkdown != previousTransformedMarkdown.value) {
                markdownRender.setMarkdown(textView, transformedMarkdown)
                previousTransformedMarkdown.value = transformedMarkdown
            }

            textView.movementMethod = LinkMovementMethod.getInstance()

            onClick?.let { textView.setOnClickListener { onClick() } }
            onLongClick?.let {
                textView.setOnLongClickListener {
                    onLongClick()
                    true
                }
            }

            textView.applyStyleAndColor(fontSize, textAlign, color, defaultColor, style)
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
    textAlign: TextAlign = TextAlign.Left,
    maxLines: Int = Int.MAX_VALUE,
    @FontRes fontResource: Int? = null,
    style: TextStyle,
    @IdRes viewId: Int? = null,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null
): TextView {
    return TextView(context).apply {
        onClick?.let { setOnClickListener { onClick() } }
        onLongClick?.let {
            setOnLongClickListener {
                onLongClick()
                true
            }
        }
        setMaxLines(maxLines)
        highlightColor = android.graphics.Color.TRANSPARENT

        viewId?.let { id = viewId }

        fontResource?.let { font ->
            typeface = ResourcesCompat.getFont(context, font)
        }

        applyStyleAndColor(fontSize, textAlign, color, defaultColor, style)
    }
}

private fun TextView.applyStyleAndColor(
    fontSize: TextUnit,
    textAlign: TextAlign,
    color: Color,
    defaultColor: Color,
    style: TextStyle
) {
    val textColor = color.takeOrElse { style.color.takeOrElse { defaultColor } }

    val mergedStyle = style.merge(
        TextStyle(
            color = textColor,
            fontSize = if (fontSize != TextUnit.Unspecified) fontSize else style.fontSize,
            textAlign = textAlign,
        )
    )

    setTextColor(textColor.toArgb())
    setTextSize(TypedValue.COMPLEX_UNIT_SP, mergedStyle.fontSize.value)

    textAlignment = when (textAlign) {
        TextAlign.Left, TextAlign.Start -> View.TEXT_ALIGNMENT_TEXT_START
        TextAlign.Right, TextAlign.End -> View.TEXT_ALIGNMENT_TEXT_END
        TextAlign.Center -> View.TEXT_ALIGNMENT_CENTER
        else -> View.TEXT_ALIGNMENT_TEXT_START
    }
}

fun createMarkdownRender(context: Context, imageLoader: ImageLoader?, linkResolver: LinkResolver?, imageWidth: Int): Markwon {
    val imagePlugin: CoilImagesPlugin? =
        if (imageLoader != null) {
            CoilImagesPlugin.create(
                object : CoilImagesPlugin.CoilStore {
                    override fun load(drawable: AsyncDrawable): ImageRequest {
                        var height = DEFAULT_IMAGE_HEIGHT
                        if (drawable.destination.contains(TYPE_ICON_RESOURCE_PATH)) {
                            height = LINK_TYPE_HINT_ICON_HEIGHT
                        }

                        return ImageRequest.Builder(context)
                            .defaults(imageLoader.defaults)
                            .data(drawable.destination)
                            .crossfade(true)
                            .size(imageWidth, height) // We set a fixed height and set the width of the image to the screen width.
                            .scale(Scale.FIT)
                            .build()
                    }

                    override fun cancel(disposable: Disposable) {
                        disposable.dispose()
                    }
                },
                imageLoader
            )
        } else null

    return Markwon.builder(context)
        .usePlugin(HtmlPlugin.create())
        .usePlugin(StrikethroughPlugin.create())
        .usePlugin(TablePlugin.create(context))
        .usePlugin(LinkifyPlugin.create())
        .usePlugin(object : AbstractMarkwonPlugin() {
            override fun configureTheme(builder: MarkwonTheme.Builder) {
                builder
                    .linkColor(context.getColor(R.color.link_color))
                    .isLinkUnderlined(false)
            }
        })
        .usePlugin(object : AbstractMarkwonPlugin() {
            override fun configureVisitor(builder: MarkwonVisitor.Builder) {
                builder.on(SoftLineBreak::class.java) { visitor, _ ->
                    visitor.forceNewLine()
                }
            }

        })
        .apply {
            if (imagePlugin != null) {
                usePlugin(imagePlugin)
            }
            if (linkResolver != null) {
                usePlugin(object : AbstractMarkwonPlugin() {
                    override fun configureConfiguration(builder: MarkwonConfiguration.Builder) {
                        builder.linkResolver(linkResolver)
                    }
                })
            }
        }
        .build()
}