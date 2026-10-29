package de.tum.informatics.www1.artemis.native_app.feature.quiz

import de.tum.informatics.www1.artemis.native_app.core.model.exercise.participation.Participation
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.QuizSubmission
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.Result

/**
 * The answers of a quiz participation. Artemis sends them as the only submission of the participation,
 * like the web app reads them, and leaves the results of the participation itself empty.
 */
internal val Participation.quizSubmission: QuizSubmission?
    get() = submissions.orEmpty().firstOrNull() as? QuizSubmission

/**
 * The result of a quiz participation, which Artemis attaches to its submission once the quiz has been evaluated.
 */
internal val Participation.quizResult: Result?
    get() = quizSubmission?.results.orEmpty().firstOrNull()
