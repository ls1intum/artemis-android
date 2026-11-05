package de.tum.informatics.www1.artemis.native_app.feature.lectureview

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.listSaver
import de.tum.informatics.www1.artemis.native_app.core.model.lecture.Attachment
import kotlinx.serialization.json.Json
import java.security.MessageDigest

internal data class PendingLectureAttachment(val id: Int, val page: Int?, val requiresSnapshot: Boolean, val revision: String?)

/** Pending IDs/pages survive recreation; document URLs and session credentials do not enter saved state. */
internal class LectureAttachmentDispatch(initial: PendingLectureAttachment? = null) {
    var pending by mutableStateOf(initial)
        private set
    fun openTarget(target: Attachment, validated: Attachment?, page: Int?) {
        pending = PendingLectureAttachment((validated ?: target).id, page, validated != null, validated?.let { revision(it, page) })
    }
    fun resolve(validated: Attachment?, page: Int?, candidates: List<Attachment>): Attachment? {
        val request = pending ?: return null
        return if (request.requiresSnapshot) validated?.takeIf { it.id == request.id && revision(it, page) == request.revision }
        else candidates.firstOrNull { it.id == request.id }
    }
    fun clear() { pending = null }
    companion object {
        private fun revision(attachment: Attachment, page: Int?) = MessageDigest.getInstance("SHA-256")
            .digest((Json.encodeToString(Attachment.serializer(), attachment) + "|$page").toByteArray())
            .joinToString("") { "%02x".format(it) }
        val Saver = listSaver<LectureAttachmentDispatch, Any>(
            save = { listOf(it.pending?.id ?: -1, it.pending?.page ?: -1, it.pending?.requiresSnapshot ?: false, it.pending?.revision.orEmpty()) },
            restore = { values -> LectureAttachmentDispatch(if (values[0] == -1) null else PendingLectureAttachment(values[0] as Int, (values[1] as Int).takeIf { it >= 0 }, values[2] as Boolean, (values[3] as String).takeIf { it.isNotEmpty() })) }
        )
    }
}
