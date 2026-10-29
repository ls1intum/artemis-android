package de.tum.informatics.www1.artemis.native_app.feature.metis.shared.db.pojo

import androidx.room.ColumnInfo
import androidx.room.Ignore
import androidx.room.Relation
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.IReaction
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.db.entities.MetisUserEntity
import kotlin.time.Instant

/**
 * Kept at the top level rather than nested in [PostPojo]: Room's KSP 2 processor resolves a nested
 * data class against its outer class and then reports the constructor as not matching the
 * properties.
 */
data class ReactionPojo(
    @ColumnInfo(name = "emoji")
    override val emojiId: String,
    @ColumnInfo(name = "author_id")
    val authorId: Long,
    @Relation(
        entity = MetisUserEntity::class,
        parentColumn = "author_id",
        entityColumn = "id",
        projection = ["name"]
    )
    val username: String,
    @ColumnInfo(name = "id")
    override val id: Long,
    @ColumnInfo(name = "creation_date")
    override val creationDate: Instant?
) : IReaction {
    @Ignore
    override val creatorId: Long = authorId
}
