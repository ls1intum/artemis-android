package de.tum.informatics.www1.artemis.native_app.core.ui.markdown

import android.content.Context
import coil.ImageLoader
import coil.request.Disposable
import coil.request.ImageRequest
import coil.size.Scale
import de.tum.informatics.www1.artemis.native_app.core.common.R
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

private const val DEFAULT_IMAGE_HEIGHT = 800
private const val LINK_TYPE_HINT_ICON_HEIGHT = 52

object MarkdownRenderFactory {

    fun create(
        context: Context,
        imageLoader: ImageLoader?,
        linkResolver: LinkResolver?,
        useOriginalImageSize: Boolean = false,
    ): Markwon {
        val displayWidth = context.resources.displayMetrics.widthPixels

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
                                .apply {
                                    if (!useOriginalImageSize) {
                                        size(displayWidth, height) // We set a fixed height and set the width of the image to the screen width.
                                        scale(Scale.FIT)
                                    }
                                }
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
}