package de.tum.informatics.www1.artemis.native_app.feature.push.notification_model

import androidx.annotation.StringRes
import de.tum.informatics.www1.artemis.native_app.feature.push.R
import kotlinx.serialization.Serializable

@Serializable
sealed interface NotificationType

@Serializable
sealed interface CommunicationNotificationType : NotificationType

@Serializable
enum class StandalonePostCommunicationNotificationType : CommunicationNotificationType {
    NEW_EXERCISE_POST,
    NEW_LECTURE_POST,
    NEW_COURSE_POST,
    NEW_ANNOUNCEMENT_POST,
    CONVERSATION_NEW_MESSAGE
}

@Serializable
enum class ReplyPostCommunicationNotificationType : CommunicationNotificationType {
    NEW_REPLY_FOR_EXERCISE_POST,
    NEW_REPLY_FOR_LECTURE_POST,
    NEW_REPLY_FOR_COURSE_POST,
    CONVERSATION_NEW_REPLY_MESSAGE
}

@Serializable
enum class MiscNotificationType(@StringRes val title: Int, @StringRes val body: Int) : NotificationType {
    EXERCISE_SUBMISSION_ASSESSED(R.string.push_notification_title_exerciseSubmissionAssessed, R.string.push_notification_text_exerciseSubmissionAssessed),
    ATTACHMENT_CHANGE(R.string.push_notification_title_attachmentChange, R.string.push_notification_text_attachmentChange),
    EXERCISE_RELEASED(R.string.push_notification_title_exerciseReleased, R.string.push_notification_text_exerciseReleased),
    EXERCISE_PRACTICE(R.string.push_notification_title_exercisePractice, R.string.push_notification_text_exercisePractice),
    QUIZ_EXERCISE_STARTED(R.string.push_notification_title_quizExerciseStarted, R.string.push_notification_text_quizExerciseStarted),
    EXERCISE_UPDATED(R.string.push_notification_title_exerciseUpdated, R.string.push_notification_text_exerciseUpdated),
    FILE_SUBMISSION_SUCCESSFUL(R.string.push_notification_title_fileSubmissionSuccessful, R.string.push_notification_text_fileSubmissionSuccessful),
    COURSE_ARCHIVE_STARTED(R.string.push_notification_title_courseArchiveStarted, R.string.push_notification_text_courseArchiveStarted),
    COURSE_ARCHIVE_FINISHED_WITH_ERRORS(R.string.push_notification_title_courseArchiveFinished, R.string.push_notification_text_courseArchiveFinishedWithErrors),
    COURSE_ARCHIVE_FINISHED_WITHOUT_ERRORS(R.string.push_notification_title_courseArchiveFinished, R.string.push_notification_text_courseArchiveFinishedWithoutErrors),
    COURSE_ARCHIVE_FAILED(R.string.push_notification_title_courseArchiveFailed, R.string.push_notification_text_courseArchiveFailed),
    PROGRAMMING_TEST_CASES_CHANGED(R.string.push_notification_title_programmingTestCasesChanged, R.string.push_notification_text_programmingTestCasesChanged),
    DUPLICATE_TEST_CASE(R.string.push_notification_title_duplicateTestCase, R.string.push_notification_text_duplicateTestCase),
    EXAM_ARCHIVE_STARTED(R.string.push_notification_title_examArchiveStarted, R.string.push_notification_text_examArchiveStarted),
    EXAM_ARCHIVE_FINISHED_WITH_ERRORS(R.string.push_notification_title_examArchiveFinished, R.string.push_notification_text_examArchiveFinishedWithErrors),
    EXAM_ARCHIVE_FINISHED_WITHOUT_ERRORS(R.string.push_notification_title_examArchiveFinished, R.string.push_notification_text_examArchiveFinishedWithoutErrors),
    EXAM_ARCHIVE_FAILED(R.string.push_notification_title_examArchiveFailed, R.string.push_notification_text_examArchiveFailed),
    ILLEGAL_SUBMISSION(R.string.push_notification_title_illegalSubmission, R.string.push_notification_text_illegalSubmission),
    NEW_PLAGIARISM_CASE_STUDENT(R.string.push_notification_title_newPlagiarismCaseStudent, R.string.push_notification_text_newPlagiarismCaseStudent),
    PLAGIARISM_CASE_VERDICT_STUDENT(R.string.push_notification_title_plagiarismCaseVerdictStudent, R.string.push_notification_text_plagiarismCaseVerdictStudent),
    NEW_MANUAL_FEEDBACK_REQUEST(R.string.push_notification_title_newManualFeedbackRequest, R.string.push_notification_text_newManualFeedbackRequest),
    TUTORIAL_GROUP_REGISTRATION_STUDENT(R.string.push_notification_title_tutorialGroupRegistrationStudent, R.string.push_notification_text_tutorialGroupRegistrationStudent),
    TUTORIAL_GROUP_DEREGISTRATION_STUDENT(R.string.push_notification_title_tutorialGroupDeregistrationStudent, R.string.push_notification_text_tutorialGroupDeregistrationStudent),
    TUTORIAL_GROUP_REGISTRATION_TUTOR(R.string.push_notification_title_tutorialGroupRegistrationTutor, R.string.push_notification_text_tutorialGroupRegistrationTutor),
    TUTORIAL_GROUP_MULTIPLE_REGISTRATION_TUTOR(R.string.push_notification_title_tutorialGroupMultipleRegistrationTutor, R.string.push_notification_text_tutorialGroupMultipleRegistrationTutor),
    TUTORIAL_GROUP_DEREGISTRATION_TUTOR(R.string.push_notification_title_tutorialGroupDeregistrationTutor, R.string.push_notification_text_tutorialGroupDeregistrationTutor),
    TUTORIAL_GROUP_DELETED(R.string.push_notification_title_tutorialGroupDeleted, R.string.push_notification_text_tutorialGroupDeleted),
    TUTORIAL_GROUP_UPDATED(R.string.push_notification_title_tutorialGroupUpdated, R.string.push_notification_text_tutorialGroupUpdated),
    TUTORIAL_GROUP_ASSIGNED(R.string.push_notification_title_tutorialGroupAssigned, R.string.push_notification_text_tutorialGroupAssigned),
    TUTORIAL_GROUP_UNASSIGNED(R.string.push_notification_title_tutorialGroupUnassigned, R.string.push_notification_text_tutorialGroupUnassigned),
}

@Serializable
data object UnknownNotificationType : NotificationType