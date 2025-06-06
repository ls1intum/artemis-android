package de.tum.informatics.www1.artemis.native_app.feature.faq.ui.overview

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.ui.Spacings
import de.tum.informatics.www1.artemis.native_app.core.ui.common.BasicDataStateUi
import de.tum.informatics.www1.artemis.native_app.core.ui.common.EmptyListHint
import de.tum.informatics.www1.artemis.native_app.core.ui.common.NoSearchResults
import de.tum.informatics.www1.artemis.native_app.core.ui.common.selectionBorder
import de.tum.informatics.www1.artemis.native_app.core.ui.common.top_app_bar.CollapsingContentState
import de.tum.informatics.www1.artemis.native_app.core.ui.markdown.MarkdownText
import de.tum.informatics.www1.artemis.native_app.core.ui.markdown.ProvideMarkwon
import de.tum.informatics.www1.artemis.native_app.feature.faq.R
import de.tum.informatics.www1.artemis.native_app.feature.faq.repository.data.Faq
import de.tum.informatics.www1.artemis.native_app.feature.faq.repository.data.FaqState
import de.tum.informatics.www1.artemis.native_app.feature.faq.ui.shared.ConfiguredFaqCategoryChip
import de.tum.informatics.www1.artemis.native_app.feature.faq.ui.shared.ConfiguredFaqCategoryChipRow
import de.tum.informatics.www1.artemis.native_app.feature.faq.ui.shared.FaqCategoryChipConfig
import de.tum.informatics.www1.artemis.native_app.feature.faq.ui.shared.FaqCategoryChipFlowRow

internal fun testTagForFaq(faq: Faq) = "TEST_TAG_FAQ_${faq.id}"

@Composable
fun FaqOverviewUi(
    modifier: Modifier = Modifier,
    viewModel: FaqOverviewViewModel,
    collapsingContentState: CollapsingContentState,
    onNavigateToFaq: (faqId: Long) -> Unit,
    selectedFaqId: Long?
) {
    val faqs by viewModel.displayedFaqs.collectAsState()
    val query by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val allCategories by viewModel.allCategories.collectAsState()

    val filterChips = allCategories.map {
        ConfiguredFaqCategoryChip(
            category = it,
            config = FaqCategoryChipConfig.Filter(
                isSelected = selectedCategory == it,
                onClick = { viewModel.onToggleSelectableFaqCategory(it) }
            )
        )
    }

    FaqOverviewUi(
        modifier = modifier,
        faqsDataState = faqs,
        filterChips = filterChips,
        query = query,
        collapsingContentState = collapsingContentState,
        onReloadRequest = viewModel::onRequestReload,
        onNavigateToFaq = onNavigateToFaq,
        selectedFaqId = selectedFaqId
    )
}


@Composable
fun FaqOverviewUi(
    modifier: Modifier = Modifier,
    faqsDataState: DataState<List<Faq>>,
    filterChips: List<ConfiguredFaqCategoryChip>,
    query: String,
    collapsingContentState: CollapsingContentState,
    onReloadRequest: () -> Unit,
    onNavigateToFaq: (faqId: Long) -> Unit,
    selectedFaqId: Long? = null,
) {
    BasicDataStateUi(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = Spacings.ScreenHorizontalSpacing),
        dataState = faqsDataState,
        loadingText = stringResource(id = R.string.faq_loading_faqs_loading),
        failureText = stringResource(id = R.string.faq_loading_faqs_failed),
        retryButtonText = stringResource(id = R.string.faq_loading_faqs_try_again),
        onClickRetry = onReloadRequest
    ) { faqs ->
        ProvideMarkwon {
            FaqOverviewBody(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding(),
                faqs = faqs,
                filterChips = filterChips,
                collapsingContentState = collapsingContentState,
                query = query,
                onNavigateToFaq = onNavigateToFaq,
                selectedFaqId = selectedFaqId
            )
        }
    }
}


