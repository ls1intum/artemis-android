package de.tum.informatics.www1.artemis.native_app.feature.faq.service.local.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "faq_to_faq_category",
    primaryKeys = ["faq_local_id", "faq_category_local_id"],
    foreignKeys = [
        ForeignKey(
            entity = FaqEntity::class,
            parentColumns = ["faq_local_id"],
            childColumns = ["faq_local_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = FaqCategoryEntity::class,
            parentColumns = ["faq_category_local_id"],
            childColumns = ["faq_category_local_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class FaqToFaqCategoryCrossRef(
    @ColumnInfo(name = "faq_local_id")
    val faqId: Long,
    @ColumnInfo(name = "faq_category_local_id")
    val faqCategoryId: Long
)