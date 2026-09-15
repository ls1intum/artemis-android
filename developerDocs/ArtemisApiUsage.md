# Artemis API usage

Every Artemis REST endpoint this app calls, and how it was checked against the server.

The app has no server-version negotiation: each endpoint is a single hardcoded path. It therefore
requires an Artemis server of **version 10 or newer**, and a path that the server stops serving turns
into a silent feature failure rather than a build error. This page exists so that the next person
comparing the app against a new Artemis release does not have to rediscover the endpoint list.

## How to re-run the comparison

The server's route table is the ground truth, and it is not the same thing as a grep over the
controllers: a path can be assembled from constants, and a controller can carry several prefixes.
Take the resolved table from a booted server instead.

1. In an Artemis checkout, add a temporary JUnit test that extends `AbstractSpringIntegrationIndependentTest`
   and writes `applicationContext.getBean("requestMappingHandlerMapping", RequestMappingHandlerMapping.class).getHandlerMethods()`
   to a file, then run it with `./gradlew test --tests <name> -x webapp`. It needs Docker.
2. In this repository, list the call sites with `grep -rn "appendPathSegments" --include="*.kt" .`.
   Paths are built from the `Api` sealed class in
   `core/data/src/main/kotlin/.../core/data/service/Api.kt`, sometimes across several
   `appendPathSegments` calls in one `url { }` block, and sometimes through a helper such as
   `ConversationServiceImpl.performActionOnConversation`.
3. Compare the two. A path the server does not answer is the interesting case.

Deserialisation gaps do not show up this way, because `JsonProvider` sets `ignoreUnknownKeys = true`
and every model field has a default: a field the server stops sending silently becomes the default.
Those have to be found by diffing the Kotlin models against the server DTOs by hand.

## Endpoints

Paths are relative to the server URL. `{…}` marks a path variable.

### Account and authentication

| Method | Path | Called from |
|---|---|---|
| POST | `api/core/public/authenticate` | `LoginServiceImpl` |
| POST | `api/core/public/saml2` | `LoginServiceImpl` |
| GET | `api/core/public/login-options` | `LoginServiceImpl` |
| POST | `api/core/public/exchange-code` | `LoginServiceImpl` |
| POST | `api/core/public/register` | `RegisterServiceImpl` |
| GET | `api/core/public/account` | `AccountDataServiceImpl` |
| PUT | `api/account/profile-picture` | `ChangeProfilePictureServiceImpl` |
| DELETE | `api/account/profile-picture` | `ChangeProfilePictureServiceImpl` |
| GET | `api/account/passkeys/user` | `PasskeySettingsServiceImpl` |
| POST | `webauthn/authenticate/options` | `PasskeyLoginServiceImpl` |
| POST | `login/webauthn` | `PasskeyLoginServiceImpl` |
| POST | `webauthn/register/options` | `PasskeySettingsServiceImpl` |
| POST | `webauthn/register` | `PasskeySettingsServiceImpl` |
| DELETE | `webauthn/register/{credentialId}` | `PasskeySettingsServiceImpl` |

The `webauthn` and `login/webauthn` paths are served by Spring Security's filter chain, not by a
controller, so they do not appear in the controller route table.

### Server metadata

| Method | Path | Called from |
|---|---|---|
| GET | `management/info` | `ServerProfileInfoServiceImpl` |
| GET | `api/public/time` | `ServerTimeServiceImpl` |

`api/public/time` is served by a servlet container valve rather than by Spring, and answers
`text/plain`. It is read as a `String` and parsed; requesting it as JSON fails.

### Courses

| Method | Path | Called from |
|---|---|---|
| GET | `api/course/courses/for-dashboard` | `DashboardServiceImpl` |
| GET | `api/course/courses/{courseId}/for-overview` | `CourseServiceImpl.getCourse` |
| GET | `api/course/courses/{courseId}/available-tabs` | `CourseServiceImpl.getCourseWithContent` |
| GET | `api/course/courses/{courseId}/exercises-for-overview` | `CourseServiceImpl.getCourseWithContent` |
| GET | `api/lecture/courses/{courseId}/lectures-for-overview` | `CourseServiceImpl.getCourseWithContent` |
| GET | `api/course/courses/for-enrollment` | `CourseRegistrationServiceImpl` |
| POST | `api/course/courses/{courseId}/enroll` | `CourseRegistrationServiceImpl` |
| GET | `api/course/courses/{courseId}/users/search` | `ConversationServiceImpl` |

### Exercises and participation

| Method | Path | Called from |
|---|---|---|
| GET | `api/exercise/exercises/{exerciseId}/details` | `ExerciseServiceImpl` |
| POST | `api/exercise/exercises/{exerciseId}/participations` | `CourseExerciseServiceImpl` |
| GET | `api/programming/programming-exercise-participations/{participationId}/latest-pending-submission` | `LiveParticipationServiceImpl` |
| GET | `api/text/participations/{participationId}/text-editor` | `TextEditorServiceImpl` |
| PUT | `api/text/exercises/{exerciseId}/text-submissions` | `TextSubmissionServiceImpl` |

