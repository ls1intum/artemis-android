package de.tum.informatics.www1.artemis.native_app.feature.metis.ui.qna

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.ui.getWindowSizeClass
import de.tum.informatics.www1.artemis.native_app.feature.metis.model.MetisContext
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.qna.filter_header.MetisFilterHeader
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.post_list.MetisListViewModel

@Composable
internal fun ViewModelMetisFilterHeaderImpl(
    modifier: Modifier,
    metisContext: MetisContext,
    viewModel: MetisListViewModel
) {
    val query: String by viewModel.query.collectAsState()

    val courseWideContext by viewModel.courseWideContext.collectAsState()
    val metisFilter by viewModel.filter.collectAsState()
    val sortingStrategy by viewModel.sortingStrategy.collectAsState()

    MetisFilterHeader(
        modifier = modifier,
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
}

/**
 * If it is feasible to display metis on the side next to the main content of the screen.
 * @param parentWidth the maximum width of the parent in which the metis ui should be displayed
 * @param metisContentRatio the percentage of the screen width associated with metis
 */
@Composable
fun canDisplayMetisOnDisplaySide(
    windowSizeClass: WindowSizeClass = getWindowSizeClass(),
    parentWidth: Dp,
    metisContentRatio: Float
): Boolean {
    return (windowSizeClass.widthSizeClass >= WindowWidthSizeClass.Expanded
            && (parentWidth * metisContentRatio) >= 300.dp)
}