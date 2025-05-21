package de.tum.informatics.www1.artemis.native_app.feature.lectureview

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.ui.PlayStoreScreenshots
import de.tum.informatics.www1.artemis.native_app.core.ui.ScreenshotData
import de.tum.informatics.www1.artemis.native_app.core.ui.ScreenshotFrame
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.ChannelChat
import kotlinx.coroutines.CompletableDeferred

@PlayStoreScreenshots
@Composable
fun `Lecture - Overview`() {
    ScreenshotFrame(title = "... and directly interact with your lectures within the app") {
        LectureScreen(
            modifier = Modifier.fillMaxSize(),
            courseId = 1,
            serverUrl = "serverUrl",
            lectureDataState = DataState.Success(
                data = ScreenshotData.lecture,
            ),
            lectureChannel = DataState.Success(
                data = ChannelChat(
                    id = 1,
                    name = "lecture-07-rocket-fuel"
                )
            ),
            lectureUnits = ScreenshotData.lecture.lectureUnits,
            onViewExercise = {},
            onReloadLecture = {},
            onUpdateLectureUnitIsComplete = { _, _ -> CompletableDeferred(true) },
            onSidebarToggle = {}
        )
    }
}