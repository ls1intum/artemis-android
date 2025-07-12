package de.tum.informatics.www1.artemis.native_app.feature.push.notification_model

/**
 * Utility class to extract information from the courseNotificationDTO structure
 * and convert it to the notification placeholder format for compatibility with existing logic.
 */
object CourseNotificationDTOExtractor {

    /**
     * Extracts notification placeholders from courseNotificationDTO parameters
     * to maintain compatibility with existing notification handling logic.
     */
    fun extractNotificationPlaceholders(courseNotificationDTO: CourseNotificationDTO): List<String> {
        return when (courseNotificationDTO.notificationType) {
            // Communication
            StandalonePostCommunicationNotificationType.NEW_POST_NOTIFICATION -> extractNewPostPlaceholders(courseNotificationDTO.parameters)
            StandalonePostCommunicationNotificationType.NEW_ANNOUNCEMENT_NOTIFICATION -> extractNewAnnouncementPlaceholders(courseNotificationDTO.parameters)
            StandalonePostCommunicationNotificationType.ADDED_TO_CHANNEL_NOTIFICATION -> extractChannelManagementPlaceholders(courseNotificationDTO.parameters)
            StandalonePostCommunicationNotificationType.REMOVED_FROM_CHANNEL_NOTIFICATION -> extractChannelManagementPlaceholders(courseNotificationDTO.parameters)
            StandalonePostCommunicationNotificationType.CHANNEL_DELETED_NOTIFICATION -> extractChannelDeletedPlaceholders(courseNotificationDTO.parameters)
            ReplyPostCommunicationNotificationType.NEW_ANSWER_NOTIFICATION -> extractNewAnswerPlaceholders(courseNotificationDTO.parameters)
            ReplyPostCommunicationNotificationType.NEW_MENTION_NOTIFICATION -> extractNewMentionPlaceholders(courseNotificationDTO.parameters)

            // Misc
            GeneralNotificationType.NEW_EXERCISE_NOTIFICATION -> extractNewExercisePlaceholders(courseNotificationDTO.parameters)
            GeneralNotificationType.EXERCISE_OPEN_FOR_PRACTICE_NOTIFICATION -> extractExerciseOpenForPracticePlaceholders(courseNotificationDTO.parameters)
            GeneralNotificationType.EXERCISE_ASSESSED_NOTIFICATION -> extractExerciseAssessedPlaceholders(courseNotificationDTO.parameters)
            GeneralNotificationType.EXERCISE_UPDATED_NOTIFICATION -> extractExerciseUpdatedPlaceholders(courseNotificationDTO.parameters)
            GeneralNotificationType.QUIZ_EXERCISE_STARTED_NOTIFICATION -> extractQuizExerciseStartedPlaceholders(courseNotificationDTO.parameters)
            GeneralNotificationType.ATTACHMENT_CHANGED_NOTIFICATION -> extractAttachmentChangedPlaceholders(courseNotificationDTO.parameters)
            GeneralNotificationType.NEW_MANUAL_FEEDBACK_REQUEST_NOTIFICATION -> extractNewManualFeedbackRequestPlaceholders(courseNotificationDTO.parameters)
            GeneralNotificationType.DUPLICATE_TEST_CASE_NOTIFICATION -> extractDuplicateTestCasePlaceholders(courseNotificationDTO.parameters)
            GeneralNotificationType.NEW_CPC_PLAGIARISM_CASE_NOTIFICATION -> extractNewPlagiarismCasePlaceholders(courseNotificationDTO.parameters)
            GeneralNotificationType.NEW_PLAGIARISM_CASE_NOTIFICATION -> extractNewPlagiarismCasePlaceholders(courseNotificationDTO.parameters)
            GeneralNotificationType.PROGRAMMING_BUILD_RUN_UPDATE_NOTIFICATION -> extractProgrammingTestCasesChangedPlaceholders(courseNotificationDTO.parameters)
            GeneralNotificationType.PROGRAMMING_TEST_CASES_CHANGED_NOTIFICATION -> extractProgrammingTestCasesChangedPlaceholders(courseNotificationDTO.parameters)
            GeneralNotificationType.PLAGIARISM_CASE_VERDICT_NOTIFICATION -> extractPlagiarismCaseVerdictPlaceholders(courseNotificationDTO.parameters)
            GeneralNotificationType.TUTORIAL_GROUP_ASSIGNED_NOTIFICATION -> extractTutorialGroupAssignedPlaceholders(courseNotificationDTO.parameters)
            GeneralNotificationType.TUTORIAL_GROUP_UNASSIGNED_NOTIFICATION -> extractTutorialGroupUnassignedPlaceholders(courseNotificationDTO.parameters)
            GeneralNotificationType.REGISTERED_TO_TUTORIAL_GROUP_NOTIFICATION -> extractRegisteredToTutorialGroupPlaceholders(courseNotificationDTO.parameters)
            GeneralNotificationType.DEREGISTERED_FROM_TUTORIAL_GROUP_NOTIFICATION -> extractDeregisteredFromTutorialGroupPlaceholders(courseNotificationDTO.parameters)
            GeneralNotificationType.TUTORIAL_GROUP_DELETED_NOTIFICATION -> extractTutorialGroupDeletedPlaceholders(courseNotificationDTO.parameters)
            // Unknown fallback
            else -> emptyList()
        }
    }


