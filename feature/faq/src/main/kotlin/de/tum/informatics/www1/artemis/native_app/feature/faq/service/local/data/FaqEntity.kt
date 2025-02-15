package de.tum.informatics.www1.artemis.native_app.feature.faq.service.local.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import de.tum.informatics.www1.artemis.native_app.core.datastore.room.course.CourseEntity


@Entity(
    tableName = "faq",
    foreignKeys = [
        ForeignKey(
            entity = CourseEntity::class,
            parentColumns = ["client_side_id"],
            childColumns = ["course_client_side_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["course_client_side_id", "id"], unique = true)]
)
data class FaqEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "local_faq_id")
    val localId: Long = 0,
    @ColumnInfo(name = "id")
    val id: Long,
    @ColumnInfo(name = "course_client_side_id")
    val courseClientSideId: Long,
    @ColumnInfo(name = "question")
    val question: String,
    @ColumnInfo(name = "answer")
    val answer: String,
    @ColumnInfo(name = "faq_state")
    val faqState: String
)
