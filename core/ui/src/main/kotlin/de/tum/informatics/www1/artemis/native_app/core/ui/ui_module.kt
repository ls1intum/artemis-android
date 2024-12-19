package de.tum.informatics.www1.artemis.native_app.core.ui

import de.tum.informatics.www1.artemis.native_app.core.ui.markdown.link_resolving.MarkdownLinkResolver
import de.tum.informatics.www1.artemis.native_app.core.ui.markdown.link_resolving.MarkdownLinkResolverImpl
import de.tum.informatics.www1.artemis.native_app.core.ui.remote_images.ArtemisImageProvider
import de.tum.informatics.www1.artemis.native_app.core.ui.remote_images.impl.ArtemisImageProviderImpl
import org.koin.dsl.module

val uiModule = module {
    single<ArtemisImageProvider> { ArtemisImageProviderImpl(get(), get()) }
    single<MarkdownLinkResolver> { MarkdownLinkResolverImpl(get(), get()) }
}