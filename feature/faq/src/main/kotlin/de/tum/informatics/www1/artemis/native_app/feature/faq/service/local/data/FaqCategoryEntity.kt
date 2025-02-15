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
            parentColumns = ["client_side_id"],
            childColumns = ["course_client_side_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["course_client_side_id", "name"], unique = true)]
)
data class FaqCategoryEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "client_side_id")
    val clientSideId: Long = 0,
    @ColumnInfo(name = "course_client_side_id")
    val courseClientSideId: Long,
    @ColumnInfo(name = "color")
    val color: String,
    @ColumnInfo(name = "name")
    val name: String,
)