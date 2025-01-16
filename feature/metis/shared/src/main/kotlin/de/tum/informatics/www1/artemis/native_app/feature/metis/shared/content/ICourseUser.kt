package de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content

import de.tum.informatics.www1.artemis.native_app.core.model.account.BaseAccount
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.UserRole

interface ICourseUser : BaseAccount {
    val isInstructor: Boolean
    val isEditor: Boolean
    val isTeachingAssistant: Boolean
    val isStudent: Boolean

    fun getUserRole(): UserRole {
        return when {
            isInstructor -> UserRole.INSTRUCTOR
            isTeachingAssistant || isEditor -> UserRole.TUTOR
            else -> UserRole.USER
        }
    }
}