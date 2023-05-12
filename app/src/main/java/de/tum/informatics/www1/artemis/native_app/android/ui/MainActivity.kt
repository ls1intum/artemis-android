package de.tum.informatics.www1.artemis.native_app.android.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import de.tum.informatics.www1.artemis.native_app.android.BuildConfig
import de.tum.informatics.www1.artemis.native_app.core.common.withPrevious
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.isLoggedIn
import de.tum.informatics.www1.artemis.native_app.core.ui.LinkOpener
import de.tum.informatics.www1.artemis.native_app.core.ui.LocalLinkOpener
import de.tum.informatics.www1.artemis.native_app.core.ui.LocalWindowSizeClassProvider
import de.tum.informatics.www1.artemis.native_app.core.ui.WindowSizeClassProvider
import de.tum.informatics.www1.artemis.native_app.feature.course_registration.courseRegistration
import de.tum.informatics.www1.artemis.native_app.feature.course_registration.navigateToCourseRegistration
import de.tum.informatics.www1.artemis.native_app.feature.course_view.course
import de.tum.informatics.www1.artemis.native_app.feature.course_view.navigateToCourse
import de.tum.informatics.www1.artemis.native_app.feature.dashboard.DASHBOARD_DESTINATION
import de.tum.informatics.www1.artemis.native_app.feature.dashboard.dashboard
import de.tum.informatics.www1.artemis.native_app.feature.dashboard.navigateToDashboard
import de.tum.informatics.www1.artemis.native_app.feature.exercise_view.ExerciseViewDestination
import de.tum.informatics.www1.artemis.native_app.feature.exercise_view.ExerciseViewMode
import de.tum.informatics.www1.artemis.native_app.feature.exercise_view.exercise
import de.tum.informatics.www1.artemis.native_app.feature.exercise_view.navigateToExercise
import de.tum.informatics.www1.artemis.native_app.feature.lecture_view.lecture
import de.tum.informatics.www1.artemis.native_app.feature.lecture_view.navigateToLecture
import de.tum.informatics.www1.artemis.native_app.feature.login.LOGIN_DESTINATION
import de.tum.informatics.www1.artemis.native_app.feature.login.loginScreen
import de.tum.informatics.www1.artemis.native_app.feature.login.navigateToLogin
import de.tum.informatics.www1.artemis.native_app.feature.metis.model.MetisContext
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.create_personal_conversation.createPersonalConversationScreen
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.detail.conversationDetailScreen
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.overview.conversationOverviewScreen
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.overview.navigateToConversationOverviewScreen
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.qna.create_standalone_post.createStandalonePostScreen
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.view_post.ViewType
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.view_post.navigateToStandalonePostScreen
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.view_post.standalonePostScreen
import de.tum.informatics.www1.artemis.native_app.feature.metis.visible_metis_context_reporter.ProvideLocalVisibleMetisContextManager
import de.tum.informatics.www1.artemis.native_app.feature.metis.visible_metis_context_reporter.VisibleMetisContext
import de.tum.informatics.www1.artemis.native_app.feature.metis.visible_metis_context_reporter.VisibleMetisContextManager
import de.tum.informatics.www1.artemis.native_app.feature.metis.visible_metis_context_reporter.VisibleMetisContextReporter
import de.tum.informatics.www1.artemis.native_app.feature.metis.visible_metis_context_reporter.VisibleStandalonePostDetails
import de.tum.informatics.www1.artemis.native_app.feature.push.communication_notification_model.CommunicationType
import de.tum.informatics.www1.artemis.native_app.feature.push.service.CommunicationNotificationManager
import de.tum.informatics.www1.artemis.native_app.feature.quiz.QuizType
import de.tum.informatics.www1.artemis.native_app.feature.quiz.participation.navigateToQuizParticipation
import de.tum.informatics.www1.artemis.native_app.feature.quiz.participation.quizParticipation
import de.tum.informatics.www1.artemis.native_app.feature.quiz.view_result.navigateToQuizResult
import de.tum.informatics.www1.artemis.native_app.feature.quiz.view_result.quizResults
import de.tum.informatics.www1.artemis.native_app.feature.settings.navigateToSettings
import de.tum.informatics.www1.artemis.native_app.feature.settings.settingsScreen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.android.get

/**
 * Main and only activity used in the android app.
 * Navigation is handled by decompose and jetpack compose.
 */
class MainActivity : AppCompatActivity(), VisibleMetisContextReporter {

