package de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content

import android.os.Parcelable
import de.tum.informatics.www1.artemis.native_app.core.model.account.BaseAccount
import kotlinx.parcelize.Parcelize

@Parcelize
sealed class UserIdentifier : Parcelable {

    abstract fun matches(account: BaseAccount): Boolean

    data class Username(
        val username: String
    ) : UserIdentifier() {
        override fun matches(account: BaseAccount): Boolean {
            return account.username == username
        }
    }

    data class UserId(
        val userId: Long
    ) : UserIdentifier() {
        override fun matches(account: BaseAccount): Boolean {
            return account.id == userId
        }
    }
}