    private fun extractNewPostPlaceholders(params: CourseNotificationParameters): List<String> {
        return listOf(
            params.courseTitle ?: "",
            params.postMarkdownContent ?: "",
            params.postCreationDate ?: "",
            params.channelName ?: "",
            params.authorName ?: "",
            params.channelType ?: "",
            params.authorImageUrl ?: "",
            params.authorId?.toString() ?: "0",
            params.postId?.toString() ?: "0"
        )
    }
    
    private fun extractNewAnnouncementPlaceholders(params: CourseNotificationParameters): List<String> {
        return listOf(
            params.courseTitle ?: "",
            params.postTitle ?: "",
            params.postMarkdownContent ?: "",
            params.postCreationDate ?: "",
            params.authorName ?: "",
            params.authorImageUrl ?: "",
            params.authorId?.toString() ?: "0",
            params.postId?.toString() ?: "0"
        )
    }
    
    private fun extractNewAnswerPlaceholders(params: CourseNotificationParameters): List<String> {
        return listOf(
            params.courseTitle ?: "",
            params.postMarkdownContent ?: "",
            params.postCreationDate ?: "",
            params.postAuthorName ?: "",
            params.replyMarkdownContent ?: "",
            params.replyCreationDate ?: "",
            params.replyAuthorName ?: "",
            params.channelName ?: "",
            params.replyImageUrl ?: "",
            params.replyAuthorId?.toString() ?: "",
            params.postId?.toString() ?: "",
            params.replyId?.toString() ?: params.postId?.toString() ?: "0" // Use postId as fallback for replyId
        )
    }
    
    private fun extractNewMentionPlaceholders(params: CourseNotificationParameters): List<String> {
        return listOf(
            params.courseTitle ?: "",
            params.postMarkdownContent ?: "",
            params.postCreationDate ?: "",
            params.postAuthorName ?: "",
            params.replyMarkdownContent ?: "",
            params.replyCreationDate ?: "",
            params.replyAuthorName ?: "",
            params.channelName ?: "",
            params.replyImageUrl ?: "",
            params.replyAuthorId?.toString() ?: "",
            params.postId?.toString() ?: "",
            params.replyId?.toString() ?: params.postId?.toString() ?: "0" // Use postId as fallback for replyId
        )
    }
    
    private fun extractExerciseAssessedPlaceholders(params: CourseNotificationParameters): List<String> {
        return listOf(
            params.courseTitle ?: "",
            params.exerciseType ?: "",
            params.exerciseTitle ?: "",
            params.numberOfPoints?.toString() ?: "0",
            params.score?.toString() ?: "0"
        )
    }
    
    private fun extractNewExercisePlaceholders(params: CourseNotificationParameters): List<String> {
        return listOf(
            params.courseTitle ?: "",
            params.exerciseTitle ?: "",
            params.difficulty ?: "",
            params.releaseDate ?: "",
            params.dueDate ?: "",
            params.numberOfPoints?.toString() ?: "0"
        )
    }
    
