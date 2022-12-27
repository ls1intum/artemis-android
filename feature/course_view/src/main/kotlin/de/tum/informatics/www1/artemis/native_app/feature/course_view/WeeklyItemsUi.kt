package de.tum.informatics.www1.artemis.native_app.feature.course_view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.daysUntil
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toLocalDateTime
import java.text.SimpleDateFormat
import java.util.Date

/**
 * Displays a lazy column for each weekly items entry. For each entry, a section header is displayed.
 * Sections can be collapsed and expanded
 */
@Composable
internal fun <T> WeeklyItemsLazyColumn(
    modifier: Modifier,
    weeklyItemGroups: List<GroupedByWeek<T>>,
    getItemId: T.() -> Long,
    itemContent: @Composable (T) -> Unit
) {
    val weeklyItemsSectionExpanded: MutableMap<GroupedByWeek<T>, Boolean> = remember(
        weeklyItemGroups
    ) {
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

        SnapshotStateMap<GroupedByWeek<T>, Boolean>().apply {
            putAll(
                weeklyItemGroups
                    .map {
                        it to when (it) {
                            is GroupedByWeek.Unbound -> true
                            is GroupedByWeek.BoundToWeek -> {
                                it.firstDayOfWeek.daysUntil(today) < 14
                            }
                        }
                    }
            )
        }
    }

    LazyColumn(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        weeklyItemGroups.forEachIndexed { index, weeklyItems ->
            item {
                WeeklyItemsSectionHeader(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = {
                            weeklyItemsSectionExpanded[weeklyItems] =
                                weeklyItemsSectionExpanded[weeklyItems] != true
                        })
                        .padding(horizontal = 16.dp),
                    weeklyItems = weeklyItems,
                    expanded = weeklyItemsSectionExpanded[weeklyItems] == true,
                )
            }

            if (weeklyItemsSectionExpanded[weeklyItems] == true) {
                items(weeklyItems.items, key = getItemId) { item ->
                    itemContent(item)
                }
            }

            if (index < weeklyItemGroups.size - 1) {
                item { Divider() }
            }
        }
    }
}

/**
 * Display a title with the time range of the week or a text indicating that no time is bound.
 * Displays an icon button that lets the user expand and collapse the weekly items
 * @param expanded if the item group this is showing is expanded
 */
@Composable
internal fun <T> WeeklyItemsSectionHeader(
    modifier: Modifier,
    weeklyItems: GroupedByWeek<T>,
    expanded: Boolean
) {
    val text = when (weeklyItems) {
        is GroupedByWeek.BoundToWeek -> {
            val (fromText, toText) = remember(
                weeklyItems.firstDayOfWeek,
                weeklyItems.lastDayOfWeek
            ) {
                val format = SimpleDateFormat.getDateInstance(SimpleDateFormat.MEDIUM)

                val dateToInstant = { date: LocalDate ->
                    Date.from(date.atStartOfDayIn(TimeZone.currentSystemDefault()).toJavaInstant())
                }

                val fromDate = dateToInstant(weeklyItems.firstDayOfWeek)
                val toData = dateToInstant(weeklyItems.lastDayOfWeek)

                format.format(fromDate) to format.format(toData)
            }

            stringResource(id = R.string.course_ui_exercise_list_week_header, fromText, toText)
        }

        is GroupedByWeek.Unbound -> stringResource(id = R.string.course_ui_exercise_list_unbound_week_header)
    }


    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier.weight(1f),
            text = text,
            style = MaterialTheme.typography.titleMedium
        )

        val icon = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore
        val contentDescription =
            stringResource(
                id = if (expanded) R.string.course_ui_list_expand_button_less_content_info
                else R.string.course_ui_list_expand_button_more_content_info
            )

        Icon(icon, contentDescription)
    }
}