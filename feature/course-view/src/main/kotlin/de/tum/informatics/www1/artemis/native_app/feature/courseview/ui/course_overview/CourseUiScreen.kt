package de.tum.informatics.www1.artemis.native_app.feature.courseview.ui.course_overview

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.model.Course
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.Exercise
import de.tum.informatics.www1.artemis.native_app.core.model.lecture.Lecture
import de.tum.informatics.www1.artemis.native_app.core.ui.common.EmptyDataStateUi
import de.tum.informatics.www1.artemis.native_app.core.ui.common.course.CourseSearchConfiguration
import de.tum.informatics.www1.artemis.native_app.core.ui.deeplinks.CommunicationDeeplinks
import de.tum.informatics.www1.artemis.native_app.core.ui.deeplinks.CourseDeeplinks
import de.tum.informatics.www1.artemis.native_app.core.ui.deeplinks.ExerciseDeeplinks
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.BoundExerciseActions
import de.tum.informatics.www1.artemis.native_app.core.ui.navigation.animatedComposable
import de.tum.informatics.www1.artemis.native_app.feature.courseview.GroupedByWeek
import de.tum.informatics.www1.artemis.native_app.feature.courseview.R
import de.tum.informatics.www1.artemis.native_app.feature.courseview.ui.CourseViewModel
import de.tum.informatics.www1.artemis.native_app.feature.courseview.ui.LectureListUi
import de.tum.informatics.www1.artemis.native_app.feature.courseview.ui.exercise_list.ExerciseListUi
import de.tum.informatics.www1.artemis.native_app.feature.metis.ConversationConfiguration
import de.tum.informatics.www1.artemis.native_app.feature.metis.IgnoreCustomBackHandling
import de.tum.informatics.www1.artemis.native_app.feature.metis.NavigateToUserConversation
import de.tum.informatics.www1.artemis.native_app.feature.metis.NothingOpened
import de.tum.informatics.www1.artemis.native_app.feature.metis.OpenedConversation
import de.tum.informatics.www1.artemis.native_app.feature.metis.OpenedThread
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.MetisContext
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.StandalonePostId
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.UserIdentifier
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.visiblemetiscontextreporter.ReportVisibleMetisContext
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.visiblemetiscontextreporter.VisibleCourse
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.ConversationFacadeUi
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf


@Serializable
private data class CourseUiScreen(
    val courseId: Long,
    val conversationId: Long? = null,
    val postId: Long? = null,
    val username: String? = null,
    val userId: Long? = null
)

@Serializable
internal sealed class CourseTab {
    @Serializable
    data object Exercises : CourseTab()
    @Serializable
    data object Lectures : CourseTab()
    @Serializable
    data object Communication : CourseTab()
}

fun NavController.navigateToCourse(courseId: Long, builder: NavOptionsBuilder.() -> Unit) {
    navigate(CourseUiScreen(courseId), builder)
}

fun NavGraphBuilder.course(
    onNavigateToExercise: (exerciseId: Long) -> Unit,
    onNavigateToExerciseResultView: (exerciseId: Long) -> Unit,
    onNavigateToTextExerciseParticipation: (exerciseId: Long, participationId: Long) -> Unit,
    onParticipateInQuiz: (courseId: Long, exerciseId: Long, isPractice: Boolean) -> Unit,
    onViewQuizResults: (courseId: Long, exerciseId: Long) -> Unit,
    onNavigateToLecture: (courseId: Long, lectureId: Long) -> Unit,
    onNavigateBack: () -> Unit
) {
    val deepLinks = CourseDeeplinks.ToCourse.generateLinks() +
            ExerciseDeeplinks.ToExerciseOverview.generateLinks() +
            CommunicationDeeplinks.ToConversation.generateLinks() +
            CommunicationDeeplinks.ToOneToOneChatByUsername.generateLinks() +
            CommunicationDeeplinks.ToOneToOneChatByUserId.generateLinks() +
            CommunicationDeeplinks.ToPostById.generateLinks()
    animatedComposable<CourseUiScreen>(
        deepLinks = deepLinks
    ) { backStackEntry ->
        val route: CourseUiScreen = backStackEntry.toRoute()
        val courseId = route.courseId

        val conversationId = route.conversationId
        val postId = route.postId
        val username = route.username
        val userId = route.userId

        CourseUiScreen(
            modifier = Modifier.fillMaxSize(),
            viewModel = koinViewModel { parametersOf(courseId) },
            courseId = courseId,
            conversationId = conversationId,
            postId = postId,
            username = username,
            userId = userId,
            onNavigateToExercise = onNavigateToExercise,
            onNavigateToTextExerciseParticipation = onNavigateToTextExerciseParticipation,
            onNavigateToExerciseResultView = onNavigateToExerciseResultView,
            onParticipateInQuiz = { exerciseId, isPractice ->
                onParticipateInQuiz(
                    courseId,
                    exerciseId,
                    isPractice
                )
            },
            onClickViewQuizResults = onViewQuizResults,
            onNavigateToLecture = { lectureId -> onNavigateToLecture(courseId, lectureId) },
            onNavigateBack = onNavigateBack
        )
    }
}

