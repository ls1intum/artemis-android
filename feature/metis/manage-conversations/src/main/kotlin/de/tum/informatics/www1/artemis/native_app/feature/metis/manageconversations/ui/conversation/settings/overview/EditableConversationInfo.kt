package de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.settings.overview

data class EditableConversationInfo(
    val name: String,
    val description: String,
    val topic: String,
    val isNameIllegal: Boolean,
    val isDescriptionIllegal: Boolean,
    val isTopicIllegal: Boolean,
    val updateName: (String) -> Unit,
    val updateDescription: (String) -> Unit,
    val updateTopic: (String) -> Unit,
    val canEditName: Boolean,
    val canEditDescription: Boolean,
    val canEditTopic: Boolean,
    val canSave: Boolean,
    val isDirty: Boolean,
    val isSavingChanges: Boolean,
    val onRequestSaveChanges: () -> Unit
)
