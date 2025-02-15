package de.tum.informatics.www1.artemis.native_app.feature.faq.service.local.data

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(
    tableName = "faq_to_faq_category",
    primaryKeys = ["faq_id", "faq_category_id"]
)
data class FaqToFaqCategoryCrossRef(
    @ColumnInfo(name = "faq_id")
    val faqId: Long,
    @ColumnInfo(name = "faq_category_id")
    val faqCategoryId: Long
)