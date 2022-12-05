package de.tum.informatics.www1.artemis.native_app.core.communication.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
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