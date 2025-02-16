package de.tum.informatics.www1.artemis.native_app.feature.faq.service.local.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import de.tum.informatics.www1.artemis.native_app.core.datastore.room.course.CourseEntity

@Entity(
    tableName = "faq_category",
    foreignKeys = [
        ForeignKey(
            entity = CourseEntity::class,
            parentColumns = ["course_local_id"],
            childColumns = ["course_local_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["course_local_id", "name"], unique = true)]
)
data class FaqCategoryEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "faq_category_local_id")
    val localId: Long = 0,
    @ColumnInfo(name = "course_local_id")
    val courseLocalId: Long,
    @ColumnInfo(name = "color")
    val color: String,
    @ColumnInfo(name = "name")
    val name: String,
)