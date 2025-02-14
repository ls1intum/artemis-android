package de.tum.informatics.www1.artemis.native_app.feature.faq.ui.shared

import de.tum.informatics.www1.artemis.native_app.feature.faq.repository.data.FaqCategory

data class ConfiguredFaqCategoryChip(
    val category: FaqCategory,
    val config: FaqCategoryChipConfig
)


interface FaqCategoryChipConfig {
    data object Colorful : FaqCategoryChipConfig

    data class Filter(
        val isSelected: Boolean,
        val onClick: () -> Unit
    ) : FaqCategoryChipConfig
}