    private val accountService: AccountService = get()
    private val communicationNotificationManager: CommunicationNotificationManager = get()

    override val visibleMetisContexts: MutableStateFlow<List<VisibleMetisContext>> =
        MutableStateFlow(
            emptyList()
        )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // When the user is logged in, immediately display the course overview.
        val startDestination = runBlocking {
            when (accountService.authenticationData.first()) {
                is AccountService.AuthenticationData.LoggedIn -> DASHBOARD_DESTINATION
                AccountService.AuthenticationData.NotLoggedIn -> LOGIN_DESTINATION
            }
        }

        val visibleMetisContextManager = object : VisibleMetisContextManager {
            override fun registerMetisContext(metisContext: VisibleMetisContext) {
                visibleMetisContexts.value = visibleMetisContexts.value + metisContext

                cancelCommunicationNotifications(metisContext)
            }

            override fun unregisterMetisContext(metisContext: VisibleMetisContext) {
                visibleMetisContexts.value = visibleMetisContexts.value - metisContext
            }

        }

        setContent {
            AppTheme {
                ProvideLocalVisibleMetisContextManager(visibleMetisContextManager = visibleMetisContextManager) {
                    MainActivityComposeUi(startDestination)
                }
            }
        }
    }

    @Composable
    private fun MainActivityComposeUi(startDestination: String) {
        val navController = rememberNavController()

        // Listen for when the user get logged out (e.g. because their token has expired)
        // This only happens when the user has the app running for multiple days or the user logged out manually
        LaunchedEffect(Unit) {
            accountService.authenticationData.withPrevious()
                .map { (prev, now) -> prev?.isLoggedIn to now.isLoggedIn }
                .collect { (wasLoggedIn, isLoggedIn) ->
                    if (wasLoggedIn == true && !isLoggedIn) {
                        navController.navigateToLogin {
                            popUpTo(DASHBOARD_DESTINATION) {
                                inclusive = true
                            }
                        }
                    }
                }
        }

        val onLoggedIn = {
            // Navigate to the course overview and remove the login screen from the navigation stack.
            navController.navigateToDashboard {
                popUpTo(LOGIN_DESTINATION) {
                    inclusive = true
                }
            }
        }

        val windowSizeClassProvider = remember {
            object : WindowSizeClassProvider {
                @Composable
                override fun provideWindowSizeClass(): WindowSizeClass =
                    calculateWindowSizeClass(
                        activity = this@MainActivity
                    )
            }
        }

        val onNavigateToTextExerciseParticipation =
            { exerciseId: Long, participationId: Long ->
                navController.navigateToExercise(
                    exerciseId,
                    ExerciseViewMode.TextParticipation(participationId)
                ) {}
            }

        val onNavigateToExerciseResultView = { exerciseId: Long ->
            navController.navigateToExercise(
                exerciseId,
                ExerciseViewMode.ViewResult
            ) {}
        }

        val onParticipateInQuiz = { courseId: Long, exerciseId: Long, isPractice: Boolean ->
            navController.navigateToQuizParticipation(
                courseId,
                exerciseId,
                if (isPractice) QuizType.Practice else QuizType.Live
            )
        }

        val onClickViewQuizResults = { courseId: Long, exerciseId: Long ->
            navController.navigateToQuizResult(courseId, exerciseId)
        }

        val linkOpener = remember {
            CustomTabsLinkOpener(this@MainActivity)
        }

        CompositionLocalProvider(
            LocalWindowSizeClassProvider provides windowSizeClassProvider,
            LocalLinkOpener provides linkOpener
        ) {
            // Use jetpack compose navigation for the navigation logic.
            NavHost(navController = navController, startDestination = startDestination) {
                loginScreen(
                    onFinishedLoginFlow = onLoggedIn,
                    onRequestOpenSettings = {
                        navController.navigateToSettings { }
                    }
                )

                dashboard(
                    onOpenSettings = {
                        navController.navigateToSettings { }
                    },
                    onClickRegisterForCourse = {
                        navController.navigateToCourseRegistration { }
                    },
                    onViewCourse = { courseId ->
                        navController.navigateToCourse(courseId) { }
                    }
                )

                courseRegistration(
                    onNavigateUp = navController::navigateUp,
                    onRegisteredInCourse = { courseId ->
                        navController.navigateUp()
                        navController.navigateToCourse(courseId) { }
                    }
                )

                course(
                    onNavigateToExercise = { exerciseId ->
                        navController.navigateToExercise(
                            exerciseId,
                            ExerciseViewMode.Overview
                        ) { }
                    },
                    onNavigateToTextExerciseParticipation = onNavigateToTextExerciseParticipation,
                    onNavigateToExerciseResultView = onNavigateToExerciseResultView,
                    onParticipateInQuiz = onParticipateInQuiz,
                    onNavigateToLecture = { _, lectureId ->
                        navController.navigateToLecture(
                            lectureId = lectureId
                        ) { }
                    },
                    onNavigateBack = navController::navigateUp,
                    navController = navController,
                    onViewQuizResults = onClickViewQuizResults,
                    onNavigateToMessages = { courseId ->
                        navController.navigateToConversationOverviewScreen(
                            courseId
                        ) { }
                    }
                )

                exercise(
                    navController = navController,
                    onNavigateBack = navController::navigateUp,
                    onParticipateInQuiz = onParticipateInQuiz,
                    onClickViewQuizResults = onClickViewQuizResults
                )

                lecture(
                    navController = navController,
                    onNavigateBack = navController::navigateUp,
                    onViewExercise = { exerciseId ->
                        navController.navigateToExercise(
                            exerciseId,
                            ExerciseViewMode.Overview
                        ) { }
                    },
                    onNavigateToExerciseResultView = onNavigateToExerciseResultView,
                    onNavigateToTextExerciseParticipation = onNavigateToTextExerciseParticipation,
                    onParticipateInQuiz = onParticipateInQuiz,
                    onClickViewQuizResults = onClickViewQuizResults
                )

                standalonePostScreen(
                    onNavigateUp = navController::navigateUp
                )

                createStandalonePostScreen(
                    onNavigateUp = navController::navigateUp,
                    onCreatedPost = { clientSidePostId, metisContext ->
                        navController.navigateUp()
                        navController.navigateToStandalonePostScreen(
                            clientSidePostId,
                            metisContext,
                            ViewType.POST
                        ) {}
                    }
                )

                conversationOverviewScreen(
                    navController = navController,
                    onNavigateBack = navController::navigateUp
                )

                conversationDetailScreen(
                    navController = navController,
                    onNavigateBack = navController::navigateUp
                )

                createPersonalConversationScreen(
                    navController = navController,
                    onNavigateBack = navController::navigateUp
                )

                quizParticipation(
                    onLeaveQuiz = {
                        val previousBackStackEntry = navController.previousBackStackEntry
                        if (previousBackStackEntry?.destination?.route == ExerciseViewDestination.EXERCISE_VIEW_ROUTE) {
                            previousBackStackEntry.savedStateHandle[ExerciseViewDestination.REQUIRE_RELOAD_KEY] =
                                true
                        }
                        navController.navigateUp()
                    }
                )

                quizResults(
                    onRequestLeaveQuizResults = navController::navigateUp
                )

                settingsScreen(
                    navController = navController,
                    versionCode = BuildConfig.VERSION_CODE,
                    versionName = BuildConfig.VERSION_NAME,
                    onNavigateUp = navController::navigateUp,
                    onLoggedOut = {
                        // Nothing to do here, automatically moved to login screen
                    }
                ) {
                    val intent =
                        Intent(this@MainActivity, OssLicensesMenuActivity::class.java)
                    startActivity(intent)
                }
            }
        }
    }

    private class CustomTabsLinkOpener(private val context: Context) : LinkOpener {
        override fun openLink(url: String) {
            CustomTabsIntent.Builder().build()
                .launchUrl(context, Uri.parse(url))
        }
    }

    override fun onResume() {
        super.onResume()

        visibleMetisContexts.value.forEach(::cancelCommunicationNotifications)
    }

    private fun cancelCommunicationNotifications(visibleMetisContext: VisibleMetisContext) {
        if (visibleMetisContext is VisibleStandalonePostDetails) {
            val parentId = visibleMetisContext.postId
            val communicationType: CommunicationType = when (visibleMetisContext.metisContext) {
                is MetisContext.Course -> CommunicationType.QNA_COURSE
                is MetisContext.Exercise -> CommunicationType.QNA_EXERCISE
                is MetisContext.Lecture -> CommunicationType.QNA_LECTURE
                is MetisContext.Conversation -> throw NotImplementedError()
            }

            lifecycleScope.launch {
                communicationNotificationManager.deleteCommunication(parentId, communicationType)
            }
        }
    }
}
