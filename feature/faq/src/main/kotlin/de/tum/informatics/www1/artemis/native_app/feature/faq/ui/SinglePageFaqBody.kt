package de.tum.informatics.www1.artemis.native_app.feature.faq.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import de.tum.informatics.www1.artemis.native_app.core.ui.ArtemisAppLayout
import de.tum.informatics.www1.artemis.native_app.core.ui.R.drawable.sidebar_icon
import de.tum.informatics.www1.artemis.native_app.core.ui.common.course.CourseSearchConfiguration
import de.tum.informatics.www1.artemis.native_app.core.ui.common.tablet.LayoutAwareTwoColumnLayout
import de.tum.informatics.www1.artemis.native_app.core.ui.common.top_app_bar.CollapsingContentState
import de.tum.informatics.www1.artemis.native_app.core.ui.getArtemisAppLayout
import de.tum.informatics.www1.artemis.native_app.core.ui.isTabletPortrait
import de.tum.informatics.www1.artemis.native_app.feature.faq.R
import de.tum.informatics.www1.artemis.native_app.feature.faq.ui.detail.FaqDetailContent
import de.tum.informatics.www1.artemis.native_app.feature.faq.ui.overview.FaqOverviewUi
import de.tum.informatics.www1.artemis.native_app.feature.faq.ui.overview.FaqOverviewViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun SinglePageFaqBody(
    modifier: Modifier = Modifier,
    collapsingContentState: CollapsingContentState,
    onNavigateToFaq: (Long) -> Unit,
    scaffold: @Composable (searchConfiguration: CourseSearchConfiguration, content: @Composable () -> Unit) -> Unit,
    title: String
) {
    val layout = getArtemisAppLayout()
    val isTabletPortrait = layout.isTabletPortrait

    var config: FaqConfiguration by rememberSaveable { mutableStateOf(NothingOpened) }
    var isSidebarOpen by rememberSaveable { mutableStateOf(true) }

    val openFaq: (Long) -> Unit = { id ->
        if (isTabletPortrait && config is NothingOpened) {
            isSidebarOpen = false
        }
        config = OpenedFaq(faqId = id, _prev = config)
    }

    val viewModel = koinViewModel<FaqOverviewViewModel>()
    val query by viewModel.searchQuery.collectAsState()

    val searchConfiguration = CourseSearchConfiguration.Search(
        query = query,
        hint = stringResource(R.string.faq_search_hint),
        onUpdateQuery = viewModel::updateQuery
    )
    val onSidebarToggle: () -> Unit = { isSidebarOpen = !isSidebarOpen }

    scaffold(searchConfiguration) {
        when (layout) {
            ArtemisAppLayout.Phone -> {
                FaqOverviewUi(
                    modifier = Modifier.fillMaxSize(),
                    collapsingContentState = collapsingContentState,
                    viewModel = viewModel,
                    onNavigateToFaq = onNavigateToFaq,
                    selectedFaqId = null // No selection in phone mode
                )
            }

            ArtemisAppLayout.Tablet -> {
                LayoutAwareTwoColumnLayout(
                    modifier = modifier,
                    isSidebarOpen = isSidebarOpen,
                    onSidebarToggle = onSidebarToggle,
                    optionalColumn = { sideMod ->
                        FaqOverviewUi(
                            modifier = Modifier.fillMaxSize(),
                            collapsingContentState = collapsingContentState,
                            viewModel = viewModel,
                            onNavigateToFaq = openFaq,
                            selectedFaqId = (config as? OpenedFaq)?.faqId,
                        )
                    },
                    priorityColumn = { contentMod ->
                        when (val conf = config) {
                            NothingOpened -> {
                                IconButton(onClick = onSidebarToggle) {
                                    Icon(
                                        painter = painterResource(id = sidebar_icon),
                                        contentDescription = null
                                    )
                                }
                                Box(
                                    modifier = contentMod.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(stringResource(id = R.string.faq_overview_no_faqs_selected))
                                }
                            }

                            is OpenedFaq -> key(conf.faqId) {
                                FaqDetailContent(
                                    faqId = conf.faqId,
                                    onSidebarToggle = onSidebarToggle
                                )
                            }
                        }
                    },
                    title = title,
                    searchConfiguration = searchConfiguration
                )
            }
        }
    }
}
