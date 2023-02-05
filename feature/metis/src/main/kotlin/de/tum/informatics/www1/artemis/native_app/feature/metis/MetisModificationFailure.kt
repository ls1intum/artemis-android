package de.tum.informatics.www1.artemis.native_app.feature.metis

enum class MetisModificationFailure(val messageRes: Int) {
    CREATE_REACTION(R.string.metis_modification_failure_dialog_message_create_reaction),
    DELETE_REACTION(R.string.metis_modification_failure_dialog_message_delete_reaction),
    CREATE_POST(R.string.metis_modification_failure_dialog_message_create_post),
    DELETE_POST(R.string.metis_modification_failure_dialog_message_delete_post)
}

sealed class MetisModificationResponse<T> {
    data class Failure<T>(val failure: MetisModificationFailure) : MetisModificationResponse<T>()

    data class Response<T>(val data: T) : MetisModificationResponse<T>()
}