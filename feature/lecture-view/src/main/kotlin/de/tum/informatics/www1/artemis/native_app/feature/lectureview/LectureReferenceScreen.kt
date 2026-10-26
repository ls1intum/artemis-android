package de.tum.informatics.www1.artemis.native_app.feature.lectureview

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.toRoute
import de.tum.informatics.www1.artemis.native_app.core.common.artemis_context.ArtemisContext
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.model.lecture.Lecture
import de.tum.informatics.www1.artemis.native_app.core.model.lecture.lecture_units.LectureUnitAttachmentVideo
import de.tum.informatics.www1.artemis.native_app.core.ui.LocalArtemisContextProvider
import de.tum.informatics.www1.artemis.native_app.core.ui.collectArtemisContextAsState
import de.tum.informatics.www1.artemis.native_app.core.ui.compose.NavigationBackButton
import de.tum.informatics.www1.artemis.native_app.core.ui.deeplinks.LectureDeeplinks
import de.tum.informatics.www1.artemis.native_app.core.ui.navigation.animatedComposable
import de.tum.informatics.www1.artemis.native_app.feature.lectureview.service.LectureService
import kotlinx.coroutines.ensureActive
import kotlinx.serialization.Serializable
import org.koin.compose.koinInject
import de.tum.informatics.www1.artemis.native_app.core.ui.R as CoreR

@Serializable
private data class LectureReferenceRoute(val courseId: Long, val referenceType: String, val reference: String)

internal fun NavGraphBuilder.lectureMediaReferences(onViewExercise: (Long) -> Unit) {
    animatedComposable<LectureReferenceRoute>(deepLinks = LectureDeeplinks.ToLectureMediaReference.generateLinks()) { entry ->
        val route = entry.toRoute<LectureReferenceRoute>()
        val session by LocalArtemisContextProvider.current.collectArtemisContextAsState()
        // A changed session destroys the old result and cancels its producer before showing another account.
        key(session.serverUrl, (session as? ArtemisContext.LoggedIn)?.loginName, (session as? ArtemisContext.LoggedIn)?.authToken) { LectureReferenceContent(route, session, onViewExercise) }
    }
}

@Composable
private fun LectureReferenceContent(route: LectureReferenceRoute, session: ArtemisContext, onViewExercise: (Long) -> Unit) {
    val service = koinInject<LectureService>()
    var attempt by remember { mutableIntStateOf(0) }
    val state by produceState<DataState<ResolvedLectureReference>>(DataState.Loading(), route, attempt) {
        value = DataState.Loading()
        val loggedIn = session as? ArtemisContext.LoggedIn
        if (loggedIn == null) { value = DataState.Failure(IllegalStateException("Sign in to open lecture media")); return@produceState }
        value = loadLectureReference(service, loggedIn, route.courseId, route.referenceType, route.reference).map(
            { DataState.Success(it) }, { DataState.Failure(it) }
        )
    }
    when (val current = state) {
        is DataState.Success -> LectureDetailContent(
            lectureId = current.data.lectureId,
            onViewExercise = onViewExercise,
            initialUnitId = current.data.unitId,
            initialAttachmentId = current.data.attachmentId,
            initialPage = current.data.page,
            initialResolvedAttachment = current.data.document,
            onRetryMediaReference = { attempt++ },
            displayedCourseId = route.courseId
        )
        else -> Scaffold(topBar = { Row { NavigationBackButton() } }) { padding ->
            Column(Modifier.fillMaxSize().padding(padding).padding(24.dp), verticalArrangement = Arrangement.Center) {
                if (current is DataState.Loading) {
                    CircularProgressIndicator()
                    Text(stringResource(CoreR.string.lecture_media_loading))
                } else {
                    Text(stringResource(CoreR.string.lecture_media_missing))
                    Text(stringResource(CoreR.string.lecture_media_access_recovery))
                    Button(onClick = { attempt++ }) { Text(stringResource(CoreR.string.lecture_media_retry)) }
                }
            }
        }
    }
}

internal suspend fun loadLectureReference(
    service: LectureService,
    session: ArtemisContext.LoggedIn,
    courseId: Long,
    type: String,
    reference: String
): NetworkResponse<ResolvedLectureReference> {
    val lectureId = referenceLectureId(reference)
    val response: NetworkResponse<List<Lecture>> = if (type == "attachment" && lectureId != null) {
        service.loadLecture(lectureId, session.serverUrl, session.authToken).bind { listOf(it) }
    } else {
        service.loadCourseLecturesWithSlides(courseId, session.serverUrl, session.authToken)
    }
    kotlin.coroutines.coroutineContext.ensureActive()
    if (response is NetworkResponse.Failure) return NetworkResponse.Failure(response.exception)
    val metadata = (response as NetworkResponse.Response).data
    val target = resolveLectureReference(metadata, type, reference)
        ?: return NetworkResponse.Failure(NoSuchElementException("Lecture resource unavailable"))
    if (type != "slide") return NetworkResponse.Response(target)
    return loadConsistentSlideAccess(service, session, courseId, reference, target)
}

/** Visible slide IDs/numbers and the selected PDF must describe the same attachment revision. */
private suspend fun loadConsistentSlideAccess(
    service: LectureService,
    session: ArtemisContext.LoggedIn,
    courseId: Long,
    reference: String,
    target: ResolvedLectureReference
): NetworkResponse<ResolvedLectureReference> {
    suspend fun details() = service.loadLecture(target.lectureId, session.serverUrl, session.authToken)
    fun unit(lecture: Lecture) = lecture.lectureUnits.filterIsInstance<LectureUnitAttachmentVideo>().firstOrNull { it.id == target.unitId }
    val before = details()
    if (before is NetworkResponse.Failure) return NetworkResponse.Failure(before.exception)
    val beforeAttachment = unit((before as NetworkResponse.Response).data)?.attachment
        ?: return NetworkResponse.Failure(NoSuchElementException("Lecture attachment unavailable"))
    val fresh = service.loadCourseLecturesWithSlides(courseId, session.serverUrl, session.authToken)
    if (fresh is NetworkResponse.Failure) return NetworkResponse.Failure(fresh.exception)
    val freshLectures = (fresh as NetworkResponse.Response).data
    // Resolve the original modern ID again; never retain only its old page/index.
    val freshTarget = resolveLectureReference(freshLectures, "slide", reference)
    val freshUnit = freshLectures.firstOrNull { it.id == target.lectureId }?.let(::unit)
    val after = details()
    if (after is NetworkResponse.Failure) return NetworkResponse.Failure(after.exception)
    kotlin.coroutines.coroutineContext.ensureActive()
    val afterLecture = (after as NetworkResponse.Response).data
    val afterUnit = unit(afterLecture)
    val afterAttachment = afterUnit?.attachment
    if (freshTarget?.lectureId != target.lectureId || freshTarget.unitId != target.unitId ||
        freshUnit?.attachment?.id != afterAttachment?.id ||
        freshUnit?.attachment?.link?.trimStart('/') != afterAttachment?.link?.trimStart('/') ||
        beforeAttachment != afterAttachment || afterUnit == null || freshUnit == null) {
        return NetworkResponse.Failure(IllegalStateException("Lecture attachment changed; retry the reference"))
    }
    val consistent = afterLecture.copy(lectureUnits = listOf(afterUnit.copy(slides = freshUnit.slides)))
    return resolveLectureReference(listOf(consistent), "slide", reference)
        ?.let { NetworkResponse.Response(it.copy(document = afterAttachment)) }
        ?: NetworkResponse.Failure(NoSuchElementException("Lecture slide unavailable"))
}
