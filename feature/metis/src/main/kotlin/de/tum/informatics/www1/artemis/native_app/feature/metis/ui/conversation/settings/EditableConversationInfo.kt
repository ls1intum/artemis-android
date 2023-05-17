package de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.settings

data class EditableConversationInfo(
    val name: String,
    val description: String,
    val topic: String,
    val updateName: (String) -> Unit,
    val updateDescription: (String) -> Unit,
    val updateTopic: (String) -> Unit,
    val canEditName: Boolean,
    val canEditDescription: Boolean,
    val canEditTopic: Boolean,
    val isDirty: Boolean,
    val isSavingChanges: Boolean,
    val onRequestSaveChanges: () -> Unit
)
