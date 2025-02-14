package de.tum.informatics.www1.artemis.native_app.feature.faq.ui.shared

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.ui.compose.BackgroundColorBasedTextColor
import de.tum.informatics.www1.artemis.native_app.feature.faq.repository.data.FaqCategory


internal fun testTagForCategoryFilterChip(category: FaqCategory) = "TEST_TAG_FAQ_CATEGORY_FILTER_${category.name}"
internal fun testTagForCategoryColorfulChip(category: FaqCategory) = "TEST_TAG_FAQ_CATEGORY_COLORFUL_${category.name}"


@Composable
internal fun FaqCategoryChipRow(
    modifier: Modifier = Modifier,
    categories: Iterable<FaqCategory>
) {
    SelectableFaqCategoryChipRow(
        modifier = modifier,
        configuredFaqCategories = categories.map {
            ConfiguredFaqCategoryChip(
                category = it,
                config = FaqCategoryChipConfig.Colorful
            )
        }
    )
}


@Composable
internal fun SelectableFaqCategoryChipRow(
    modifier: Modifier = Modifier,
    configuredFaqCategories: Iterable<ConfiguredFaqCategoryChip>
) {
    Row(
        modifier = modifier
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        configuredFaqCategories.forEach {
            FaqCategoryChip(
                faqCategory = it.category,
                config = it.config
            )
        }
    }
}


@Composable
internal fun FaqCategoryChip(
    modifier: Modifier = Modifier,
    faqCategory: FaqCategory,
    config: FaqCategoryChipConfig = FaqCategoryChipConfig.Colorful
) {
    val chipColor = Color(faqCategory.color.toArgb())
    val textColor = BackgroundColorBasedTextColor.of(chipColor)

    val filterConfig = config as? FaqCategoryChipConfig.Filter

    val testTag = when (config) {
        is FaqCategoryChipConfig.Colorful -> testTagForCategoryColorfulChip(faqCategory)
        is FaqCategoryChipConfig.Filter -> testTagForCategoryFilterChip(faqCategory)
    }

    FilterChip(
        modifier = modifier
            .testTag(testTag),
        enabled = filterConfig != null,
        selected = filterConfig?.isSelected ?: false,
        onClick = filterConfig?.onClick ?: {},
        colors = FilterChipDefaults.filterChipColors().copy(
            // To not make it too colorful, we only apply the color to the disabled state (we use
            // the default Material3 colors for the selectable category chips)
            disabledContainerColor = chipColor,
            disabledLabelColor = textColor,
        ),
        leadingIcon = {
            AnimatedVisibility(
                visible = filterConfig?.isSelected == true,
                enter = fadeIn() + expandHorizontally() + scaleIn(),
                exit = fadeOut() + shrinkHorizontally() + scaleOut()
            ) {
                Icon(
                    modifier = Modifier.size(18.dp),
                    imageVector = Icons.Filled.Check,
                    contentDescription = null
                )
            }
        },
        label = {
            Text(
                modifier = Modifier.padding(4.dp),
                text = faqCategory.name,
            )
        }
    )
}