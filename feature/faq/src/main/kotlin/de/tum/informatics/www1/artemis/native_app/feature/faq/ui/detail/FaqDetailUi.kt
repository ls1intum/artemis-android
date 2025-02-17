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
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.toRoute
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.ui.Spacings
import de.tum.informatics.www1.artemis.native_app.core.ui.common.BasicDataStateUi
import de.tum.informatics.www1.artemis.native_app.core.ui.compose.NavigationBackButton
import de.tum.informatics.www1.artemis.native_app.core.ui.markdown.LocalMarkdownTransformer
import de.tum.informatics.www1.artemis.native_app.core.ui.markdown.MarkdownText
import de.tum.informatics.www1.artemis.native_app.core.ui.markdown.ProvideMarkwon
import de.tum.informatics.www1.artemis.native_app.core.ui.navigation.animatedComposable
import de.tum.informatics.www1.artemis.native_app.feature.faq.R
import de.tum.informatics.www1.artemis.native_app.feature.faq.repository.data.Faq
import de.tum.informatics.www1.artemis.native_app.feature.faq.ui.rememberFaqArtemisMarkdownTransformer
import de.tum.informatics.www1.artemis.native_app.feature.faq.ui.shared.FaqCategoryChipRow
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Serializable
data class FaqDetailUi(
    val courseId: Long,
    val faqId: Long,
)

fun NavController.navigateToFaqDetail(
    courseId: Long,
    faqId: Long,
    builder: NavOptionsBuilder.() -> Unit
) {
    navigate(FaqDetailUi(courseId, faqId), builder)
}

fun NavGraphBuilder.faqDetail(
    onNavigateBack: () -> Unit,
) {
    animatedComposable<FaqDetailUi> { backStackEntry ->
        val route: FaqDetailUi = backStackEntry.toRoute()

        val courseId = route.courseId
        val faqId = route.faqId

        val viewModel = koinViewModel<FaqDetailViewModel> { parametersOf(courseId, faqId) }
        val faq by viewModel.faq.collectAsState()

        val serverUrl by viewModel.serverUrl.collectAsState()
        val markdownTransformer = rememberFaqArtemisMarkdownTransformer(serverUrl)

        CompositionLocalProvider(LocalMarkdownTransformer provides markdownTransformer) {
            FaqDetailUi(
                modifier = Modifier.fillMaxSize(),
                faqDataState = faq,
                onReloadRequest = viewModel::requestReload,
                onNavigateBack = onNavigateBack
            )
        }
    }
}

@Composable
fun FaqDetailUi(
    modifier: Modifier = Modifier,
    faqDataState: DataState<Faq>,
    onReloadRequest: () -> Unit,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = { NavigationBackButton(onNavigateBack) }
            )
        }
    ) { paddingValues ->
        BasicDataStateUi(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
                .consumeWindowInsets(WindowInsets.systemBars.only(WindowInsetsSides.Top))
                .padding(horizontal = Spacings.ScreenHorizontalSpacing),
            dataState = faqDataState,
            loadingText = stringResource(id = R.string.faq_loading_faq_loading),
            failureText = stringResource(id = R.string.faq_loading_faq_failed),
            retryButtonText = stringResource(id = R.string.faq_loading_faq_try_again),
            onClickRetry = onReloadRequest
        ) { faq ->
            ProvideMarkwon(
                useOriginalImageSize = true
            ) {
                FaqDetail(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .navigationBarsPadding(),
                    faq = faq
                )
            }
        }
    }
}

@Composable
private fun FaqDetail(
    modifier: Modifier = Modifier,
    faq: Faq,
) {
    Column(
        modifier = modifier,
    ) {

        Text(
            text = faq.questionTitle,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        FaqCategoryChipRow(
            modifier = Modifier.padding(
                top = 8.dp,
                bottom = 16.dp
            ),
            categories = faq.categories
        )

        MarkdownText(
            markdown = faq.questionAnswer,
            style = MaterialTheme.typography.bodyLarge,
        )

        Spacer(modifier = Modifier
            .height(Spacings.EndOfScrollablePageSpacing)
        )
    }
}