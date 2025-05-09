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
        val notReleased: Color
            get() = Color(0xffffc107)
    }

    object Quiz {
        val submitButton: Color
            @Composable get() = if (isSystemInDarkTheme()) Color(0xff00bc8c) else Color(0xff28a745)

        val submitButtonText: Color
            @Composable get() = Color.White

        val dragItemBackground: Color
            @Composable get() = if (isSystemInDarkTheme()) Color.Black else Color.White

        val dragItemText: Color
            @Composable get() = if (isSystemInDarkTheme()) Color.White else Color.Black

        val dropTargetColorNotDragging: Color
            @Composable get() = Color.White

        val dropTargetColorDragging: Color
            @Composable get() = Color.Blue

        val dropTargetColorDropTarget: Color
            @Composable get() = Color.Green

         object Header {
             val dragAndDrop: Color
                 @Composable get() = if (isSystemInDarkTheme()) Color(0xFF296773) else Color(0xFF86ccd5)

             val multipleChoice: Color
                 @Composable get() = if (isSystemInDarkTheme()) Color(0xFF9d8a1e) else Color(0xFFeee066)

             val shortAnswer: Color
                 @Composable get() = if (isSystemInDarkTheme()) Color(0xFF703e20) else Color(0xFFd8956c)

             val selectedBorder: Color
                 @Composable get() = if (isSystemInDarkTheme()) Color.Green else Color.Green
         }
    }

    @Composable
    fun getDueDateColor(dueDate: Instant): Color =
        if (dueDate.hasPassed()) Color(0xffdc3545) else Color(0xff28a745)
}