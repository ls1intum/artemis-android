package de.tum.informatics.www1.artemis.native_app.feature.quiz_participation.screens.work.question

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.quiz.QuizQuestion
import de.tum.informatics.www1.artemis.native_app.core.ui.markdown.MarkdownText

/**
 * Display the question text as a markdown text.
 */
@Composable
internal fun QuizQuestionBodyText(modifier: Modifier, question: QuizQuestion) {
    QuizQuestionBodyText(modifier = modifier, text = question.text.orEmpty())
}

@Composable
private fun QuizQuestionBodyText(modifier: Modifier, text: String) {
    MarkdownText(
        modifier = modifier,
        markdown = text,
        style = MaterialTheme.typography.bodyMedium
    )
}

/**
 * Display the question instruction text
 */
@Composable
internal fun QuizQuestionInstructionText(modifier: Modifier, instructionText: String) {
    Text(
        modifier = modifier,
        text = instructionText,
        style = MaterialTheme.typography.bodyLarge,
        fontWeight = FontWeight.Bold
    )
}