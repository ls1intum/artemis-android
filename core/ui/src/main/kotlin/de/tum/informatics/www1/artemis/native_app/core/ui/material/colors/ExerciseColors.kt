package de.tum.informatics.www1.artemis.native_app.core.ui.material.colors

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import de.tum.informatics.www1.artemis.native_app.core.ui.date.hasPassed
import kotlinx.datetime.Instant

object ExerciseColors {

    object Result {
        val success: Color
            get() = Color(0xFF4CAF50)
        val medium: Color
            get() = Color(0xFFFF8F07)
        val bad: Color
            get() = Color(0xffdc3545)
    }

    object Difficulty {
        val hard: Color
            @Composable get() = Color(0xffdc3545)
        val medium: Color
            @Composable get() = Color(0xffffc107)
        val easy: Color
            @Composable get() = Color(0xff28a745)
    }

    object Type {
        val bonus: Color
            get() = Color(0xffffc107)
        val notIncluded: Color
            get() = Color.Gray
    }

    object Category {
        val live: Color
            get() = Color(0xff28a745)
        val unknown: Color
            get() = Color(0xFFFFFFFF)
    }

    @Composable
    fun getDueDateColor(dueDate: Instant): Color =
        if (dueDate.hasPassed()) Color(0xffdc3545) else Color(0xff28a745)

    object ParticipationNotPossibleInfoMessageCardColors {
        val background: Color
            @Composable get() = if(isSystemInDarkTheme()) Color(0xFF062A30) else Color(0xFFD1ECF1)
        val border: Color
            @Composable get() = if(isSystemInDarkTheme()) Color(0xFF148EA1) else Color(0xFFA2DAE3)
        val text: Color
            @Composable get() = if(isSystemInDarkTheme()) Color(0xFF36CEE6) else Color(0xFF09414A)
    }
}