@Composable
fun CourseUiScreen(
    modifier: Modifier,
    viewModel: CourseViewModel,
    courseId: Long,
    conversationId: Long? = null,
    postId: Long? = null,
    username: String? = null,
    userId: Long? = null,
    onNavigateToExercise: (exerciseId: Long) -> Unit,
    onNavigateToTextExerciseParticipation: (exerciseId: Long, participationId: Long) -> Unit,
    onNavigateToExerciseResultView: (exerciseId: Long) -> Unit,
    onParticipateInQuiz: (exerciseId: Long, isPractice: Boolean) -> Unit,
    onClickViewQuizResults: (courseId: Long, exerciseId: Long) -> Unit,
    onNavigateToLecture: (lectureId: Long) -> Unit,
    onNavigateBack: () -> Unit
) {
    val courseDataState by viewModel.course.collectAsState()
    val weeklyExercisesDataState by viewModel.exercisesGroupedByWeek.collectAsState()
    val weeklyLecturesDataState by viewModel.lecturesGroupedByWeek.collectAsState()

    CourseUiScreen(
        modifier = modifier,
        conversationId = conversationId,
        courseDataState = courseDataState,
        username = username,
        userId = userId,
        onNavigateBack = onNavigateBack,
        weeklyExercisesDataState = weeklyExercisesDataState,
        onNavigateToExercise = onNavigateToExercise,
        onNavigateToTextExerciseParticipation = onNavigateToTextExerciseParticipation,
        onParticipateInQuiz = onParticipateInQuiz,
        onNavigateToExerciseResultView = onNavigateToExerciseResultView,
        onClickViewQuizResults = onClickViewQuizResults,
        courseId = courseId,
        weeklyLecturesDataState = weeklyLecturesDataState,
        onNavigateToLecture = onNavigateToLecture,
        postId = postId,
        onReloadCourse = viewModel::reloadCourse,
        onUpdateExerciseQuery = viewModel::onUpdateExerciseQuery,
        onUpdateLectureQuery = viewModel::onUpdateLectureQuery,
        onClickStartTextExercise = { exerciseId: Long ->
            viewModel.startExercise(exerciseId) { participationId ->
                onNavigateToTextExerciseParticipation(
                    exerciseId,
                    participationId
                )
            }
        }
    )
}

