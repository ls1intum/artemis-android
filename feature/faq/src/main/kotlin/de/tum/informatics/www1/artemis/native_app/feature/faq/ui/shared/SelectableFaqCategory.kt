package de.tum.informatics.www1.artemis.native_app.feature.faq.ui.shared

import de.tum.informatics.www1.artemis.native_app.feature.faq.repository.data.FaqCategory

data class SelectableFaqCategory(
    val category: FaqCategory,
    val selectionConfig: FaqCategoryChipSelectionConfig
)


interface FaqCategoryChipSelectionConfig {
    data object Disabled : FaqCategoryChipSelectionConfig

    data class Enabled(
        val isSelected: Boolean,
        val onClick: () -> Unit
    ) : FaqCategoryChipSelectionConfig
}
