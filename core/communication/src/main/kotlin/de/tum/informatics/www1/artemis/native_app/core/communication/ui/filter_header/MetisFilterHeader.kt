package de.tum.informatics.www1.artemis.native_app.core.communication.ui.filter_header

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Pending
import androidx.compose.material.icons.filled.Recommend
import androidx.compose.material.icons.filled.Reply
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.communication.R
import de.tum.informatics.www1.artemis.native_app.core.communication.ui.getHumanReadableTextForCourseWideContext
import de.tum.informatics.www1.artemis.native_app.core.communication.ui.getIconForCourseWideContext
import de.tum.informatics.www1.artemis.native_app.core.datastore.model.metis.MetisContext
import de.tum.informatics.www1.artemis.native_app.core.datastore.model.metis.MetisFilter
import de.tum.informatics.www1.artemis.native_app.core.datastore.model.metis.MetisSortingStrategy
import de.tum.informatics.www1.artemis.native_app.core.model.metis.CourseWideContext
import de.tum.informatics.www1.artemis.native_app.core.ui.common.DropdownButton

@Composable
internal fun MetisFilterHeader(
    modifier: Modifier,
    context: MetisContext,
    courseWideContext: CourseWideContext?,
    metisFilter: List<MetisFilter>,
    metisSortingStrategy: MetisSortingStrategy,
    query: String,
    selectCourseWideContext: (CourseWideContext?) -> Unit,
    onSelectFilter: (MetisFilter, Boolean) -> Unit,
    onChangeMetisSortingStrategy: (MetisSortingStrategy) -> Unit,
    onUpdateQuery: (String) -> Unit
) {
    @Suppress("LocalVariableName")
    val MetisFilterRow: @Composable (Modifier) -> Unit =
        @Composable { filterRowModifier ->
            FilterChipRow(
                modifier = filterRowModifier,
                filterRowType = FilterRowType.MetisFilterRowType(
                    selectedFilter = metisFilter,
                    onSelectFilter = onSelectFilter
                )
            )
        }

    @Suppress("LocalVariableName")
    val CourseWideContextFilterRow: @Composable (Modifier) -> Unit =
        @Composable { courseWideContextRowModifier ->
            FilterChipRow(
                modifier = courseWideContextRowModifier,
                filterRowType = FilterRowType.CourseWideContextFilterRowType(
                    currentCourseWideContext = courseWideContext,
                    selectCourseWideContext = selectCourseWideContext
                )
            )
        }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        var displayTuning by rememberSaveable(context) {
            mutableStateOf(false)
        }

        var displaySearch by rememberSaveable(context) {
            mutableStateOf(false)
        }

        Row(modifier = modifier, horizontalArrangement = Arrangement.End) {
            AnimatedVisibility(
                modifier = Modifier.weight(1f),
                visible = !displayTuning
            ) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    if (context is MetisContext.Course) {
                        CourseWideContextFilterRow(Modifier.weight(1f))
                    } else {
                        MetisFilterRow(Modifier.weight(1f))
                    }

                    IconButton(onClick = { displayTuning = true }) {
                        Icon(imageVector = Icons.Default.Tune, contentDescription = null)
                    }
                }
            }

            AnimatedVisibility(visible = !displaySearch) {
                IconButton(onClick = { displaySearch = true }) {
                    Icon(imageVector = Icons.Default.Search, contentDescription = null)
                }
            }
        }

        AnimatedVisibility(visible = displayTuning) {
            TuneCard(
                modifier = Modifier.fillMaxWidth(),
                context = context,
                displayMetisFilterRow = MetisFilterRow,
                displayCourseWideContextFilterRow = CourseWideContextFilterRow,
                metisSortingStrategy = metisSortingStrategy,
                onChangeMetisSortingStrategy = onChangeMetisSortingStrategy,
                hideTuning = { displayTuning = false }
            )
        }

        AnimatedVisibility(visible = displaySearch) {
            FilterQueryPosts(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                query = query,
                onUpdateQuery = onUpdateQuery,
                onClose = {
                    onUpdateQuery("")
                    displaySearch = false
                }
            )
        }
    }
}

@Composable
private fun TuneCard(
    modifier: Modifier,
    context: MetisContext,
    displayMetisFilterRow: @Composable (Modifier) -> Unit,
    displayCourseWideContextFilterRow: @Composable (Modifier) -> Unit,
    metisSortingStrategy: MetisSortingStrategy,
    onChangeMetisSortingStrategy: (MetisSortingStrategy) -> Unit,
    hideTuning: () -> Unit
) {
    OutlinedCard(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(
                    onClick = hideTuning
                ) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = null)
                }
            }

            Column(modifier = Modifier.padding(8.dp)) {
                if (context is MetisContext.Course) {
                    displayMetisFilterRow(Modifier.fillMaxWidth())

                    Divider()
                }

                displayCourseWideContextFilterRow(Modifier.fillMaxWidth())

                Divider()

                SelectMetisSortingStrategy(
                    modifier = Modifier.fillMaxWidth(),
                    currentSortingStrategy = metisSortingStrategy,
                    onSelectSortingStrategy = onChangeMetisSortingStrategy
                )
            }
        }
    }
}