    private fun extractExerciseOpenForPracticePlaceholders(params: CourseNotificationParameters): List<String> {
        return listOf(
            params.courseTitle ?: "",
            params.exerciseTitle ?: ""
        )
    }
    
    private fun extractExerciseUpdatedPlaceholders(params: CourseNotificationParameters): List<String> {
        return listOf(
            params.courseTitle ?: "",
            params.exerciseTitle ?: ""
        )
    }
    
    private fun extractQuizExerciseStartedPlaceholders(params: CourseNotificationParameters): List<String> {
        return listOf(
            params.courseTitle ?: "",
            params.exerciseTitle ?: ""
        )
    }
    
    private fun extractAttachmentChangedPlaceholders(params: CourseNotificationParameters): List<String> {
        return listOf(
            params.courseTitle ?: "",
            params.attachmentName ?: "",
            params.exerciseOrLectureName ?: ""
        )
    }
    
    private fun extractTutorialGroupAssignedPlaceholders(params: CourseNotificationParameters): List<String> {
        return listOf(
            params.courseTitle ?: "",
            params.groupTitle ?: "",
            params.moderatorName ?: ""
        )
    }
    
    private fun extractTutorialGroupUnassignedPlaceholders(params: CourseNotificationParameters): List<String> {
        return listOf(
            params.courseTitle ?: "",
            params.groupTitle ?: "",
            params.moderatorName ?: ""
        )
    }
    
    private fun extractRegisteredToTutorialGroupPlaceholders(params: CourseNotificationParameters): List<String> {
        return listOf(
            params.courseTitle ?: "",
            params.groupTitle ?: "",
            params.moderatorName ?: ""
        )
    }
    
    private fun extractDeregisteredFromTutorialGroupPlaceholders(params: CourseNotificationParameters): List<String> {
        return listOf(
            params.courseTitle ?: "",
            params.groupTitle ?: "",
            params.moderatorName ?: ""
        )
    }
    
    private fun extractTutorialGroupDeletedPlaceholders(params: CourseNotificationParameters): List<String> {
        return listOf(
            params.courseTitle ?: "",
            params.groupTitle ?: ""
        )
    }

    private fun extractProgrammingTestCasesChangedPlaceholders(params: CourseNotificationParameters): List<String> {
        return listOf(
            params.courseTitle ?: "",
            params.exerciseTitle ?: ""
        )
    }


    private fun extractDuplicateTestCasePlaceholders(params: CourseNotificationParameters): List<String> {
        return listOf(
            params.courseTitle ?: ""
        )
    }
    
    private fun extractNewPlagiarismCasePlaceholders(params: CourseNotificationParameters): List<String> {
        return listOf(
            params.courseTitle ?: "",
            params.exerciseTitle ?: ""
        )
    }
    
    private fun extractPlagiarismCaseVerdictPlaceholders(params: CourseNotificationParameters): List<String> {
        return listOf(
            params.courseTitle ?: "",
            params.exerciseTitle ?: ""
        )
    }
    
    private fun extractNewManualFeedbackRequestPlaceholders(params: CourseNotificationParameters): List<String> {
        return listOf(
            params.courseTitle ?: "",
            params.exerciseTitle ?: ""
        )
    }

    private fun extractChannelManagementPlaceholders(params: CourseNotificationParameters): List<String> {
        return listOf(
            params.courseTitle ?: "",
            params.channelName ?: "",
            params.channelModerator ?: "",
            params.authorImageUrl ?: "",
            params.authorId?.toString() ?: "0"
        )
    }

    private fun extractChannelDeletedPlaceholders(params: CourseNotificationParameters): List<String> {
        return listOf(
            params.courseTitle ?: "",
            params.channelName ?: "",
            params.deletingUser ?: "",
            params.authorImageUrl ?: "",
            params.authorId?.toString() ?: "0"
        )
    }
    
    /**
     * Gets the notification type string from courseNotificationDTO
     */
    fun getNotificationType(courseNotificationDTO: CourseNotificationDTO): NotificationType {
        return courseNotificationDTO.notificationType
    }
} 