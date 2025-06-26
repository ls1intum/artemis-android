package de.tum.informatics.www1.artemis.native_app.feature.faq.ui.detail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.toRoute
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.ui.AttachmentHandler
import de.tum.informatics.www1.artemis.native_app.core.ui.LocalLinkOpener
import de.tum.informatics.www1.artemis.native_app.core.ui.Spacings
import de.tum.informatics.www1.artemis.native_app.core.ui.alert.TextAlertDialog
import de.tum.informatics.www1.artemis.native_app.core.ui.common.BasicDataStateUi
import de.tum.informatics.www1.artemis.native_app.core.ui.common.top_app_bar.AdaptiveNavigationIcon
import de.tum.informatics.www1.artemis.native_app.core.ui.common.top_app_bar.ArtemisTopAppBar
import de.tum.informatics.www1.artemis.native_app.core.ui.deeplinks.FaqDeeplinks
import de.tum.informatics.www1.artemis.native_app.core.ui.markdown.LocalMarkdownTransformer
import de.tum.informatics.www1.artemis.native_app.core.ui.markdown.LocalMarkwon
import de.tum.informatics.www1.artemis.native_app.core.ui.markdown.MarkdownRenderFactory
import de.tum.informatics.www1.artemis.native_app.core.ui.markdown.MarkdownText
import de.tum.informatics.www1.artemis.native_app.core.ui.navigation.animatedComposable
import de.tum.informatics.www1.artemis.native_app.core.ui.remote_images.LocalArtemisImageProvider
import de.tum.informatics.www1.artemis.native_app.feature.faq.R
import de.tum.informatics.www1.artemis.native_app.feature.faq.repository.data.Faq
import de.tum.informatics.www1.artemis.native_app.feature.faq.ui.FaqLinkResolver
import de.tum.informatics.www1.artemis.native_app.feature.faq.ui.rememberFaqArtemisMarkdownTransformer
import de.tum.informatics.www1.artemis.native_app.feature.faq.ui.shared.FaqCategoryChipFlowRow
import io.noties.markwon.LinkResolver
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Serializable
data class FaqDetailScreenRoute(
    val faqId: Long,
)

fun NavController.navigateToFaqDetail(
    faqId: Long,
    builder: NavOptionsBuilder.() -> Unit
) {
    navigate(FaqDetailScreenRoute(faqId), builder)
}

fun NavGraphBuilder.faqDetail() {
    animatedComposable<FaqDetailScreenRoute>(
        deepLinks = FaqDeeplinks.ToFaq.generateLinks(),
    ) { backStackEntry ->
        val route: FaqDetailScreenRoute = backStackEntry.toRoute()
        val faqId = route.faqId

        FaqDetailContent(faqId)
    }
}

@Composable
fun FaqDetailContent(
    faqId: Long,
    onSidebarToggle: () -> Unit = {},
) {
    val viewModel: FaqDetailViewModel = koinViewModel(key = "faq|$faqId") {
        parametersOf(faqId)
    }
    val faq by viewModel.faq.collectAsState()

    val serverUrl by viewModel.serverUrl.collectAsState()
    val markdownTransformer = rememberFaqArtemisMarkdownTransformer(serverUrl)

    CompositionLocalProvider(LocalMarkdownTransformer provides markdownTransformer) {
        FaqDetailUi(
            modifier = Modifier.fillMaxSize(),
            faqDataState = faq,
            onReloadRequest = viewModel::onRequestReload,
            onSidebarToggle = onSidebarToggle,
            serverUrl = serverUrl
        )
    }
}

@Composable
fun FaqDetailUi(
    modifier: Modifier = Modifier,
    faqDataState: DataState<Faq>,
    onReloadRequest: () -> Unit,
    onSidebarToggle: () -> Unit,
    serverUrl: String
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            ArtemisTopAppBar(
                title = {
                    Text(stringResource(R.string.faq_details_title))
                },
                navigationIcon = {
                    AdaptiveNavigationIcon(onSidebarToggle = onSidebarToggle)
                }
            )
        }
    ) { paddingValues ->
        BasicDataStateUi(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding() + Spacings.ScreenTopBarSpacing)
                .consumeWindowInsets(WindowInsets.systemBars.only(WindowInsetsSides.Top))
                .padding(horizontal = Spacings.ScreenHorizontalSpacing),
            dataState = faqDataState,
            loadingText = stringResource(id = R.string.faq_loading_faq_loading),
            failureText = stringResource(id = R.string.faq_loading_faq_failed),
            retryButtonText = stringResource(id = R.string.faq_loading_faq_try_again),
            onClickRetry = onReloadRequest
        ) { faq ->

            val context = LocalContext.current
            val markdownTransformer = rememberFaqArtemisMarkdownTransformer(serverUrl)
            val imageLoader = LocalArtemisImageProvider.current.rememberArtemisImageLoader()
            val linkOpener = LocalLinkOpener.current

            var pendingOpenFileAttachmentByUrl: String? by remember { mutableStateOf(null) }
            var pendingOpenLink: String? by remember { mutableStateOf(null) }

            val linkResolver = remember(serverUrl) {
                FaqLinkResolver(
                    serverUrl = serverUrl,
                    onRequestOpenAttachment = { pendingOpenFileAttachmentByUrl = it },
                    onRequestOpenLink = { pendingOpenLink = it }
                )
            }

            val markwon = remember(linkResolver, imageLoader) {
                MarkdownRenderFactory.create(context, imageLoader, linkResolver)
            }

            CompositionLocalProvider(
                LocalMarkdownTransformer provides markdownTransformer,
                LocalMarkwon provides markwon
            ) {
                FaqDetail(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .navigationBarsPadding(),
                    faq = faq,
                    linkResolver = linkResolver
                )
            }

            AttachmentHandler(
                url = pendingOpenFileAttachmentByUrl,
                onDismiss = { pendingOpenFileAttachmentByUrl = null }
            )

            if (pendingOpenLink != null) {
                TextAlertDialog(
                    title = stringResource(id = R.string.faq_open_link_dialog_title),
                    text = stringResource(
                        id = R.string.faq_open_link_dialog_message,
                        pendingOpenLink.orEmpty()
                    ),
                    confirmButtonText = stringResource(id = R.string.faq_link_dialog_positive),
                    dismissButtonText = stringResource(id = R.string.faq_link_dialog_negative),
                    onPressPositiveButton = {
                        linkOpener.openLink(pendingOpenLink.orEmpty())
                        pendingOpenLink = null
                    },
                    onDismissRequest = { pendingOpenLink = null }
                )
            }
        }
    }
}

@Composable
private fun FaqDetail(
    modifier: Modifier = Modifier,
    faq: Faq,
    linkResolver: LinkResolver
) {
    Column(
        modifier = modifier,
    ) {
        Text(
            text = faq.questionTitle,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        FaqCategoryChipFlowRow(categories = faq.categories)

        Spacer(modifier = Modifier.height(16.dp))

        MarkdownText(
            markdown = faq.questionAnswer,
            style = MaterialTheme.typography.bodyLarge,
            linkResolver = linkResolver
        )

        Spacer(modifier = Modifier
            .height(Spacings.EndOfScrollablePageSpacing)
        )
    }
}