/**
 * Displays a row of filter chips depending on the context.
 */
@Composable
private fun FilterChipRow(modifier: Modifier, filterRowType: FilterRowType) {
    Row(
        modifier = modifier.then(Modifier.horizontalScroll(rememberScrollState())),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        when (filterRowType) {
            is FilterRowType.CourseWideContextFilterRowType -> {
                CourseWideContext.values().forEach { cwc ->
                    CourseWideContextFilterChip(
                        modifier = Modifier,
                        courseWideContext = cwc,
                        selectedCourseWideContext = filterRowType.currentCourseWideContext,
                        onSelect = filterRowType.selectCourseWideContext
                    )
                }
            }

            is FilterRowType.MetisFilterRowType -> {
                MetisFilter.values().forEach { mf ->
                    MetisFilterChip(
                        modifier = Modifier,
                        metisFilter = mf,
                        selectedFilters = filterRowType.selectedFilter,
                        onSelectFilter = filterRowType.onSelectFilter
                    )
                }
            }

            else -> {}
        }
    }
}

sealed class FilterRowType {
    class CourseWideContextFilterRowType(
        val currentCourseWideContext: CourseWideContext?,
        val selectCourseWideContext: (CourseWideContext?) -> Unit
    ) : FilterRowType()

    class MetisFilterRowType(
        val selectedFilter: List<MetisFilter>,
        val onSelectFilter: (MetisFilter, Boolean) -> Unit
    ) : FilterRowType()
}

@Composable
private fun CourseWideContextFilterChip(
    modifier: Modifier,
    courseWideContext: CourseWideContext,
    selectedCourseWideContext: CourseWideContext?,
    onSelect: (CourseWideContext?) -> Unit
) {
    val icon = getIconForCourseWideContext(courseWideContext = courseWideContext)
    val text = getHumanReadableTextForCourseWideContext(courseWideContext = courseWideContext)

    val isSelected = courseWideContext == selectedCourseWideContext
    FilterChip(
        modifier = modifier,
        selected = isSelected,
        onClick = {
            if (isSelected) {
                onSelect(null)
            } else {
                onSelect(courseWideContext)
            }
        },
        leadingIcon = { Icon(imageVector = icon, contentDescription = null) },
        label = {
            Text(text = text)
        }
    )
}

@Composable
private fun MetisFilterChip(
    modifier: Modifier,
    metisFilter: MetisFilter,
    selectedFilters: List<MetisFilter>,
    onSelectFilter: (MetisFilter, Boolean) -> Unit
) {
    val icon = when (metisFilter) {
        MetisFilter.CREATED_BY_CLIENT -> Icons.Default.Smartphone
        MetisFilter.WITH_REACTION -> Icons.Default.Reply
        MetisFilter.UNRESOLVED -> Icons.Default.Pending
    }

    val text = stringResource(
        id = when (metisFilter) {
            MetisFilter.CREATED_BY_CLIENT -> R.string.metis_filter_client_created
            MetisFilter.WITH_REACTION -> R.string.metis_filter_with_reaction
            MetisFilter.UNRESOLVED -> R.string.metis_filter_unresolved
        }
    )

    val isSelected = metisFilter in selectedFilters
    FilterChip(
        modifier = modifier,
        selected = isSelected,
        onClick = {
            if (isSelected) {
                onSelectFilter(metisFilter, false)
            } else {
                onSelectFilter(metisFilter, true)
            }
        },
        leadingIcon = {
            Icon(imageVector = icon, contentDescription = null)
        },
        label = {
            Text(text = text)
        }
    )
}