@Composable
private fun FaqOverviewBody(
    modifier: Modifier = Modifier,
    faqs: List<Faq>,
    filterChips: List<ConfiguredFaqCategoryChip>,
    collapsingContentState: CollapsingContentState,
    query: String,
    onNavigateToFaq: (Long) -> Unit,
    selectedFaqId: Long? = null,
) {
    val isSearching = query.isNotBlank()

    Column(
        modifier = modifier,
    ) {
        if (filterChips.size > 1) {
            ConfiguredFaqCategoryChipRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                configuredFaqCategories = filterChips
            )
        }

        if (faqs.isEmpty()) {
            if (isSearching) {
                NoSearchResults(
                    modifier = Modifier.fillMaxSize(),
                    title = stringResource(R.string.faq_overview_no_faqs),
                    details = stringResource(R.string.faq_overview_no_faqs_search, query)
                )
                return
            }

            EmptyListHint(
                modifier = Modifier.fillMaxSize(),
                hint = stringResource(R.string.faq_overview_no_faqs),
                imageVector = Icons.AutoMirrored.Filled.Help
            )
            return
        }

        FaqList(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(collapsingContentState.nestedScrollConnection),
            faqs = faqs,
            onNavigateToFaq = onNavigateToFaq,
            selectedFaqId = selectedFaqId
        )
    }
}


@Composable
private fun FaqList(
    modifier: Modifier = Modifier,
    faqs: List<Faq>,
    onNavigateToFaq: (Long) -> Unit,
    selectedFaqId: Long? = null,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = Spacings.calculateContentPaddingValues(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            items = faqs,
            key = { faq -> faq.id }
        ) { faq ->
            FaqPreviewItem(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateItem()
                    .testTag(testTagForFaq(faq)),
                faq = faq,
                onClick = { onNavigateToFaq(faq.id) },
                selected = selectedFaqId == faq.id
            )
        }
    }
}

@Composable
private fun FaqPreviewItem(
    modifier: Modifier = Modifier,
    faq: Faq,
    onClick: () -> Unit,
    selected: Boolean,
) {
    Card(
        modifier = modifier
            .clickable(onClick = onClick)
            .selectionBorder(selected),
    ) {
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = faq.questionTitle,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                FaqCategoryChipFlowRow(categories = faq.categories)

                MarkdownText(
                    modifier = Modifier
                        .padding(vertical = 8.dp),
                    markdown = faq.questionAnswer + "\n\n\n.",
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 8,
                    onClick = onClick
                )
            }

            // Overlay fading background with read more button, inspired by the iOS app
            val color = MaterialTheme.colorScheme.surfaceContainer
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                color.copy(alpha = 0.8f),
                                color,
                                color,
                            )
                        )
                    )
            ) {
                TextButton(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(16.dp)
                        .padding(top = 8.dp),
                    onClick = onClick
                ) {
                    Text(stringResource(R.string.faq_overview_read_more))
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 380, heightDp = 700)
@Composable
private fun FaqListPreview() {
    val faqs = listOf(
        Faq(
            id = 1,
            questionTitle = "How to create a new course?",
            questionAnswer = "To create a new course, you need to go to the course management page and click on the 'Create new course' button.",
            categories = emptyList(),
            faqState = FaqState.ACCEPTED,
        ),
        Faq(
            id = 2,
            questionTitle = "How to create a new exercise when I am the tutor in the course?",
            questionAnswer = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Cras ut massa dui. Vivamus scelerisque urna elit, vitae consequat urna pulvinar in. Praesent ut dolor non mauris porttitor fringilla ut ac elit. Fusce in eleifend metus. Interdum et malesuada fames ac ante ipsum primis in faucibus. Praesent faucibus nisl sit amet enim pharetra convallis. Donec nec facilisis nunc. Sed scelerisque lorem sit amet justo tristique, vitae consectetur mauris semper. Cras id dignissim risus. Nunc vitae convallis arcu, in interdum lorem. Donec vitae vulputate magna. Aliquam molestie bibendum tincidunt. ",
            categories = emptyList(),
            faqState = FaqState.ACCEPTED,
        ),
    )

    MaterialTheme {
        FaqList(
            modifier = Modifier.fillMaxSize(),
            faqs = faqs,
            onNavigateToFaq = {}
        )
    }
}