### Quizzes

| Method | Path | Called from |
|---|---|---|
| GET | `api/quiz/quiz-exercises/{exerciseId}/for-student` | `QuizExerciseServiceImpl` |
| POST | `api/quiz/quiz-exercises/{exerciseId}/join` | `QuizExerciseServiceImpl` |
| POST | `api/quiz/quiz-exercises/{exerciseId}/start-participation` | `ParticipationServiceImpl` |
| POST | `api/quiz/exercises/{exerciseId}/submissions/live` | `QuizParticipationServiceImpl` |
| POST | `api/quiz/exercises/{exerciseId}/submissions/practice` | `QuizParticipationServiceImpl` |

### Lectures

| Method | Path | Called from |
|---|---|---|
| GET | `api/lecture/lectures/{lectureId}/details` | `LectureServiceImpl` |
| POST | `api/lecture/lectures/{lectureId}/lecture-units/{lectureUnitId}/completion` | `LectureServiceImpl` |

### Communication

| Method | Path | Called from |
|---|---|---|
| GET | `api/communication/courses/{courseId}/conversations` | `ConversationServiceImpl` |
| PUT | `api/communication/courses/{courseId}/channels/{conversationId}` | `ConversationServiceImpl` |
| PUT | `api/communication/courses/{courseId}/group-chats/{conversationId}` | `ConversationServiceImpl` |
| POST | `api/communication/courses/{courseId}/channels` | `ConversationServiceImpl` |
| DELETE | `api/communication/courses/{courseId}/channels/{conversationId}` | `ConversationServiceImpl` |
| POST | `api/communication/courses/{courseId}/channels/{conversationId}/register` | `ConversationServiceImpl`, `ChannelServiceImpl` |
| POST | `api/communication/courses/{courseId}/channels/{conversationId}/deregister` | `ConversationServiceImpl` |
| POST | `api/communication/courses/{courseId}/channels/{conversationId}/archive` | `ConversationServiceImpl` |
| POST | `api/communication/courses/{courseId}/channels/{conversationId}/unarchive` | `ConversationServiceImpl` |
| POST | `api/communication/courses/{courseId}/channels/{conversationId}/grant-channel-moderator` | `ConversationServiceImpl` |
| POST | `api/communication/courses/{courseId}/channels/{conversationId}/revoke-channel-moderator` | `ConversationServiceImpl` |
| POST | `api/communication/courses/{courseId}/channels/{conversationId}/toggle-privacy` | `ConversationServiceImpl` |
| POST | `api/communication/courses/{courseId}/channels/mark-as-read` | `ConversationServiceImpl` |
| GET | `api/communication/courses/{courseId}/channels/overview` | `ChannelServiceImpl` |
| GET | `api/communication/courses/{courseId}/exercises/{exerciseId}/channel` | `ChannelServiceImpl` |
| GET | `api/communication/courses/{courseId}/lectures/{lectureId}/channel` | `ChannelServiceImpl` |
| POST | `api/communication/courses/{courseId}/group-chats` | `ConversationServiceImpl` |
| POST | `api/communication/courses/{courseId}/one-to-one-chats` | `ConversationServiceImpl` |
| POST | `api/communication/courses/{courseId}/conversations/{conversationId}/hidden` | `ConversationServiceImpl` |
| POST | `api/communication/courses/{courseId}/conversations/{conversationId}/favorite` | `ConversationServiceImpl` |
| POST | `api/communication/courses/{courseId}/conversations/{conversationId}/muted` | `ConversationServiceImpl` |
| PATCH | `api/communication/courses/{courseId}/conversations/{conversationId}/mark-as-read` | `ConversationServiceImpl` |
| GET | `api/communication/courses/{courseId}/conversations/{conversationId}/members/search` | `ConversationServiceImpl` |
| GET | `api/communication/courses/{courseId}/messages` | `MetisServiceImpl`, `ChannelServiceImpl` |
| POST | `api/communication/courses/{courseId}/messages` | `MetisModificationServiceImpl` |
| PUT | `api/communication/courses/{courseId}/messages/{messageId}` | `MetisModificationServiceImpl` |
| PUT | `api/communication/courses/{courseId}/messages/{messageId}/display-priority` | `MetisModificationServiceImpl` |
| DELETE | `api/communication/courses/{courseId}/messages/{messageId}` | `MetisModificationServiceImpl` |
| GET | `api/communication/courses/{courseId}/messages-source-posts` | `MetisServiceImpl` |
| POST | `api/communication/courses/{courseId}/answer-messages` | `MetisModificationServiceImpl` |
| PUT | `api/communication/courses/{courseId}/answer-messages/{answerMessageId}` | `MetisModificationServiceImpl` |
| DELETE | `api/communication/courses/{courseId}/answer-messages/{answerMessageId}` | `MetisModificationServiceImpl` |
| GET | `api/communication/courses/{courseId}/answer-messages-source-posts` | `MetisServiceImpl` |
| POST | `api/communication/courses/{courseId}/postings/reactions` | `MetisModificationServiceImpl` |
| DELETE | `api/communication/courses/{courseId}/postings/reactions/{reactionId}` | `MetisModificationServiceImpl` |
| GET | `api/communication/courses/{courseId}/code-of-conduct/agreement` | `CodeOfConductServiceImpl` |
| PATCH | `api/communication/courses/{courseId}/code-of-conduct/agreement` | `CodeOfConductServiceImpl` |
| GET | `api/communication/courses/{courseId}/code-of-conduct/responsible-users` | `CodeOfConductServiceImpl` |
| GET | `api/communication/forwarded-messages` | `MetisServiceImpl` |
| POST | `api/communication/forwarded-messages` | `MetisServiceImpl` |
| GET | `api/communication/link-preview` | `MetisServiceImpl` |
| GET | `api/communication/saved-posts` | `SavedPostServiceImpl` |
| POST | `api/communication/saved-posts/{postId}` | `SavedPostServiceImpl` |
| PUT | `api/communication/saved-posts/{postId}` | `SavedPostServiceImpl` |
| DELETE | `api/communication/saved-posts/{postId}` | `SavedPostServiceImpl` |