@Composable
private fun SelectMetisSortingStrategy(
    modifier: Modifier,
    currentSortingStrategy: MetisSortingStrategy,
    onSelectSortingStrategy: (MetisSortingStrategy) -> Unit
) {
    val selectedCriteria: SortingCriteria = when (currentSortingStrategy) {
        MetisSortingStrategy.DATE_ASCENDING, MetisSortingStrategy.DATE_DESCENDING -> SortingCriteria.DATE
        MetisSortingStrategy.REPLIES_ASCENDING, MetisSortingStrategy.REPLIES_DESCENDING -> SortingCriteria.REPLIES
        MetisSortingStrategy.VOTES_ASCENDING, MetisSortingStrategy.VOTES_DESCENDING -> SortingCriteria.VOTES

    }

    val sortingOrder: SortingOrder = when (currentSortingStrategy) {
        MetisSortingStrategy.DATE_ASCENDING, MetisSortingStrategy.REPLIES_ASCENDING, MetisSortingStrategy.VOTES_ASCENDING -> SortingOrder.ASCENDING
        MetisSortingStrategy.DATE_DESCENDING, MetisSortingStrategy.REPLIES_DESCENDING, MetisSortingStrategy.VOTES_DESCENDING -> SortingOrder.DESCENDING
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = stringResource(id = R.string.metis_sorting_strategy_criteria_title))

        DropdownButton(
            modifier = Modifier,
            text = stringResource(id = selectedCriteria.textRes),
            icon = selectedCriteria.icon
        ) { hide ->
            SortingCriteria.values().forEach { sortingCriteria ->
                DropdownMenuItem(
                    text = { Text(text = stringResource(id = sortingCriteria.textRes)) },
                    leadingIcon = {
                        Icon(
                            imageVector = sortingCriteria.icon,
                            contentDescription = null
                        )
                    },
                    onClick = {
                        hide()

                        onSelectSortingStrategy(
                            constructSortingStrategy(
                                sortingCriteria,
                                sortingOrder
                            )
                        )
                    }
                )
            }
        }

        Text(text = stringResource(id = R.string.metis_sorting_strategy_order_title))

        DropdownButton(
            modifier = Modifier,
            text = stringResource(id = sortingOrder.textRes),
            icon = sortingOrder.icon
        ) { hide ->
            SortingOrder.values().forEach { order ->
                DropdownMenuItem(
                    text = { Text(text = stringResource(id = order.textRes)) },
                    leadingIcon = { Icon(imageVector = order.icon, contentDescription = null) },
                    onClick = {
                        hide()

                        onSelectSortingStrategy(
                            constructSortingStrategy(
                                selectedCriteria,
                                order
                            )
                        )
                    }
                )
            }
        }
    }
}

private enum class SortingCriteria(val textRes: Int, val icon: ImageVector) {
    DATE(R.string.metis_sorting_strategy_criteria_date, Icons.Default.History),
    REPLIES(R.string.metis_sorting_strategy_criteria_replies, Icons.Default.Comment),
    VOTES(R.string.metis_sorting_strategy_criteria_votes, Icons.Default.Recommend)
}

private enum class SortingOrder(val textRes: Int, val icon: ImageVector) {
    DESCENDING(R.string.metis_sorting_strategy_order_descending, Icons.Default.ArrowDownward),
    ASCENDING(R.string.metis_sorting_strategy_order_ascending, Icons.Default.ArrowUpward),
}

private fun constructSortingStrategy(
    sortingCriteria: SortingCriteria,
    sortingOrder: SortingOrder
): MetisSortingStrategy {
    return when (sortingCriteria) {
        SortingCriteria.DATE -> {
            when (sortingOrder) {
                SortingOrder.DESCENDING -> MetisSortingStrategy.DATE_DESCENDING
                SortingOrder.ASCENDING -> MetisSortingStrategy.DATE_ASCENDING
            }
        }

        SortingCriteria.REPLIES -> {
            when (sortingOrder) {
                SortingOrder.DESCENDING -> MetisSortingStrategy.REPLIES_DESCENDING
                SortingOrder.ASCENDING -> MetisSortingStrategy.REPLIES_ASCENDING
            }
        }

        SortingCriteria.VOTES -> {
            when (sortingOrder) {
                SortingOrder.DESCENDING -> MetisSortingStrategy.VOTES_DESCENDING
                SortingOrder.ASCENDING -> MetisSortingStrategy.VOTES_ASCENDING
            }
        }
    }
}

private class MetisContextProvider : PreviewParameterProvider<MetisContext> {
    override val values: Sequence<MetisContext> = sequenceOf(
        MetisContext.Course(0),
        MetisContext.Exercise(0, 0),
        MetisContext.Lecture(0, 0)
    )
}

@Preview
@Composable
private fun MetisFilterHeaderPreview(
    @PreviewParameter(provider = MetisContextProvider::class) metisContext: MetisContext
) {
    var courseWideContext: CourseWideContext? by remember { mutableStateOf(null) }
    var filter: List<MetisFilter> by remember { mutableStateOf(emptyList()) }
    var sortingStrategy: MetisSortingStrategy by remember {
        mutableStateOf(MetisSortingStrategy.DATE_DESCENDING)
    }

    var query: String by remember { mutableStateOf("") }

    MetisFilterHeader(
        modifier = Modifier.fillMaxWidth(),
        context = metisContext,
        courseWideContext = courseWideContext,
        metisFilter = filter,
        selectCourseWideContext = { new -> courseWideContext = new },
        onSelectFilter = { f, selected ->
            filter = if (selected) {
                filter + f
            } else {
                filter.filterNot { it == f }
            }
        },
        metisSortingStrategy = sortingStrategy,
        onChangeMetisSortingStrategy = { sortingStrategy = it },
        query = query,
        onUpdateQuery = { query = it }
    )
}

@Preview
@Composable
private fun SelectMetisSortingStrategyPreview() {
    var current: MetisSortingStrategy by remember { mutableStateOf(MetisSortingStrategy.DATE_DESCENDING) }

    SelectMetisSortingStrategy(
        modifier = Modifier.fillMaxWidth(),
        currentSortingStrategy = current,
        onSelectSortingStrategy = { current = it }
    )
}