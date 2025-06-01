package de.tum.informatics.www1.artemis.native_app.feature.lectureview

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.TabRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.model.lecture.Attachment
import de.tum.informatics.www1.artemis.native_app.core.model.lecture.Lecture
import de.tum.informatics.www1.artemis.native_app.core.model.lecture.lecture_units.LectureUnit
import de.tum.informatics.www1.artemis.native_app.core.ui.AwaitDeferredCompletion
import de.tum.informatics.www1.artemis.native_app.core.ui.Spacings
import de.tum.informatics.www1.artemis.native_app.core.ui.common.BasicDataStateUi
import de.tum.informatics.www1.artemis.native_app.core.ui.material.DefaultTab
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.ChannelChat
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
) {
    val selectedTabIndexState = rememberSaveable {
        mutableIntStateOf(0)
    }

    val overviewTabIndex = 0
    val attachmentsTabIndex = 1

    val selectedTabIndex = selectedTabIndexState.intValue

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
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shadowElevation = Spacings.AppBarElevation
        ){
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.background,
                divider = {},
            ) {
                DefaultTab(
                    index = overviewTabIndex,
                    iconPainter = painterResource(id = de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.R.drawable.list),
                    textRes = R.string.lecture_view_tab_overview,
                    selectedTabIndex = selectedTabIndexState
                )

                DefaultTab(
                    index = attachmentsTabIndex,
                    iconPainter = painterResource(id = de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.R.drawable.attachment),
                    textRes = R.string.lecture_view_tab_attachments,
                    selectedTabIndex = selectedTabIndexState
                )
            }
        }

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
            when (selectedTabIndex) {
                overviewTabIndex -> {
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
                        state = overviewListState
                    )
                }

                attachmentsTabIndex -> {
                    AttachmentsTab(
                        modifier = Modifier.fillMaxSize(),
                        attachments = lecture.attachments,
                        onClickFileAttachment = onRequestOpenAttachment,
                        onClickOpenLinkAttachment = {
                            onRequestViewLink(
                                it.link ?: return@AttachmentsTab
                            )
                        }
                    )
                }
            }
        }
    }
}
