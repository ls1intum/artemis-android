package de.tum.informatics.www1.artemis.native_app.feature.lectureview

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.model.lecture.Attachment
import de.tum.informatics.www1.artemis.native_app.core.model.lecture.Lecture
import de.tum.informatics.www1.artemis.native_app.core.model.lecture.lecture_units.LectureUnit
import de.tum.informatics.www1.artemis.native_app.core.ui.AwaitDeferredCompletion
import de.tum.informatics.www1.artemis.native_app.core.ui.Spacings
import de.tum.informatics.www1.artemis.native_app.core.ui.common.BasicDataStateUi
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.ChannelChat
import io.noties.markwon.LinkResolver
import kotlinx.coroutines.Deferred

@Composable
internal fun LectureScreenBody(
    modifier: Modifier,
    lectureDataState: DataState<Lecture>,
    lectureChannel: DataState<ChannelChat>,
    lectureUnits: List<LectureUnit>,
    onViewExercise: (exerciseId: Long) -> Unit,
    overviewListState: LazyListState,
    onRequestViewLink: (String) -> Unit,
    onRequestOpenAttachment: (Attachment) -> Unit,
    onDisplaySetCompletedFailureDialog: () -> Unit,
    onReloadLecture: () -> Unit,
    onUpdateLectureUnitIsComplete: (lectureUnitId: Long, isCompleted: Boolean) -> Deferred<Boolean>,
    linkResolver: LinkResolver
) {
    val markLectureUnitDeferredMap = remember { SnapshotStateMap<Long, Deferred<Boolean>>() }

    markLectureUnitDeferredMap.forEach { (lectureUnitId, deferred) ->
        AwaitDeferredCompletion(job = deferred) { isSuccessful ->
            if (!isSuccessful) {
                onDisplaySetCompletedFailureDialog()
            }

            markLectureUnitDeferredMap -= lectureUnitId
        }
    }

    Column(modifier = modifier) {
        BasicDataStateUi(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            dataState = lectureDataState,
            loadingText = stringResource(id = R.string.lecture_view_lecture_loading),
            failureText = stringResource(id = R.string.lecture_view_lecture_loading_failure),
            retryButtonText = stringResource(id = R.string.lecture_view_lecture_loading_try_again),
            onClickRetry = onReloadLecture,
        ) { lecture ->

            val lectureUnitsWithData by remember(lectureUnits, markLectureUnitDeferredMap) {
                derivedStateOf {
                    lectureUnits.map {
                        LectureUnitData(it, markLectureUnitDeferredMap[it.id] != null)
                    }
                }
            }

            LectureOverviewTab(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = Spacings.ScreenHorizontalSpacing),
                lecture = lecture,
                lectureChannel = lectureChannel,
                lectureUnits = lectureUnitsWithData,
                attachments = lecture.attachments,
                onViewExercise = onViewExercise,
                onMarkAsCompleted = { lectureUnitId, isCompleted ->
                    val deferred = onUpdateLectureUnitIsComplete(
                        lectureUnitId,
                        isCompleted
                    )

                    markLectureUnitDeferredMap[lectureUnitId] = deferred
                },
                onRequestViewLink = onRequestViewLink,
                onRequestOpenAttachment = onRequestOpenAttachment,
                linkResolver = linkResolver,
                state = overviewListState
            )

        }
    }
}