### FAQ

| Method | Path | Called from |
|---|---|---|
| GET | `api/communication/courses/{courseId}/faq-state/ACCEPTED` | `FaqRemoteServiceImpl` |
| GET | `api/communication/courses/{courseId}/faqs/{faqId}` | `FaqRemoteServiceImpl` |

### Notifications

| Method | Path | Called from |
|---|---|---|
| GET | `api/notification/courses/info` | `CourseNotificationSettingsServiceImpl` |
| GET | `api/notification/courses/{courseId}/settings` | `CourseNotificationSettingsServiceImpl` |
| PUT | `api/notification/courses/{courseId}/setting-preset` | `CourseNotificationSettingsServiceImpl` |
| PUT | `api/notification/courses/{courseId}/setting-specification` | `CourseNotificationSettingsServiceImpl` |
| POST | `api/notification/push_notification/register` | `NotificationSettingsServiceImpl` |
| DELETE | `api/notification/push_notification/unregister` | `NotificationSettingsServiceImpl` |

### Files

| Method | Path | Called from |
|---|---|---|
| GET | `api/core/files/templates/code-of-conduct` | `CodeOfConductServiceImpl` |
| POST | `api/core/files/courses/{courseId}/conversations/{conversationId}` | `MetisModificationServiceImpl` |
| GET | `api/core/files/{path}` | `ArtemisImageProviderImpl`, `AttachmentUtil` |

`api/core/files/{path}` is not a single endpoint: the app appends whatever relative path the server
sent for an image or attachment. Artemis keeps these spellings deliberately, because values written
into post markdown years ago still resolve to them.

## Known gaps

Found while comparing the models against the server, and not fixed here.

- **`Lecture.attachments` is always empty.** The server answers `lectures/{id}/details` with a
  `LectureDetailsDTO` that has no `attachments`; attachments reach the client as attachment-video
  lecture units instead. The attachments section of the lecture overview therefore never renders.
  The lecture unit list already shows the same files.
- **`CourseWithScore` is only meaningful on the dashboard.** The scores come from
  `courses/for-dashboard`. The single-course endpoints no longer carry them, and no screen outside
  the dashboard read them.
- **`SavedPost.authorRole` is always null.** `saved-posts` answers with a `PostingDTO`, which spells
  that field `role`; every other posting endpoint spells it `authorRole`. The inconsistency is on
  the server and predates Artemis 10, so it is recorded rather than worked around with a
  `@SerialName` that would break once the server is made consistent.
- **`SavedPost.hasForwardedMessages` is always null**, for the same reason: `PostingDTO` does not
  carry it, so the saved-posts list cannot show the forwarded-message indicator.

The models were compared field by field against the server DTOs for the course, account, exercise,
lecture, participation, submission, result, attachment, conversation, post, reaction, forwarded
message, saved post and passkey types. Everything not listed above matched.

## Endpoints that are no longer legacy aliases

Artemis serves, and will keep serving until at least 30 September 2026, a set of legacy path
prefixes (`api/core/admin/`, `api/core/account/`, `api/core/passkey/`, `api/communication/notification/`
and others) for clients that have not migrated. This app calls none of them.
