package de.tum.informatics.www1.artemis.native_app.feature.faq.ui.overview

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.ui.common.BasicDataStateUi
import de.tum.informatics.www1.artemis.native_app.core.ui.markdown.MarkdownText
import de.tum.informatics.www1.artemis.native_app.core.ui.markdown.ProvideMarkwon
import de.tum.informatics.www1.artemis.native_app.feature.faq.R
import de.tum.informatics.www1.artemis.native_app.feature.faq.repository.data.Faq
import de.tum.informatics.www1.artemis.native_app.feature.faq.repository.data.FaqState
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun FaqOverviewUi(
    modifier: Modifier = Modifier,
    courseId: Long,
    onNavigateToFaq: (faqId: Long) -> Unit
) {
    val viewModel = koinViewModel<FaqOverviewViewModel> { parametersOf(courseId) }

    val faqs by viewModel.faqs.collectAsState()

    FaqOverviewUi(
        modifier = modifier,
        faqsDataState = faqs,
        onReloadRequest = viewModel::requestReload,
        onNavigateToFaq = onNavigateToFaq
    )
}



@Composable
fun FaqOverviewUi(
    modifier: Modifier = Modifier,
    faqsDataState: DataState<List<Faq>>,
    onReloadRequest: () -> Unit,
    onNavigateToFaq: (faqId: Long) -> Unit
) {
    BasicDataStateUi(
        modifier = modifier.fillMaxWidth(),
        dataState = faqsDataState,
        loadingText = stringResource(id = R.string.faq_loading_faqs_loading),
        failureText = stringResource(id = R.string.faq_loading_faqs_failed),
        retryButtonText = stringResource(id = R.string.faq_loading_faqs_try_again),
        onClickRetry = onReloadRequest
    ) { faqs ->
        // TODO: empty state
        // TODO: search bar

        ProvideMarkwon {
            FaqList(
                modifier = Modifier.padding(16.dp),
                faqs = faqs,
                onNavigateToFaq = onNavigateToFaq
            )
        }
    }
}


@Composable
private fun FaqList(
    modifier: Modifier = Modifier,
    faqs: List<Faq>,
    onNavigateToFaq: (Long) -> Unit
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(faqs) { faq ->
            FaqPreviewItem(
                modifier = Modifier.fillMaxWidth(),
                faq = faq,
                onClick = { onNavigateToFaq(faq.id) }
            )
        }
    }
}

@Composable
private fun FaqPreviewItem(
    modifier: Modifier = Modifier,
    faq: Faq,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clickable(onClick = onClick),
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = faq.questionTitle,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // TODO: Images are not displayed. I am not sure why
            MarkdownText(
                markdown = faq.questionAnswer,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 8,
            )

            TextButton(
                onClick = onClick,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(stringResource(R.string.faq_overview_read_more))
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



