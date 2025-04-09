package de.tum.informatics.www1.artemis.native_app.feature.courseview

import de.tum.informatics.www1.artemis.native_app.core.ui.ScreenshotData
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.shared.ui.ChatListItem
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.ChannelChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.ConversationUser
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.GroupChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.OneToOneChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.db.pojo.PostPojo
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.minutes

object ScreenshotCommunicationData {

    val conversation = ChannelChat(
        id = 1L,
        name = "Designing a rocket engine - Q&A",
        unreadMessagesCount = 17,
        numberOfMembers = 64
    )

    val student1 = ConversationUser(
        id = 0,
        username = "u1",
        firstName = "Sam",
        lastName = "Smith"
    )

    val student2 = ConversationUser(
        id = 1,
        username = "u2",
        firstName = "Mia",
        lastName = "Johnson"
    )

    val tutor1 = ConversationUser(
        id = 3,
        username = "u3",
        firstName = "Ethan",
        lastName = "Martin",
        imageUrl = ScreenshotData.Images.IMAGE_MARS
    )

    val conversations = listOf(
        ChannelChat(
            id = 0L,
            name = "General Course Questions",
            unreadMessagesCount = 8,
        ),
        conversation,
        GroupChat(
            id = 2L,
            name = "Team Rocket Students"
        ),
        OneToOneChat(
            id = 3L,
            members = listOf(
                tutor1
            )
        ),
        OneToOneChat(
            id = 4L,
            members = listOf(
                student2
            ),
            unreadMessagesCount = 3,
        )
    )

    private val firstMessageTime = Clock.System.now() - 20.minutes

    val posts = listOf(
        ChatListItem.DateDivider(firstMessageTime.toLocalDateTime(TimeZone.currentSystemDefault()).date),
        generateMessage(
            author = student1,
            text = "Hey, folks! What are the big advantages of solid chemical propellants in rockets?",
            time = firstMessageTime,
            id = "1",
        ),
        generateMessage(
            author = student2,
            text = "Hey, Sam! Solid propellants are known for their simplicity and reliability. They're easy to handle.",
            time = firstMessageTime + 3.minutes,
            id = "2",
        ),
        generateMessage(
            author = tutor1,
            text = "That's right, Mia. They have a consistent burn rate and a good thrust-to-weight ratio, which makes them handy for various missions.",
            time = firstMessageTime + 12.minutes,
            id = "3",
        ),
        generateMessage(
            author = student1,
            text = " Thanks, Mia and Ethan! So, they're like the dependable workhorses of rocket propellants, huh?",
            time = firstMessageTime + 15.minutes,
            id = "4",
        ),
    ).reversed()

    private fun generateMessage(
        author: ConversationUser = student1,
        text: String,
        time: Instant,
        id: String,
        reactions: List<PostPojo.Reaction> = emptyList()
    ): ChatListItem.PostItem {
        return ChatListItem.PostItem.IndexedItem.Post(
            PostPojo(
                clientPostId = id,
                serverPostId = 0L,
                title = null,
                content = text,
                authorName = author.firstName,
                authorRole = author.getUserRole(),
                authorId = author.id,
                authorImageUrl = author.imageUrl,
                creationDate = time,
                updatedDate = null,
                resolved = false,
                isSaved = false,
                courseWideContext = null,
                tags = emptyList(),
                answers = emptyList(),
                reactions = reactions,
                displayPriority = null,
                hasForwardedMessages = false
            ),
            emptyList()
        )
    }

}