@Composable
internal fun CourseUiScreen(
    modifier: Modifier,
    courseId: Long,
    conversationId: Long? = null,
    postId: Long? = null,
    username: String? = null,
    userId: Long? = null,
    courseDataState: DataState<Course>,
    weeklyExercisesDataState: DataState<List<GroupedByWeek<Exercise>>>,
    weeklyLecturesDataState: DataState<List<GroupedByWeek<Lecture>>>,
    onNavigateToExercise: (exerciseId: Long) -> Unit,
    onNavigateToTextExerciseParticipation: (exerciseId: Long, participationId: Long) -> Unit,
    onParticipateInQuiz: (exerciseId: Long, isPractice: Boolean) -> Unit,
    onNavigateToExerciseResultView: (exerciseId: Long) -> Unit,
    onClickViewQuizResults: (courseId: Long, exerciseId: Long) -> Unit,
    onNavigateToLecture: (lectureId: Long) -> Unit,
    onClickStartTextExercise: (exerciseId: Long) -> Unit,
    onNavigateBack: () -> Unit,
    onReloadCourse: () -> Unit,
    onUpdateExerciseQuery: (String) -> Unit,
    onUpdateLectureQuery: (String) -> Unit
) {
    ReportVisibleMetisContext(VisibleCourse(MetisContext.Course(courseId)))

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()

    // This scaffold function is needed because of the way the navigation in the communication tab
    // is handled and the fact that the communicationTab supports the tablet layout. In the tablet
    // layout we want to display the scaffold always, while for the normal (phone) layout the
    // scaffold is only shown in the ConversationOverviewScreen.
    val scaffold = @Composable { searchConfiguration: CourseSearchConfiguration, content: @Composable () -> Unit ->
        CourseScaffold(
            modifier = modifier,
            courseDataState = courseDataState,
            isCourseTabSelected = { tab ->
                val currentDestination = navBackStackEntry?.destination
                currentDestination?.hierarchy?.any { it.hasRoute(tab::class) } == true
            },
            updateSelectedCourseTab = {
                navController.navigate(it) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            onNavigateBack = onNavigateBack,
            onReloadCourse = onReloadCourse,
            searchConfiguration = searchConfiguration,
            content = content
        )
    }

    val initialTab = when {
        conversationId != null || postId != null -> CourseTab.Communication
        username != null || userId != null -> CourseTab.Communication
        else -> CourseTab.Exercises
    }

    NavHost(
        navController = navController,
        startDestination = initialTab::class,
    ) {
        composable<CourseTab.Exercises> {
            scaffold(
                CourseSearchConfiguration.Search(
                    query = "",
                    hint = stringResource(id = R.string.course_ui_exercises_search_hint),
                    onUpdateQuery = onUpdateExerciseQuery
                )
            ) {
                EmptyDataStateUi(dataState = weeklyExercisesDataState) { weeklyExercises ->
                    ExerciseListUi(
                        modifier = Modifier.fillMaxSize(),
                        weeklyExercises = weeklyExercises,
                        onClickExercise = onNavigateToExercise,
                        actions = BoundExerciseActions(
                            onClickStartTextExercise = onClickStartTextExercise,
                            onClickOpenQuiz = { exerciseId ->
                                onParticipateInQuiz(exerciseId, false)
                            },
                            onClickPracticeQuiz = { exerciseId ->
                                onParticipateInQuiz(exerciseId, true)
                            },
                            onClickStartQuiz = { exerciseId ->
                                onParticipateInQuiz(exerciseId, false)
                            },
                            onClickOpenTextExercise = onNavigateToTextExerciseParticipation,
                            onClickViewResult = onNavigateToExerciseResultView,
                            onClickViewQuizResults = { exerciseId ->
                                onClickViewQuizResults(
                                    courseId,
                                    exerciseId
                                )
                            }
                        )
                    )
                }
            }
        }

        composable<CourseTab.Lectures> {
            scaffold(
                CourseSearchConfiguration.Search(
                    query = "",
                    hint = stringResource(id = R.string.course_ui_lectures_search_hint),
                    onUpdateQuery = onUpdateLectureQuery
                )
            ) {
                EmptyDataStateUi(dataState = weeklyLecturesDataState) { weeklyLectures ->
                    LectureListUi(
                        modifier = Modifier.fillMaxSize(),
                        lectures = weeklyLectures,
                        onClickLecture = { onNavigateToLecture(it.id ?: 0L) }
                    )
                }
            }
        }

        composable<CourseTab.Communication> {
            EmptyDataStateUi(
                dataState = courseDataState,
                otherwise = {
                    scaffold(CourseSearchConfiguration.DisabledSearch) {}
                }
            ) { course ->
                val isCommunicationEnabled = course.courseInformationSharingConfiguration.supportsMessaging

                if (!isCommunicationEnabled) {
                    scaffold(CourseSearchConfiguration.DisabledSearch) {
                        CommunicationDisabledInfo()
                    }
                    return@EmptyDataStateUi
                }

                val initialConfiguration = remember(conversationId, postId, username, userId) {
                    getInitialConversationConfiguration(
                        conversationId,
                        postId,
                        username,
                        userId
                    )
                }

                ConversationFacadeUi(
                    modifier = Modifier.fillMaxSize(),
                    courseId = courseId,
                    scaffold = scaffold,
                    initialConfiguration = initialConfiguration
                )
            }
        }
    }
}

@Composable
private fun CommunicationDisabledInfo() {
    Box(modifier = Modifier.fillMaxSize()) {
        Text(
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.Center),
            text = stringResource(id = R.string.course_ui_communication_disabled),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

private fun getInitialConversationConfiguration(
    conversationId: Long?,
    postId: Long?,
    username: String?,
    userId: Long?
): ConversationConfiguration = when {
    conversationId != null && postId != null -> OpenedConversation(
        _prevConfiguration = IgnoreCustomBackHandling,
        conversationId = conversationId,
        openedThread = OpenedThread(
            StandalonePostId.ServerSideId(postId)
        )
    )

    conversationId != null -> OpenedConversation(
        _prevConfiguration = IgnoreCustomBackHandling,
        conversationId = conversationId,
        openedThread = null
    )

    username != null -> NavigateToUserConversation(
        _prevConfiguration = IgnoreCustomBackHandling,
        userIdentifier = UserIdentifier.Username(username)
    )

    userId != null -> NavigateToUserConversation(
        _prevConfiguration = IgnoreCustomBackHandling,
        userIdentifier = UserIdentifier.UserId(userId)
    )

    else -> NothingOpened
}
