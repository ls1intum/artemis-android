package de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto

import android.renderscript.Type
import de.tum.informatics.www1.artemis.native_app.core.model.account.User
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
sealed class BasePost : IBasePost {
    abstract val id: Long?
    abstract val author: User?
    abstract override val authorRole: UserRole?
    abstract override val creationDate: Instant?
    abstract override val updatedDate: Instant?
    abstract override val content: String?
    abstract override val reactions: List<Reaction>?
    abstract override val isSaved: Boolean?

    override val authorName: String?
        get() = author?.name

    override val authorImageUrl: String?
        get() = author?.imageUrl
}

// TODO: how to create and use custom serializer
class PostDeserializer : JsonDeserializer<BasePost> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): BasePost {
        val jsonObject = json.asJsonObject
        val postType = jsonObject.get("postType").asInt

        return when (postType) {
            0 -> context.deserialize(json, StandalonePost::class.java)
            1 -> context.deserialize(json, AnswerPost::class.java)
            else -> throw IllegalArgumentException("Unknown post type: $postType")
        }
    }
}