package de.tum.informatics.www1.artemis.native_app.core.communication.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import de.tum.informatics.www1.artemis.native_app.core.communication.R
import de.tum.informatics.www1.artemis.native_app.core.communication.ui.list.MetisListViewModel
import de.tum.informatics.www1.artemis.native_app.core.communication.ui.list.MetisStandalonePostList
import de.tum.informatics.www1.artemis.native_app.core.communication.ui.standalone_post.ViewType
import de.tum.informatics.www1.artemis.native_app.core.communication.ui.standalone_post.navigateToStandalonePostScreen
import de.tum.informatics.www1.artemis.native_app.core.datastore.model.metis.MetisContext
import de.tum.informatics.www1.artemis.native_app.core.datastore.model.metis.MetisSortingStrategy
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun SmartphoneMetisUi(
    modifier: Modifier,
    metisContext: MetisContext,
    navController: NavController
) {
    val viewModel: MetisListViewModel = koinViewModel { parametersOf(metisContext) }
    val courseWideContext = viewModel.courseWideContext.collectAsState(initial = null).value
    val metisFilter = viewModel.filter.collectAsState(initial = emptyList()).value
    val sortingStrategy = viewModel
        .sortingStrategy
        .collectAsState(initial = MetisSortingStrategy.DATE_DESCENDING)
        .value

    val query: String = viewModel.query.collectAsState(initial = "").value

    Column(modifier = modifier) {
        MetisFilterHeader(
            modifier = Modifier.fillMaxWidth(),
            context = metisContext,
            courseWideContext = courseWideContext,
            metisFilter = metisFilter,
            metisSortingStrategy = sortingStrategy,
            query = query,
            selectCourseWideContext = viewModel::updateCourseWideContext,
            onSelectFilter = { selectedFilter, isSelected ->
                if (isSelected) {
                    viewModel.addMetisFilter(selectedFilter)
                } else {
                    viewModel.removeMetisFilter(selectedFilter)
                }
            },
            onChangeMetisSortingStrategy = viewModel::updateSortingStrategy,
            onUpdateQuery = viewModel::updateQuery
        )

        MetisStandalonePostList(
            modifier = modifier,
            viewModel = viewModel,
            onClickReply = { clientPostId ->
                navController.navigateToStandalonePostScreen(
                    clientPostId = clientPostId,
                    viewType = ViewType.WRITE_COMMENT
                ) {}
            },
            onClickViewReplies = { clientPostId ->
                navController.navigateToStandalonePostScreen(
                    clientPostId = clientPostId,
                    viewType = ViewType.REPLIES
                ) {}
            }
        )
    }
}

@Composable
internal fun ColumnScope.MetisOutdatedBanner(modifier: Modifier, isOutdated: Boolean) {
    AnimatedVisibility(visible = isOutdated) {
        Box(
            modifier = modifier.then(
                Modifier.background(
                    MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(15)
                )
            )
        ) {
            Text(
                text = stringResource(id = R.string.metis_outdated_data_banner_text),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}